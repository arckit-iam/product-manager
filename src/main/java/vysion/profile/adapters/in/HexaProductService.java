package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vysion.profile.domain.exception.ExistProductNameConstraintViolationException;
import vysion.profile.domain.exception.NotFoundException;
import vysion.profile.domain.exception.UnidentifiedException;
import vysion.profile.domain.exception.UniqueProductNameConstraintViolationException;
import vysion.profile.domain.model.Product;
import vysion.profile.domain.model.Profile;
import vysion.profile.domain.model.Ressource;
import vysion.profile.domain.port.ProductProvider;
import vysion.profile.domain.port.ProfileProvider;
import vysion.profile.domain.service.ProfileApplicaManager;

@Service
public class HexaProductService implements ProfileApplicaManager {

    private final ProductProvider productProvider;
    private final ProfileProvider profileProvider;
    
    @Autowired
    HexaProductService(ProductProvider productProvider,ProfileProvider profileProvider){
        this.productProvider = productProvider;
        this.profileProvider = profileProvider;
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
        profileProvider.save(profile,productName);
        Product result = productProvider.findByProductName(productName);
        result.setProfiles(profileProvider.getProfilesByProduct(productName));
        return result;
    }

    @Override
    public Product deleteProfileToProduct(String productName, Profile profile) {
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
