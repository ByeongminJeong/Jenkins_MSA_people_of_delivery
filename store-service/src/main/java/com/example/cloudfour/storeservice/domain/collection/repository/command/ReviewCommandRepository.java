package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.collection.document.ReviewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ReviewCommandRepository extends MongoRepository<ReviewDocument, UUID>,ReviewCustomCommandRepository {

}
