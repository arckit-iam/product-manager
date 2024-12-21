package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Product addResourceToProduct(Long productId, Ressource resource) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addResourceToProduct'");
    }

    @Override
    public Product addProfileToProduct(Long productId, Profile profile) {    
        profileProvider.save(profile,productId);
        var result = productProvider.findById(productId);
        result.setProfiles(profileProvider.getProfilesByProduct(productId));
        return result;
    }
    
}
