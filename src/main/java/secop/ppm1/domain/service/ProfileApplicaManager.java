package secop.ppm1.domain.service;

import java.util.List;

import secop.ppm1.domain.exception.UniqueProductNameConstraintViolationException;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.model.Ressource;

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