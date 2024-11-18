package vysion.profile.adapters.in;


import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vysion.profile.domain.model.Product;

@Mapper(componentModel = "spring")
public interface ProductEntityMapper {
    Product toDomain(ProductEntity entity);
    @Mapping(target = "id", ignore = true) // Ignorer le champ id
    ProductEntity toEntity(Product domain);
     // Mapper une liste de ProductEntity vers une liste de Product
    List<Product> toDomainList(List<ProductEntity> entities);

    // Mapper une liste de Product vers une liste de ProductEntity
    List<ProductEntity> toEntityList(List<Product> products);
}
