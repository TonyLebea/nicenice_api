package com.blueconnectionz.nicenice.repository;

import com.blueconnectionz.nicenice.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car,Long> {
    List<Car> findByOwnerID(Long ownerID);
}
