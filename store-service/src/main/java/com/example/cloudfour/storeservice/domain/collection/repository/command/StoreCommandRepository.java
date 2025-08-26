package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface StoreCommandRepository extends MongoRepository<StoreDocument, UUID> {
    void deleteAllByStoreIdIn(List<UUID> ids);
}
