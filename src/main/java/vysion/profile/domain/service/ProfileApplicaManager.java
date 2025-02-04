package vysion.profile.domain.service;

import java.util.List;

import vysion.profile.domain.exception.UniqueProductNameConstraintViolationException;
import vysion.profile.domain.model.Product;
import vysion.profile.domain.model.Profile;
import vysion.profile.domain.model.Ressource;

public interface ProfileApplicaManager {
    List<Product> getAllProducts();
    Product getProduct(Long id);
    Product getProduct(String productName);
    Product createProduct(Product product) throws UniqueProductNameConstraintViolationException;
    void deleteProduct(Long id);
    void deleteProduct(String productName);
    Product updateProduct(Long id, Product domain);
    Product updateProduct(String productName, Product domain);
    Product addResourceToProduct(String productName, Ressource resource);
    Product addProfileToProduct(String productName, Profile profile);
    Product deleteProfileToProduct(String productName, Profile profile);
}