package vysion.profile.adapters.out;



import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productDtoMapper.toDtoList(productService.getAllProducts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
            return ResponseEntity.ok(productDtoMapper.toDto(productService.getProduct(id)));
          
       
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto product) {
    
            ProductDto createdProduct =  productDtoMapper.toDto(productService.createProduct(productDtoMapper.toDomain(product)));
            URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdProduct.getName())
            .toUri();
            return ResponseEntity.created(location).body(createdProduct);

    
    }

    @PutMapping("/{id}")
    public ProductDto updateProduct(@PathVariable Long id, @RequestBody ProductDto product) {
        return productDtoMapper.toDto(productService.updateProduct(id,productDtoMapper.toDomain(product)));
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
