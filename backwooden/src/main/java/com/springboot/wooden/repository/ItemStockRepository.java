package com.springboot.wooden.repository;

import com.springboot.wooden.domain.ItemStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemStockRepository extends JpaRepository<ItemStock, Long> {

    @EntityGraph(attributePaths = "item")
    @Query("select s from ItemStock s")
    List<ItemStock> findAllWithItem();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ItemStock s where s.item.itemNo = :itemNo")
    Optional<ItemStock> findByItemNoForUpdate(@Param("itemNo") Long itemNo);
}
