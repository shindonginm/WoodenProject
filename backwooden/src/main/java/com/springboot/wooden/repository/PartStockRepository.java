package com.springboot.wooden.repository;

import com.springboot.wooden.domain.PartStock;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface PartStockRepository extends JpaRepository<PartStock, Long> {

    @EntityGraph(attributePaths = "part")
    Optional<PartStock> findByPart_PartNo(Long partNo);

}
