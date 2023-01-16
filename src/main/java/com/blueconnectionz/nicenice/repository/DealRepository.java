package com.blueconnectionz.nicenice.repository;

import com.blueconnectionz.nicenice.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DealRepository extends JpaRepository<Deal,Long> {
    List<Deal> findByCarID(Long carID);
    List<Deal> findByDriverID(Long driverID);
}
