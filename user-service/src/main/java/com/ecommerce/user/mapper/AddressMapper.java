package com.ecommerce.user.mapper;

import com.ecommerce.user.entity.Address;
import com.ecommerce.user.entity.dtos.request.CreateAddressRequest;
import com.ecommerce.user.entity.dtos.response.AddressResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    Address createAddressRequestToEntity(CreateAddressRequest request);
    AddressResponse addressToResponse(Address address);
}

