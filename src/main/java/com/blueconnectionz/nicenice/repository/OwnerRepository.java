package com.blueconnectionz.nicenice.repository;

import com.blueconnectionz.nicenice.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends JpaRepository<Owner,Long> {
    Boolean existsByPhoneNumber(String phoneNumber);
    List<Owner> findByApproved(boolean approved);
    Optional<Owner> findByUserId(Long userId);
}