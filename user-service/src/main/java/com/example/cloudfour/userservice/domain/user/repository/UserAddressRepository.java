package com.example.cloudfour.userservice.domain.user.repository;


import com.example.cloudfour.userservice.domain.user.dto.UserAddressResponseDTO;
import com.example.cloudfour.userservice.domain.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findAllByUser_Id(UUID userId);

    Optional<UserAddress> findByIdAndUser_Id(UUID addressId, UUID userId);

    @Query("select ua from UserAddress ua where ua.user.isDeleted = false and ua.user.id =:userId and ua.addressStatus = " +
            "com.example.cloudfour.userservice.domain.user.enums.AddressStatus.PRIMARY")
    Optional<UserAddress> findPrimaryByIdAndUserId(@Param("userId") UUID userId);

    @Query("select count(ua) > 0 from UserAddress ua where ua.user.isDeleted = false and ua.user.id =:userId and ua.id =:addressId")
    boolean existsByUserIdAndAddressId(@Param("userId") UUID userId, @Param("addressId") UUID addressId);
}
