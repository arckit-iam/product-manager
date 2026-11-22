// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import secop.ppm1.domain.exception.AccountConflictException;
import secop.ppm1.domain.exception.ExistProductNameConstraintViolationException;
import secop.ppm1.domain.exception.InvalidAccountException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.exception.UnidentifiedException;
import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.Ressource;
import secop.ppm1.domain.model.ProvisioningOrder;
import secop.ppm1.domain.port.OrderPublisher;
import secop.ppm1.domain.port.ProductProvider;
import secop.ppm1.domain.port.ProfileProvider;
import secop.ppm1.domain.service.OrderSchedule;
import secop.ppm1.domain.service.ProfileApplicaManager;

@Service
public class HexaProductService implements ProfileApplicaManager {

    private final ProductProvider productProvider;
    private final ProfileProvider profileProvider;
    private final OrderPublisher orderPublisher;
    private final OrderSchedule schedule;

    @Autowired
    public HexaProductService(ProductProvider productProvider, ProfileProvider profileProvider,
            OrderPublisher orderPublisher, OrderSchedule schedule) {
        this.productProvider = productProvider;
        this.profileProvider = profileProvider;
        this.orderPublisher = orderPublisher;
        this.schedule = schedule;
    }

    @Override
    public List<Product> getAllProducts() {
        return productProvider.findAll();
    }

    @Override
    public Product getProduct(Long id){
        return productProvider.findById(id);
    }

    @Override
    public Product createProduct(Product product) throws UniqueProductNameConstraintViolationException{
        if(productProvider.existeByName(product.getName())){
            throw new UniqueProductNameConstraintViolationException("The product's name already exists");
        }
        return productProvider.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        productProvider.deleteById(id);
    }

    @Override
    public Product updateProduct(Long id, Product domain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateProduct'");
    }

