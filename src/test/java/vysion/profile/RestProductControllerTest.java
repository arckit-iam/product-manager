package vysion.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import secop.ppm1.adapters.expose.ProductDto;
import secop.ppm1.adapters.expose.ProductDtoMapper;
import secop.ppm1.adapters.expose.ProfileDto;
import secop.ppm1.adapters.expose.ProfileDtoMapper;
import secop.ppm1.adapters.expose.RestProductController;
import secop.ppm1.domain.model.Product;
import secop.ppm1.domain.model.Profile;
import secop.ppm1.domain.service.ProfileApplicaManager;
import secop.ppm1.ProfileApplication;

@WebMvcTest(value = RestProductController.class, 
            properties = "spring.main.allow-bean-definition-overriding=true")
@ContextConfiguration(classes = {RestProductController.class})
public class RestProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileApplicaManager productService;

    @MockBean
    private ProductDtoMapper productDtoMapper;

    @MockBean
    private ProfileDtoMapper profileDtoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private ProductDto testProductDto;
    private Profile testProfile;
    private ProfileDto testProfileDto;

    @BeforeEach
    void setUp() {
        // Setup test data
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        
        testProductDto = new ProductDto();
        testProductDto.setName("Test Product");
        testProductDto.setDescription("Test Description");

        testProfile = new Profile();
        testProfile.setName("Test Profile");
        testProfile.setDescription("Test Profile Description");
        
        testProfileDto = new ProfileDto();
        testProfileDto.setName("Test Profile");
        testProfileDto.setDescription("Test Profile Description");
    }

    @Test
    void testGetAllProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        List<ProductDto> productDtos = Arrays.asList(testProductDto);

        when(productService.getAllProducts()).thenReturn(products);
        when(productDtoMapper.toDtoList(products)).thenReturn(productDtos);

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        verify(productService).getAllProducts();
        verify(productDtoMapper).toDtoList(products);
    }

    @Test
    void testGetProductById() throws Exception {
        // Given
        String productName = "Test Product";
        when(productService.getProduct(productName)).thenReturn(testProduct);
        when(productDtoMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(get("/products/{productName}", productName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(productService).getProduct(productName);
        verify(productDtoMapper).toDto(testProduct);
    }

    @Test
    void testCreateProduct() throws Exception {
        // Given
        when(productDtoMapper.toDomain(any(ProductDto.class))).thenReturn(testProduct);
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);
        when(productDtoMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/products/Test%20Product"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(productDtoMapper).toDomain(any(ProductDto.class));
        verify(productService).createProduct(any(Product.class));
        verify(productDtoMapper).toDto(testProduct);
    }

    @Test
    void testAddProfileToProduct() throws Exception {
        // Given
        String productName = "Test Product";
        when(profileDtoMapper.toDomain(any(ProfileDto.class))).thenReturn(testProfile);
        when(productService.addProfileToProduct(anyString(), any(Profile.class))).thenReturn(testProduct);
        when(productDtoMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(post("/products/{productName}/profiles", productName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProfileDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(profileDtoMapper).toDomain(any(ProfileDto.class));
        verify(productService).addProfileToProduct(productName, testProfile);
        verify(productDtoMapper).toDto(testProduct);
    }

    @Test
    void testUpdateProduct() throws Exception {
        // Given
        String productName = "Test Product";
        when(productDtoMapper.toDomain(any(ProductDto.class))).thenReturn(testProduct);
        when(productService.updateProduct(anyString(), any(Product.class))).thenReturn(testProduct);
        when(productDtoMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(put("/products/{productName}", productName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(productDtoMapper).toDomain(any(ProductDto.class));
        verify(productService).updateProduct(productName, testProduct);
        verify(productDtoMapper).toDto(testProduct);
    }

    @Test
    void testDeleteProduct() throws Exception {
        // Given
        String productName = "Test Product";
        doNothing().when(productService).deleteProduct(productName);

        // When & Then
        mockMvc.perform(delete("/products/{productName}", productName))
                .andExpect(status().isOk());

        verify(productService, times(1)).deleteProduct(productName);
    }

    @Test
    void testGetAllProducts_EmptyList() throws Exception {
        // Given
        List<Product> products = Arrays.asList();
        List<ProductDto> productDtos = Arrays.asList();

        when(productService.getAllProducts()).thenReturn(products);
        when(productDtoMapper.toDtoList(products)).thenReturn(productDtos);

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());

        verify(productService).getAllProducts();
        verify(productDtoMapper).toDtoList(products);
    }

    @Test
    void testCreateProduct_WithNullValues() throws Exception {
        // Given
        ProductDto productWithNulls = new ProductDto();
        productWithNulls.setName(null);
        productWithNulls.setDescription(null);

        Product productDomain = new Product();
        Product createdProduct = new Product();
        createdProduct.setName("Generated");
        createdProduct.setDescription("Generated Description");
        ProductDto createdProductDto = new ProductDto();
        createdProductDto.setName("Generated");
        createdProductDto.setDescription("Generated Description");

        when(productDtoMapper.toDomain(any(ProductDto.class))).thenReturn(productDomain);
        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);
        when(productDtoMapper.toDto(createdProduct)).thenReturn(createdProductDto);

        // When & Then
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productWithNulls)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        verify(productDtoMapper).toDomain(any(ProductDto.class));
        verify(productService).createProduct(any(Product.class));
        verify(productDtoMapper).toDto(createdProduct);
    }

    @Test
    void testAddProfileToProduct_WithNullProfile() throws Exception {
        // Given
        String productName = "Test Product";
        ProfileDto nullProfileDto = new ProfileDto();
        nullProfileDto.setName(null);
        nullProfileDto.setDescription(null);

        Profile nullProfile = new Profile();
        when(profileDtoMapper.toDomain(any(ProfileDto.class))).thenReturn(nullProfile);
        when(productService.addProfileToProduct(anyString(), any(Profile.class))).thenReturn(testProduct);
        when(productDtoMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(post("/products/{productName}/profiles", productName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullProfileDto)))
                .andExpect(status().isOk());

        verify(profileDtoMapper).toDomain(any(ProfileDto.class));
        verify(productService).addProfileToProduct(productName, nullProfile);
        verify(productDtoMapper).toDto(testProduct);
    }

    @Test
    void testUpdateProduct_WithNullValues() throws Exception {
        // Given
        String productName = "Test Product";
        ProductDto productWithNulls = new ProductDto();
        productWithNulls.setName(null);
        productWithNulls.setDescription(null);

        Product productDomain = new Product();

        when(productDtoMapper.toDomain(any(ProductDto.class))).thenReturn(productDomain);
        when(productService.updateProduct(anyString(), any(Product.class))).thenReturn(testProduct);
        when(productDtoMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(put("/products/{productName}", productName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productWithNulls)))
                .andExpect(status().isOk());

        verify(productDtoMapper).toDomain(any(ProductDto.class));
        verify(productService).updateProduct(productName, productDomain);
        verify(productDtoMapper).toDto(testProduct);
    }
}