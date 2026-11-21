// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.provide;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import secop.ppm1.domain.exception.ExistProductNameConstraintViolationException;
import secop.ppm1.domain.exception.NotFoundException;
import secop.ppm1.domain.exception.UnidentifiedException;
import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.Ressource;
import secop.ppm1.domain.port.JobPublisher;
import secop.ppm1.domain.port.ProductProvider;
import secop.ppm1.domain.port.ProfileProvider;
import secop.ppm1.domain.service.ProfileApplicaManager;

@Service
public class HexaProductService implements ProfileApplicaManager {

    private final ProductProvider productProvider;
    private final ProfileProvider profileProvider;
    private final JobPublisher jobPublisher;

    @Autowired
    HexaProductService(ProductProvider productProvider, ProfileProvider profileProvider, JobPublisher jobPublisher) {
        this.productProvider = productProvider;
        this.profileProvider = profileProvider;
        this.jobPublisher = jobPublisher;
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
        java.util.UUID jobId = jobPublisher.publishCreateEntitlement(productName, profile);
        profile.setJobId(jobId);
        profileProvider.save(profile, productName);
        Product result = productProvider.findByProductName(productName);
        result.setProfiles(profileProvider.getProfilesByProduct(productName));
        return result;
    }

    @Override
    public Product deleteProfileToProduct(String productName, Profile profile) {
        jobPublisher.publishDeleteEntitlement(productName, profile);
        profileProvider.deleteProfile(profile);
        Product result = productProvider.findByProductName(productName);
        result.setProfiles(profileProvider.getProfilesByProduct(productName));
        return result;
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
