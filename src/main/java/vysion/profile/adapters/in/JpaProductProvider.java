package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import vysion.profile.domain.model.Product;
import vysion.profile.domain.port.ProductProvider;

@Repository
public class JpaProductProvider implements ProductProvider{

    
    private final JpaProductRepository productRepository;
    private final ProductEntityMapper productEntityMapper;

    @Autowired
    public JpaProductProvider(JpaProductRepository productRepository,ProductEntityMapper productEntityMapper) {
        this.productRepository = productRepository;
        this.productEntityMapper = productEntityMapper;
    }
    @Override
    public List<Product> findAll() {
        return productEntityMapper.toDomainList(productRepository.findAll());
    }

    @Override
    public Product findById(Long id) {
        return productEntityMapper.toDomain(productRepository.findById(id).orElseThrow());
    }

    @Override
    public Product save(Product product) {
        return productEntityMapper.toDomain(productRepository.save(productEntityMapper.toEntity(product)));
    }
    @Override
    public void deleteById(Long id) {
       productRepository.deleteById(id);
    }
    @Override
    public boolean existeByName(String name) {
        return productRepository.existsByName(name);
    }


    
}
