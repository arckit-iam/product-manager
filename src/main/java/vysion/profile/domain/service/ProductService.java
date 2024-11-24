package vysion.profile.domain.service;

import java.util.List;

import vysion.profile.domain.exception.UniqueConstraintViolationException;
import vysion.profile.domain.model.Product;

public interface ProductService {
    List<Product> getAllProducts();
    Product getProduct(Long id);
    Product createProduct(Product product) throws UniqueConstraintViolationException;
    void deleteProduct(Long id);
    Product updateProduct(Long id, Product domain);
}