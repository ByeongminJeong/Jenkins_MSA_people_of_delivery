package com.example.cloudfour.storeservice.domain.menu.service.query;

import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.collection.repository.query.StoreSearchRepository;
import com.example.cloudfour.storeservice.domain.commondto.MenuCartResponseDTO;
import com.example.cloudfour.storeservice.domain.commondto.MenuOptionCartResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.converter.MenuConverter;
import com.example.cloudfour.storeservice.domain.menu.converter.MenuOptionConverter;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuOptionResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuCategoryErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuCategoryException;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuException;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuOptionErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuOptionException;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuCategoryRepository;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuOptionRepository;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuRepository;
import com.example.cloudfour.storeservice.domain.store.exception.StoreErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryService {
    private final MenuCategoryRepository menuCategoryRepository;
    private final StoreSearchRepository storeMongoRepository;
    private final MenuRepository menuQuery;
    private final MenuOptionRepository menuOptionQuery;

    public MenuResponseDTO.MenuStoreListResponseDTO getMenusByStoreWithCursor(
            UUID storeId, CurrentUser user
    ) {
        storeMongoRepository.findStoreByStoreId(storeId).orElseThrow(() -> {
            log.warn("존재하지 않는 가게");
            return new StoreException(StoreErrorCode.NOT_FOUND);
        });

        if(user==null){
            log.warn("가게 메뉴 조회 권한 없음");
            throw new MenuException(MenuErrorCode.UNAUTHORIZED_ACCESS);
        }
        log.info("가게 메뉴 목록 조회 권한 확인 성공");
        List<StoreDocument.Menu> menus =
                storeMongoRepository.findMenuByStoreId(storeId);

        if (menus.isEmpty()){
            log.warn("가게 메뉴 없음");
            throw new MenuException(MenuErrorCode.NOT_FOUND);
        }

        List<MenuResponseDTO.MenuListResponseDTO> menusDto = menus.stream()
                .map(MenuConverter::toMenuListResponseDTO)
                .toList();

        log.info("가게 별 메뉴 목록 조회 성공");
        return MenuResponseDTO.MenuStoreListResponseDTO.builder()
                .menus(menusDto)
                .build();
    }

    public MenuResponseDTO.MenuStoreListResponseDTO getMenusByStoreWithCategory(
            UUID storeId, UUID categoryId,CurrentUser user
    ) {
        storeMongoRepository.findStoreByStoreId(storeId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 가게");
                    return new StoreException(StoreErrorCode.NOT_FOUND);
                });
        menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 메뉴 카테고리");
                    return new MenuCategoryException(MenuCategoryErrorCode.NOT_FOUND);
                });

        if(user==null){
            log.warn("가게, 카테고리 별 메뉴 목록 조회 권한 없음");
            throw new MenuException(MenuErrorCode.UNAUTHORIZED_ACCESS);
        }

        log.info("가게, 카테고리 별 메뉴 목록 조회 권한 확인 성공");
        List<StoreDocument.Menu> menus =
                storeMongoRepository.findMenuByStoreIdAndMenuCategoryId(
                        storeId, categoryId);

        if (menus.isEmpty()) {
            log.warn("가게 메뉴 없음");
            throw new MenuException(MenuErrorCode.NOT_FOUND);
        }

        List<MenuResponseDTO.MenuListResponseDTO> menusDto = menus.stream()
                .map(MenuConverter::toMenuListResponseDTO)
                .toList();

        log.info("가게, 카테고리 별 메뉴 목록 조회 성공");
        return MenuResponseDTO.MenuStoreListResponseDTO.builder()
                .menus(menusDto)
                .build();
    }

