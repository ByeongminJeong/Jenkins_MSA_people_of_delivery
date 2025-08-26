package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.collection.converter.DocumentConverter;
import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;
import com.mongodb.client.result.UpdateResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Repository
@RequiredArgsConstructor
@Transactional
public class MenuCommandRepositoryImpl implements MenuCommandRepository{

    private final MongoTemplate mongoTemplate;

    @Override
    public void deleteAllByMenuIdIn(List<UUID> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        Query query = new Query(Criteria.where("menus.id").in(menuIds));

        Update update = new Update().pull("menus", Query.query(Criteria.where("_id").in(menuIds)));

        mongoTemplate.updateMulti(query, update, StoreDocument.class);
    }

    @Override
    public void deleteAllByMenuOptionIdIn(List<UUID> menuOptionIds) {
        if (menuOptionIds == null || menuOptionIds.isEmpty()) {
            return;
        }

        Query query = new Query();
        Update update = new Update().pull("menus.$[].menuOptions",
                Query.query(Criteria.where("id").in(menuOptionIds)));

        mongoTemplate.updateMulti(query, update, StoreDocument.class);
    }

    @Override
    public void createMenuByStoreId(List<UUID> storeIds, Map<UUID, List<Menu>> menusByStoreId) {
        Query query = Query.query(Criteria.where("storeId").in(storeIds));
        List<StoreDocument> storeDocuments = mongoTemplate.find(query, StoreDocument.class);

        for(StoreDocument storeDocument:storeDocuments){
            UUID storeId = storeDocument.getStoreId();
            List<StoreDocument.Menu> currentMenus = storeDocument.getMenus();
            List<StoreDocument.Menu> menusToAdd = menusByStoreId.get(storeId).stream().map(
                    DocumentConverter::toStoreDocumentMenu).toList();
            currentMenus.addAll(menusToAdd);
            Query createquery = Query.query(Criteria.where("storeId").is(storeDocument.getStoreId()));
            Update update = Update.update("menus", currentMenus);
            mongoTemplate.updateFirst(createquery, update, StoreDocument.class);
        }
    }

    @Override
    public void createMenuOptionByMenuId(List<UUID> menuIds, Map<UUID, List<MenuOption>> menuOptionsByMenuId) {
        Query query = Query.query(Criteria.where("menus.id").in(menuIds));
        List<StoreDocument> storeDocuments = mongoTemplate.find(query, StoreDocument.class);
        for(StoreDocument storeDocument: storeDocuments){
            for(int i=0; i<storeDocument.getMenus().size(); i++){
                StoreDocument.Menu menu = storeDocument.getMenus().get(i);
                UUID menuId = menu.getId();
                if(menuOptionsByMenuId.containsKey(menuId)){
                    List<MenuOption> menuOptionsToAdd = menuOptionsByMenuId.get(menuId);
                    List<StoreDocument.MenuOption> currentMenuOptions = menu.getMenuOptions();
                    List<StoreDocument.MenuOption> menuOptionsToAddConverted = menuOptionsToAdd.stream()
                            .map(DocumentConverter::toStoreDocumentMenuOption)
                            .toList();
                    currentMenuOptions.addAll(menuOptionsToAddConverted);

                    Query updateQuery = Query.query(Criteria.where("storeId").is(storeDocument.getStoreId())
                            .and("menus.id").is(menuId));
                    Update update = Update.update("menus.$.menuOptions", currentMenuOptions);

                    UpdateResult result = mongoTemplate.updateFirst(updateQuery, update, StoreDocument.class);
                }
            }
        }
    }
}
