package com.blueconnectionz.nicenice.repository;

import com.blueconnectionz.nicenice.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions,Long> {
    List<Transactions> findByUserID(Long userID);
}
