package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class StockCommandRepositoryImpl implements StockCommandRepository{

    private final MongoTemplate mongoTemplate;

    @Override
    public void updateStockByMenuId(UUID menuId, Long quantity) {
        Query query = new Query(Criteria.where("menus.id").is(menuId));
        Update update = new Update()
                .set("menus.$.stock.quantity", quantity);

        mongoTemplate.updateFirst(query, update, StoreDocument.class);
    }
}
