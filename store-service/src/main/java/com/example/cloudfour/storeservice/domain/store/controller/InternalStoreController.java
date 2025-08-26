package com.example.cloudfour.storeservice.domain.store.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.storeservice.domain.commondto.StoreCartResponseDTO;
import com.example.cloudfour.storeservice.domain.store.converter.StoreConverter;
import com.example.cloudfour.storeservice.domain.store.dto.StoreResponseDTO;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import com.example.cloudfour.storeservice.domain.store.exception.StoreErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreException;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import com.example.cloudfour.storeservice.domain.store.service.query.StoreQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/stores")
public class InternalStoreController {
    private final StoreRepository query;
    private final StoreQueryService queryservice;

    @RequestMapping(value = "/exists", method = RequestMethod.HEAD)
    public CustomResponse<Boolean> existsByStoreId(@RequestParam UUID storeId) {
        boolean exists = query.existsByIdAndIsDeletedFalse(storeId);

        if (exists) {
            return CustomResponse.onSuccess(HttpStatus.OK, true);
        } else {
            return CustomResponse.onSuccess(HttpStatus.NOT_FOUND, false);
        }
    }

    @GetMapping("/{storeId}")
    public StoreCartResponseDTO getStoreDetail(
            @PathVariable UUID storeId
    ) {
        return queryservice.findStore(storeId);
    }

}
