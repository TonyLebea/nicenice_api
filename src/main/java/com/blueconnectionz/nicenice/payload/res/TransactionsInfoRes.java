package com.blueconnectionz.nicenice.payload.res;

import com.blueconnectionz.nicenice.model.Transactions;
import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionsInfoRes {
    List<Transactions> transactions;
    private int balance;
}
