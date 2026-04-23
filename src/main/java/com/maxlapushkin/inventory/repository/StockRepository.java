package com.maxlapushkin.inventory.repository;

import com.maxlapushkin.inventory.model.StockItem;
import com.maxlapushkin.inventory.model.StockKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends JpaRepository<StockItem, StockKey> {
    List<StockItem> findByIdProductId(Long productId);
    List<StockItem> findByIdWarehouseId(Long warehouseId);
    List<StockItem> findByIdProductIdAndIdWarehouseId(Long productId, Long warehouseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update StockItem s
            set s.available = s.available - :qty,
                s.reserved = s.reserved + :qty
            where s.id.productId = :productId
              and s.id.warehouseId = :warehouseId
              and s.available >= :qty
            """)
    int reserveStock(@Param("productId") Long productId,
                     @Param("warehouseId") Long warehouseId,
                     @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update StockItem s
            set s.available = s.available + :qty,
                s.reserved = s.reserved - :qty
            where s.id.productId = :productId
              and s.id.warehouseId = :warehouseId
              and s.reserved >= :qty
            """)
    int releaseStock(@Param("productId") Long productId,
                     @Param("warehouseId") Long warehouseId,
                     @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update StockItem s
            set s.reserved = s.reserved - :qty
            where s.id.productId = :productId
              and s.id.warehouseId = :warehouseId
              and s.reserved >= :qty
            """)
    int fulfillStock(@Param("productId") Long productId,
                     @Param("warehouseId") Long warehouseId,
                     @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update StockItem s
            set s.available = s.available + :qty
            where s.id.productId = :productId
              and s.id.warehouseId = :warehouseId
            """)
    int increaseAvailable(@Param("productId") Long productId,
                          @Param("warehouseId") Long warehouseId,
                          @Param("qty") int qty);
}
