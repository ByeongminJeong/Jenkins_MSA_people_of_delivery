package com.example.cloudfour.userservice.domain.user.service;

import com.example.cloudfour.userservice.domain.region.entity.Region;
import com.example.cloudfour.userservice.domain.region.repository.RegionRepository;
import com.example.cloudfour.userservice.domain.user.converter.UserConverter;
import com.example.cloudfour.userservice.domain.user.dto.UserAddressResponseDTO;
import com.example.cloudfour.userservice.domain.user.dto.UserRequestDTO;
import com.example.cloudfour.userservice.domain.user.dto.UserResponseDTO;
import com.example.cloudfour.userservice.domain.user.entity.User;
import com.example.cloudfour.userservice.domain.user.entity.UserAddress;
import com.example.cloudfour.userservice.domain.user.repository.UserAddressRepository;
import com.example.cloudfour.userservice.domain.user.repository.UserRepository;
import com.example.cloudfour.userservice.domain.user.exception.UserAddressErrorCode;
import com.example.cloudfour.userservice.domain.user.exception.UserAddressException;
import com.example.cloudfour.userservice.domain.region.service.RegionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAddressService {

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final RegionService regionService;

    public UserResponseDTO.AddressResponseDTO addAddress(UUID userId, UserRequestDTO.AddressRequestDTO req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserAddressException(UserAddressErrorCode.NOT_FOUND));

        Region region = regionService.getOrCreateFromAddress(req.address());

        UserAddress ua = UserConverter.toUserAddress(req.address(), user, region);

        userAddressRepository.save(ua);
        return UserConverter.toAddressResponseDTO(ua);
    }


    public List<UserResponseDTO.AddressResponseDTO> getAddresses(UUID userId) {
        return userAddressRepository.findAllByUser_Id(userId).stream()
                .map(UserConverter::toAddressResponseDTO)
                .toList();
    }

    public UserResponseDTO.AddressResponseDTO updateAddress(UUID userId, UUID addressId, UserRequestDTO.AddressRequestDTO req) {
        UserAddress ua = userAddressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new UserAddressException(UserAddressErrorCode.NOT_FOUND));

        Region region = regionService.getOrCreateFromAddress(req.address());
        ua.changeAddress(req.address(), region);

        return UserConverter.toAddressResponseDTO(ua);
    }

    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress ua = userAddressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new UserAddressException(UserAddressErrorCode.NOT_FOUND));
        userAddressRepository.delete(ua);
    }

    public UserAddressResponseDTO findAddress(UUID userId){
        UserAddress ua = userAddressRepository.findPrimaryByIdAndUserId(userId)
                .orElseThrow(() -> new UserAddressException(UserAddressErrorCode.NOT_FOUND));
        return UserConverter.toFindAddress(ua);
    }
}