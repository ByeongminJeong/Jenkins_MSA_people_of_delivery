package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuCommandRepository {
    public void deleteAllByMenuIdIn(List<UUID> menuIds);
    public void deleteAllByMenuOptionIdIn(List<UUID> menuOptionIds);
    public void createMenuByStoreId(List<UUID> storeIds, Map<UUID, List<Menu>> menusByStoreId);
    public void createMenuOptionByMenuId(List<UUID> menuIds, Map<UUID, List<MenuOption>> menuOptionsByMenuId);
}
