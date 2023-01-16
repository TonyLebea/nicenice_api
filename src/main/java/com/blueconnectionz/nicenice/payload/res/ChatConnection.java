package com.blueconnectionz.nicenice.payload.res;

import com.blueconnectionz.nicenice.model.Transactions;
import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatConnection {
    private String ownerID;
    private String driverID;
    private String token;
    private Transactions transactions;
}
