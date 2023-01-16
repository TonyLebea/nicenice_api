package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LatestTransactionsRes {
    Long id;
    String transaction;
    LocalDateTime dateTime;
    int amount;
}
