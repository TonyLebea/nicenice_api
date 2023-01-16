package com.blueconnectionz.nicenice.model;

import lombok.*;

import javax.persistence.*;

@ToString
@NoArgsConstructor
@Getter
@Setter
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "transactions")
public class Transactions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int amount;
    private Long userID;
    private boolean isAdminTopUp;

    public Transactions(int amount, Long userID,boolean isAdminTopUp ) {
        this.amount = amount;
        this.userID = userID;
        this.isAdminTopUp = isAdminTopUp;
    }
}
