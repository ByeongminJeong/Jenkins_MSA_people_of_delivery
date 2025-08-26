package com.example.cloudfour.storeservice.domain.collection.repository.command;

import java.util.UUID;

public interface StockCommandRepository {
    void updateStockByMenuId(UUID menuId, Long quantity);
}
