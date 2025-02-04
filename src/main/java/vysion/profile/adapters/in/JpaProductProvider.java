package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import vysion.profile.domain.exception.NotFoundException;
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
        return productEntityMapper.toDomain(productRepository.findById(id).orElseThrow(()-> new NotFoundException("The product does not exist")));
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = productEntityMapper.toEntity(product);
        if(productRepository.existsByName(product.getName())){
            productEntity.setId(productRepository.findIdByProductName(product.getName()));
        }
        return productEntityMapper.toDomain(productRepository.saveAndFlush(productEntity));
    }
    @Override
    public void deleteById(Long id) {
       productRepository.deleteById(id);
    }
    @Override
    public boolean existeByName(String name) {
        return productRepository.existsByName(name);
    }
    @Override
    public Product findByProductName(String name) {
        return  productEntityMapper.toDomain(productRepository.findByName(name));
    }

    @Override
    public void deleteByName(String name) {
        productRepository.deleteById(productRepository.findIdByProductName(name));
    }


    
}
