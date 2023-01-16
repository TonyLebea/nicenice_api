package com.blueconnectionz.nicenice.payload.res;

import com.blueconnectionz.nicenice.model.Transactions;
import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionInfo <T> {
    private T custom;
    private Transactions transaction;
}
