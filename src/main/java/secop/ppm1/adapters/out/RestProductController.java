package secop.ppm1.adapters.out;



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

import secop.ppm1.domain.service.ProfileApplicaManager;

@RestController
@RequestMapping("/products")
public class RestProductController {


    private final ProfileApplicaManager productService;
    private final ProductDtoMapper productDtoMapper;
    private final ProfileDtoMapper profileDtoMapper;
    @Autowired
    RestProductController(ProfileApplicaManager productService,ProductDtoMapper productDtoMapper,ProfileDtoMapper profileDtoMapper){

        this.productService = productService;
        this.productDtoMapper = productDtoMapper;
        this.profileDtoMapper = profileDtoMapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productDtoMapper.toDtoList(productService.getAllProducts()));
    }

    @GetMapping("/{productName}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String productName) {
            return ResponseEntity.ok(productDtoMapper.toDto(productService.getProduct(productName)));
          
       
    }
// Does not respect Rest principles. To refactor
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto product) {
    
            ProductDto createdProduct =  productDtoMapper.toDto(productService.createProduct(productDtoMapper.toDomain(product)));
            URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{productName}")
            .buildAndExpand(createdProduct.getName())
            .toUri();
            return ResponseEntity.created(location).body(createdProduct);

    
    }

    // A reprendre



    //  @PostMapping("/{productId}/resources")
    // public ProductDto addResourceToProduct(@PathVariable Long productId, @RequestBody RessourceDto resource) {
    //     return productService.addResourceToProduct(productId, resource);
    // }

    @PostMapping("/{productName}/profiles")
    public ProductDto addProfileToProduct(@PathVariable String productName, @RequestBody ProfileDto profile) {
        return productDtoMapper.toDto(productService.addProfileToProduct(productName, profileDtoMapper.toDomain(profile)));
    }


    @PutMapping("/{productName}")
    public ProductDto updateProduct(@PathVariable String productName, @RequestBody ProductDto product) {
        return productDtoMapper.toDto(productService.updateProduct(productName,productDtoMapper.toDomain(product)));
    }

    @DeleteMapping("/{productName}")
    public void deleteProduct(@PathVariable String productName) {
        productService.deleteProduct(productName);
    }
}
