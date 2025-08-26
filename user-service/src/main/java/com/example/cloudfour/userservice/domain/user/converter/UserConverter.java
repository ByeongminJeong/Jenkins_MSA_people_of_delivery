package com.example.cloudfour.userservice.domain.user.converter;

import com.example.cloudfour.userservice.domain.user.dto.UserAddressResponseDTO;
import com.example.cloudfour.userservice.domain.user.dto.UserResponseDTO;
import com.example.cloudfour.userservice.domain.user.entity.User;
import com.example.cloudfour.userservice.domain.user.entity.UserAddress;
import com.example.cloudfour.userservice.domain.region.entity.Region;
import com.example.cloudfour.userservice.domain.user.enums.AddressStatus;

import java.util.List;

public class UserConverter {

    public static UserResponseDTO.MeResponseDTO toMeResponseDTO(User user) {
        return UserResponseDTO.MeResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .number(user.getNumber())
                .role(user.getRole())
                .build();
    }

    public static UserResponseDTO.AddressResponseDTO toAddressResponseDTO(UserAddress address) {
        return UserResponseDTO.AddressResponseDTO.builder()
                .addressId(address.getId())
                .address(address.getAddress())
                .regionId(address.getRegion().getId())
                .build();
    }

    public static UserResponseDTO.AddressListResponseDTO toAddressListResponseDTO(List<UserResponseDTO.AddressResponseDTO> list) {
        return UserResponseDTO.AddressListResponseDTO.builder()
                .addresses(list)
                .build();
    }

    public static UserAddress toUserAddress(String address, User user, Region region) {
        UserAddress ua = UserAddress.builder()
                .address(address)
                .addressStatus(AddressStatus.PRIMARY)
                .build();
        ua.setUser(user);
        ua.setRegion(region);
        return ua;
    }

    public static UserAddressResponseDTO toFindAddress(UserAddress address){
        return UserAddressResponseDTO.builder()
                .address(address.getAddress())
                .build();
    }
}


