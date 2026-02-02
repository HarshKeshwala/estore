package com.estore.productservice.product.mapper;

import com.estore.productservice.product.dto.request.ProductRequest;
import com.estore.productservice.product.dto.response.ProductResponse;
import com.estore.productservice.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponse toResponse(Product product);

    Product toEntity(ProductRequest request);

    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
