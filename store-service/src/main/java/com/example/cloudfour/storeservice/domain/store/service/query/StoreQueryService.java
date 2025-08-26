package com.example.cloudfour.storeservice.domain.store.service.query;

import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.collection.repository.query.StoreSearchRepository;

import com.example.cloudfour.storeservice.domain.common.RegionResponseDTO;

import com.example.cloudfour.storeservice.domain.commondto.StoreCartResponseDTO;
import com.example.cloudfour.storeservice.domain.region.exception.RegionErrorCode;
import com.example.cloudfour.storeservice.domain.region.exception.RegionException;
import com.example.cloudfour.storeservice.domain.store.converter.StoreConverter;
import com.example.cloudfour.storeservice.domain.store.dto.StoreResponseDTO;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import com.example.cloudfour.storeservice.domain.store.exception.StoreErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreException;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreQueryService {

    private final StoreSearchRepository storeMongoRepository;
    private final StoreRepository query;
    private final RestTemplate rt;
    private static final String BASE = "http://user-service/internal/regions";

    public StoreResponseDTO.StoreCursorListResponseDTO getAllStores(
            LocalDateTime cursor, int size, String keyword, CurrentUser user
    ) {
        if(user==null){
            log.warn("가게 목록 조회 권한 없음");
            throw new StoreException(StoreErrorCode.UNAUTHORIZED_ACCESS);
        }
        RegionResponseDTO findRegion =  rt.getForObject(BASE+"/{userId}",RegionResponseDTO.class,user.id());
        if(findRegion == null){
            log.warn("존재하지 않는 지역");
            throw new RegionException(RegionErrorCode.NOT_FOUND);
        }
        log.info("가게 검색 목록 조회 권한 확인 성공");
        LocalDateTime baseTime = (cursor != null) ? cursor : LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, size);
        Slice<StoreDocument> storeSlice = storeMongoRepository.findAllStoreByKeyWordAndRegion(keyword, baseTime, pageable,
                findRegion.getSiDo(), findRegion.getSiGunGu(), findRegion.getEupMyeonDong());

        List<StoreResponseDTO.StoreListResponseDTO> storeList = storeSlice.getContent().stream()
                .map(StoreConverter::toStoreListResponseDTO)
                .toList();

        LocalDateTime nextCursor = storeSlice.hasNext() && !storeList.isEmpty()
                ? storeList.get(storeList.size() - 1).getCreatedAt()
                : null;
        log.info("가게 검색 목록 조회 성공");
        return StoreConverter.toStoreCursorListResponseDTO(storeList, nextCursor);

    }
    public StoreResponseDTO.StoreCursorListResponseDTO getStoresByCategory(
            UUID categoryId, LocalDateTime cursor, int size,CurrentUser user
    ) {
        if(user==null){
            log.warn("카테고리 별 가게 목록 조회 권한 없음");
            throw new StoreException(StoreErrorCode.UNAUTHORIZED_ACCESS);
        }
        RegionResponseDTO findRegion =  rt.getForObject(BASE+"/{userId}",RegionResponseDTO.class,user.id());
        if(findRegion == null){
            log.warn("존재하지 않는 지역");
            throw new RegionException(RegionErrorCode.NOT_FOUND);
        }
        log.info("가게 카테고리 별 목록 조회 권한 확인 성공");
        LocalDateTime baseTime = (cursor != null) ? cursor : LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, size);
        Slice<StoreDocument> storeSlice = storeMongoRepository.findAllStoreByCategoryAndCursor(categoryId, baseTime, pageable
        , findRegion.getSiDo(), findRegion.getSiGunGu(), findRegion.getEupMyeonDong());

        List<StoreResponseDTO.StoreListResponseDTO> storeList = storeSlice.getContent().stream()
                .map(StoreConverter::toStoreListResponseDTO)
                .toList();

        LocalDateTime nextCursor = storeSlice.hasNext() && !storeList.isEmpty()
                ? storeList.get(storeList.size() - 1).getCreatedAt()
                : null;
        log.info("가게 카테고리 별 목록 조회 성공");
        return StoreResponseDTO.StoreCursorListResponseDTO.of(storeList, nextCursor);
    }

    public StoreResponseDTO.StoreDetailResponseDTO getStoreById(UUID storeId,CurrentUser user) {
        if(user==null){
            log.warn("가게 상세 조회 권한 없음");
            throw new StoreException(StoreErrorCode.UNAUTHORIZED_ACCESS);
        }
        log.info("가게 상세 조회 권한 확인 성공");
        StoreDocument store = storeMongoRepository.findStoreByStoreId(storeId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 가게");
                    return new StoreException(StoreErrorCode.NOT_FOUND);
                });
        log.info("가게 상제 조회 성공");
        return StoreConverter.documentToStoreDetailResponseDTO(store);
    }

    public StoreCartResponseDTO findStore(UUID storeId){
        Store findStore = query.findByIdAndIsDeletedFalse(storeId).orElseThrow(
                ()->new StoreException(StoreErrorCode.NOT_FOUND));
        return StoreConverter.toFindStoreDTO(findStore);
    }

}
