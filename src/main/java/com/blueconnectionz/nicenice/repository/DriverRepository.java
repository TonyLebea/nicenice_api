package com.blueconnectionz.nicenice.repository;

import com.blueconnectionz.nicenice.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {
    Boolean existsByPhoneNumber(String phoneNumber);
    List<Driver> findByApproved(boolean approved);
    Optional<Driver> findByUserId(Long userId);
}
