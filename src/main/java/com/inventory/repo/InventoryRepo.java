package com.inventory.repo;

import com.inventory.entity.InventoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepo extends JpaRepository<InventoryEntity,Long> {
    Optional<InventoryEntity> findByProduct(String product);
    @Transactional
    @Modifying
    @Query(value = "update inventory i set i.quantity=:quantity where i.product=:product",nativeQuery = true)
    public int updateQuantityByProductName(@Param("quantity") double quantity, @Param("product") String product);

    @Query(value = "select quantity from inventory where product=:product",nativeQuery = true)
    public double getQuantityByProductName(@Param("product") String product);

    @Query(value = "select price from inventory where product=:product",nativeQuery = true)
    public double getPriceByProductName(@Param("product") String product);
}
