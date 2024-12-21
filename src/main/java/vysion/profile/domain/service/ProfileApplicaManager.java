package vysion.profile.domain.service;

import java.util.List;

import vysion.profile.domain.exception.UniqueProductNameConstraintViolationException;
import vysion.profile.domain.model.Product;
import vysion.profile.domain.model.Profile;
import vysion.profile.domain.model.Ressource;

public interface ProfileApplicaManager {
    List<Product> getAllProducts();
    Product getProduct(Long id);
    Product createProduct(Product product) throws UniqueProductNameConstraintViolationException;
    void deleteProduct(Long id);
    Product updateProduct(Long id, Product domain);
    Product addResourceToProduct(Long productId, Ressource resource);
    Product addProfileToProduct(Long productId, Profile profile);
    Product deleteProfileToProduct(Long productId, Profile profile);
}