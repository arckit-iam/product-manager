package vysion.profile.adapters.in;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vysion.profile.domain.exception.UniqueProductNameConstraintViolationException;
import vysion.profile.domain.model.Product;
import vysion.profile.domain.port.ProductProvider;
import vysion.profile.domain.service.ProductService;

@Service
public class SpringProductService implements ProductService {

    private final ProductProvider productProvider;
    
    @Autowired
    SpringProductService(ProductProvider productProvider){
        this.productProvider = productProvider;
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
    
}
