package vysion.profile.domain.service;

import java.util.List;

import vysion.profile.domain.model.Product;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
    Product updateProduct(Long id, Product domain);
}