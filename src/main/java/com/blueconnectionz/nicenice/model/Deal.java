package com.blueconnectionz.nicenice.model;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "deals"
)
@ToString
@NoArgsConstructor
public class Deal  extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerID;
    private Long driverID;
    private Long carID;
    private float amount;
    // Channel ID

    public Deal(Long ownerID, Long driverID,Long carID,float amount) {
        this.ownerID = ownerID;
        this.driverID = driverID;
        this.carID = carID;
        this.amount = amount;
    }
}
