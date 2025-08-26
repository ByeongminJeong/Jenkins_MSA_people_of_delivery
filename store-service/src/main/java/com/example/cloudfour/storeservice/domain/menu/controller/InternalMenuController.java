package com.example.cloudfour.storeservice.domain.menu.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.storeservice.domain.commondto.MenuCartResponseDTO;
import com.example.cloudfour.storeservice.domain.commondto.MenuOptionCartResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.converter.MenuConverter;
import com.example.cloudfour.storeservice.domain.menu.converter.MenuOptionConverter;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuOptionResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuException;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuOptionErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuOptionException;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuOptionRepository;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuRepository;
import com.example.cloudfour.storeservice.domain.menu.service.query.MenuQueryService;
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
@RequestMapping("/internal/menus")
public class InternalMenuController {
    private final MenuRepository menuQuery;
    private final MenuQueryService query;

    @RequestMapping(value = "/exists", method = RequestMethod.HEAD)
    public CustomResponse<Boolean> existsByMenuId(@RequestParam UUID menuId) {
        boolean exists = menuQuery.existsById(menuId);

        if (exists) {
            return CustomResponse.onSuccess(HttpStatus.OK, true);
        } else {
            return CustomResponse.onSuccess(HttpStatus.NOT_FOUND, false);
        }
    }

    @GetMapping("/{menuId}")
    public MenuCartResponseDTO getMenuDetail(
            @PathVariable("menuId") UUID menuId) {
        return query.findMenu(menuId);
    }

    @GetMapping("/options/{optionId}/detail")
    public MenuOptionCartResponseDTO getMenuOptionDetail(
            @PathVariable("optionId") UUID optionId
    ) {
        return query.findMenuOption(optionId);
    }
}