//    public List<MenuResponseDTO.MenuTopResponseDTO> getTopMenus(UUID userId) {
//        OrderItemResponseDTO orderItemResponseDTO = restTemplate.getForObject("http://order-service/api/order-items/{userId}", OrderItemResponseDTO.class, userId);
//        return menuRepository.findTopMenusByOrderCount(PageRequest.of(0, 20))
//                .stream()
//                .map(MenuConverter::toMenuTopResponseDTO)
//                .toList();
//    }
//
//    public List<MenuResponseDTO.MenuTimeTopResponseDTO> getTimeTopMenus(UUID userId) {
//        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
//        LocalDateTime endTime = LocalDateTime.now();
//
//        return menuRepository.findTopMenusByTimeRange(startTime, endTime, PageRequest.of(0, 20))
//                .stream()
//                .map(MenuConverter::toMenuTimeTopResponseDTO)
//                .toList();
//    }
//
//    public List<MenuResponseDTO.MenuRegionTopResponseDTO> getRegionTopMenus(String si, String gu, UUID userId) {
//        return menuRepository.findTopMenusByRegion(si, gu, PageRequest.of(0, 20))
//                .stream()
//                .map(MenuConverter::toMenuRegionTopResponseDTO)
//                .toList();
//    }

    public MenuResponseDTO.MenuDetailResponseDTO getMenuDetail(UUID menuId,CurrentUser user) {
        if(user==null){
            log.warn("메뉴 상세 조회 권한 없음");
            throw new MenuException(MenuErrorCode.UNAUTHORIZED_ACCESS);
        }
        StoreDocument.Menu storeDocument = storeMongoRepository.findMenuByMenuId(menuId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 메뉴");
                    return new MenuException(MenuErrorCode.NOT_FOUND);
                });
        log.info("메뉴 상세 조회 권한 확인 성공");
        var optionDTOs = storeMongoRepository.findMenuOptionByMenuIdOrderByAdditionalPrice(menuId)
                .stream()
                .map(MenuConverter::toMenuOptionDTO)
                .toList();

        log.info("메뉴 상세 조회 완료");
        return MenuConverter.toMenuDetail2ResponseDTO(storeDocument, optionDTOs);
    }

    public MenuOptionResponseDTO.MenuOptionsByMenuResponseDTO getMenuOptionsByMenu(UUID menuId,CurrentUser user) {
        if(user==null){
            log.warn("메뉴 별 메뉴 옵션 조회 권한 없음");
            throw new MenuOptionException(MenuOptionErrorCode.UNAUTHORIZED_ACCESS);
        }
        storeMongoRepository.findMenuByMenuId(menuId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 메뉴");
                    return new MenuException(MenuErrorCode.NOT_FOUND);
                });

        log.info("메뉴 별 메뉴 옵션 조회 권한 확인 성공");

        var options = storeMongoRepository.findMenuOptionByMenuIdOrderByAdditionalPrice(menuId)
                .stream()
                .map(MenuOptionConverter::documentToMenuOptionSimpleResponseDTO)
                .toList();
        log.info("메뉴 별 메뉴 옵션 조회 완료");
        return MenuOptionResponseDTO.MenuOptionsByMenuResponseDTO.builder()
                .options(options)
                .build();
    }

    public MenuOptionResponseDTO.MenuOptionSimpleResponseDTO getMenuOptionDetail(UUID optionId,CurrentUser user) {
        if(user==null){
            log.warn("메뉴 옵션 상세 조회 권한 없음");
            throw new MenuOptionException(MenuOptionErrorCode.UNAUTHORIZED_ACCESS);
        }
        log.info("메뉴 옵션 상세 조회 권한 확인 성공");
        StoreDocument.MenuOption option = storeMongoRepository.findMenuOptionByMenuOptionId(optionId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 메뉴 옵션");
                    return new MenuOptionException(MenuOptionErrorCode.NOT_FOUND);
                });
        log.info("메뉴 옵션 상세 조회 완료");
        return MenuOptionConverter.documentToMenuOptionSimpleResponseDTO(option);
    }

    public MenuCartResponseDTO findMenu(UUID menuId){
        Menu findMenu = menuQuery.findById(menuId).orElseThrow(()->new MenuException(MenuErrorCode.NOT_FOUND));
        return MenuConverter.toFindMenuDTO(findMenu);
    }

    public MenuOptionCartResponseDTO findMenuOption(UUID optionId){
        MenuOption findMenuOption = menuOptionQuery.findById(optionId).orElseThrow(
                ()-> new MenuOptionException(MenuOptionErrorCode.NOT_FOUND)
        );
        return MenuOptionConverter.toFindMenuOptionDTO(findMenuOption);
    }
}
