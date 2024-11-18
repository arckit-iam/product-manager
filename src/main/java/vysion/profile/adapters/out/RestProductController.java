package vysion.profile.adapters.out;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vysion.profile.domain.service.ProductService;

@RestController
@RequestMapping("/products")
public class RestProductController {


    private final ProductService productService;
    private final ProductDtoMapper productDtoMapper;

    @Autowired
    RestProductController(ProductService productService,ProductDtoMapper productDtoMapper){

        this.productService = productService;
        this.productDtoMapper = productDtoMapper;
    }

    @GetMapping
    public List<ProductDto> getAllProducts() {
        return productDtoMapper.toDtoList(productService.findAll());
    }

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable Long id) {
        return productDtoMapper.toDto(productService.findById(id));
    }

    @PostMapping
    public ProductDto createProduct(@RequestBody ProductDto product) {

        return productDtoMapper.toDto(productService.save(productDtoMapper.toDomain(product)));
    }

    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto product) {
        return productDtoMapper.toDto(productService.updateProduct(id,productDtoMapper.toDomain(product)));
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
    }
}