    @Override
    public Product addResourceToProduct(String productName, Ressource resource) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addResourceToProduct'");
    }

    @Override
    public Product addProfileToProduct(String productName, Profile profile) {
        validateDates(profile.getStartDate(), profile.getEndDate());
        profile.setOrderId(null);
        profile.setEndOrderId(null);
        scheduleCreation(productName, profile);
        if (profile.getEndDate() != null) {
            try {
                scheduleEnd(productName, profile);
            } catch (RuntimeException e) {
                orderPublisher.cancel(profile.getOrderId());
                throw e;
            }
        }
        profileProvider.save(profile, productName);
        return productWithProfiles(productName);
    }

    @Override
    public Product deleteProfileToProduct(String productName, String profileName) {
        Profile profile = loadProfile(productName, profileName);
        cancel(profile.getEndOrderId());
        if (!schedule.hasStarted(profile.getStartDate())) {
            // Création encore à venir : rien n'existe dans l'annuaire, il suffit de l'annuler.
            cancel(profile.getOrderId());
        } else if (!schedule.hasEnded(profile.getEndDate())) {
            orderPublisher.submit(ProvisioningOrder.forProfile(ProvisioningOrder.Action.DELETE, productName, profile,
                    schedule.now(), profileCorrelation(productName, profile) + ":delete"));
        }
        profileProvider.deleteProfile(productName, profileName);
        return productWithProfiles(productName);
    }

    @Override
    public Product updateProfileDates(String productName, String profileName, LocalDate startDate,
            LocalDate endDate) {
        Profile profile = loadProfile(productName, profileName);
        if (schedule.hasEnded(profile.getEndDate())) {
            throw new AccountConflictException("The profile has already expired: " + profileName);
        }
        boolean started = schedule.hasStarted(profile.getStartDate());
        if (started && !Objects.equals(startDate, profile.getStartDate())) {
            throw new InvalidAccountException("The start date cannot change once the profile is created");
        }
        validateDates(startDate, endDate);

        if (!started) {
            cancel(profile.getOrderId());
            profile.setStartDate(startDate);
            scheduleCreation(productName, profile);
        }
        if (!Objects.equals(endDate, profile.getEndDate())) {
            cancel(profile.getEndOrderId());
            profile.setEndOrderId(null);
            profile.setEndDate(endDate);
            if (endDate != null) {
                scheduleEnd(productName, profile);
            }
        }
        profileProvider.save(profile, productName);
        return productWithProfiles(productName);
    }

    @Override
    public Product cancelProfilePending(String productName, String profileName) {
        Profile profile = loadProfile(productName, profileName);
        if (!schedule.hasStarted(profile.getStartDate())) {
            cancel(profile.getOrderId());
            cancel(profile.getEndOrderId());
            profileProvider.deleteProfile(productName, profileName);
        } else if (profile.getEndDate() != null && !schedule.hasEnded(profile.getEndDate())) {
            cancel(profile.getEndOrderId());
            profile.setEndOrderId(null);
            profile.setEndDate(null);
            profileProvider.save(profile, productName);
        } else {
            throw new AccountConflictException("No pending order for the profile: " + profileName);
        }
        return productWithProfiles(productName);
    }

    private Profile loadProfile(String productName, String profileName) {
        if (!productProvider.existeByName(productName)) {
            throw new NotFoundException("The product does not exist: " + productName);
        }
        Profile profile = profileProvider.getProfileByNameAndProduct(profileName, productName);
        if (profile == null) {
            throw new NotFoundException("The profile does not exist: " + profileName);
        }
        return profile;
    }

    private Product productWithProfiles(String productName) {
        Product result = productProvider.findByProductName(productName);
        result.setProfiles(profileProvider.getProfilesByProduct(productName));
        return result;
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new InvalidAccountException("The end date cannot be before the start date");
        }
        if (endDate != null && endDate.isBefore(schedule.today())) {
            throw new InvalidAccountException("The end date cannot be in the past");
        }
    }

    private String profileCorrelation(String productName, Profile profile) {
        return "profile:" + productName + ":" + profile.getName();
    }

    private void scheduleCreation(String productName, Profile profile) {
        ProvisioningOrder order = ProvisioningOrder.forProfile(ProvisioningOrder.Action.CREATE, productName, profile,
                schedule.startAt(profile.getStartDate()), profileCorrelation(productName, profile));
        orderPublisher.submit(order);
        profile.setOrderId(order.id());
    }

    private void scheduleEnd(String productName, Profile profile) {
        ProvisioningOrder order = ProvisioningOrder.forProfile(ProvisioningOrder.Action.DELETE, productName, profile,
                schedule.endAt(profile.getEndDate()), profileCorrelation(productName, profile) + ":end");
        orderPublisher.submit(order);
        profile.setEndOrderId(order.id());
    }

    private void cancel(UUID orderId) {
        if (orderId != null) {
            orderPublisher.cancel(orderId);
        }
    }

    @Override
    public Product getProduct(String productName) {
       return productProvider.findByProductName(productName);
    }

    @Override
    public void deleteProduct(String productName) {
        if(!productProvider.existeByName(productName)){
            throw new NotFoundException("The product's name does not exist");
        }
       try {
        productProvider.deleteByName(productName);
       } catch (Exception e) {
           throw new UnidentifiedException("An unexpected error occurred");
       }
    }
 
    @Override
    public Product updateProduct(String productName, Product domain) {

        if(!productProvider.existeByName(productName)){
            throw new NotFoundException("The product does not exist");
        }
        if(!productProvider.existeByName(domain.getName())){
            throw new ExistProductNameConstraintViolationException("Unhautorized request");
        }
        if(!productName.equals(domain.getName())){
            throw new UniqueProductNameConstraintViolationException("Update failed, the product name can not be changed");
        }
        

        try {
            return productProvider.save(domain);
        } catch (Exception e) {
            throw new UnidentifiedException("An unexpected error occurred");
        }
    }
    
}
