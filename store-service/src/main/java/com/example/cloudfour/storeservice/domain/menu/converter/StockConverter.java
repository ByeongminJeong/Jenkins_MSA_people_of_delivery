package com.example.cloudfour.storeservice.domain.menu.converter;

import com.example.cloudfour.storeservice.domain.menu.dto.StockResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.entity.Stock;

public class StockConverter {
    public static StockResponseDTO toStockResposneDTO(Stock stock){
        return StockResponseDTO.builder()
                .stockId(stock.getId())
                .menuId(stock.getMenu().getId())
                .quantity(stock.getQuantity())
                .version(stock.getVersion())
                .build();
    }
}
