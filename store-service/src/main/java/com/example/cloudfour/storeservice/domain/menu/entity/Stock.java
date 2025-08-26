package com.example.cloudfour.storeservice.domain.menu.entity;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.exception.StockErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.StockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_stock")
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {
    @Id
    @GeneratedValue
    private UUID id;

    @Builder.Default
    private Long quantity = (long) Integer.MAX_VALUE;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(name = "syncStatus", nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.UPDATED_PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menuid", nullable = false)
    private Menu menu;

    public static class StockBuilder {
        private StockBuilder id(UUID id) {
            throw new StockException(StockErrorCode.CREATE_FAILED);
        }
    }

    public void setMenu(Menu menu){
        this.menu = menu;
        menu.setStock(this);
    }

    public void setSyncStatus(SyncStatus syncStatus){
        this.syncStatus = syncStatus;
    }

    public void decrease(Long quantity){
        if(quantity<0)quantity = -quantity;
        if(this.quantity - quantity < 0){
            throw new StockException(StockErrorCode.MINUS_FAILED);
        }
        this.quantity-=quantity;
        this.syncStatus = SyncStatus.UPDATED_PENDING;
    }

    public void increase(Long quantity){
        this.quantity+=quantity;
        this.syncStatus = SyncStatus.UPDATED_PENDING;
    }
}
