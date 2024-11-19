package vysion.profile.domain.port;

import java.util.List;

import vysion.profile.domain.model.Product;

public interface ProductProvider {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
}
