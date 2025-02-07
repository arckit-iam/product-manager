package secop.ppm1.domain.port;

import java.util.List;

import secop.ppm1.domain.model.Product;

public interface ProductProvider {
    List<Product> findAll();
    Product findById(Long id);
    Product findByProductName(String name);
    Product save(Product product);
    void deleteById(Long id);
    void deleteByName(String name);
    boolean existeByName(String name);
}
