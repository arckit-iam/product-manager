// SPDX-FileCopyrightText: 2026 Dédale Solutions
// SPDX-License-Identifier: EUPL-1.2

package secop.ppm1.adapters.expose;


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
