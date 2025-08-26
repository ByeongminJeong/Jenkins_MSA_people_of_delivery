package com.example.cloudfour.storeservice.domain.menu.service.query;

import com.example.cloudfour.storeservice.domain.menu.converter.StockConverter;
import com.example.cloudfour.storeservice.domain.menu.dto.StockResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.Stock;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.MenuException;
import com.example.cloudfour.storeservice.domain.menu.exception.StockErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.StockException;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuRepository;
import com.example.cloudfour.storeservice.domain.menu.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockQueryService {
    private final StockRepository stockRepository;
    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public StockResponseDTO getMenuStock(UUID menuId){
        Menu menu = menuRepository.findById(menuId).orElseThrow(()->new MenuException(MenuErrorCode.NOT_FOUND));
        UUID stockId = menu.getStock().getId();
        Stock stock = stockRepository.findByIdWithOptimisticLock(stockId).orElseThrow(()->new StockException(StockErrorCode.NOT_FOUND));
        return StockConverter.toStockResposneDTO(stock);
    }
}