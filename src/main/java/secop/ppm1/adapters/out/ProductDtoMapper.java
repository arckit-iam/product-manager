package secop.ppm1.adapters.out;


import java.util.List;

import org.mapstruct.Mapper;

import secop.ppm1.domain.model.Product;

@Mapper(componentModel = "spring")
public interface ProductDtoMapper {
    ProductDto toDto(Product domain);
    Product toDomain(ProductDto dto);
    // Mapper une liste de ProductDto vers une liste de Product
    List<Product> toDomainList(List<ProductDto> dtos);

    // Mapper une liste de Product vers une liste de ProductDto
    List<ProductDto> toDtoList(List<Product> products);
}
