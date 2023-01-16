package com.blueconnectionz.nicenice.model;

import com.blueconnectionz.nicenice.utils.ListToStringConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;


@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "cars"
)
@ToString
@NoArgsConstructor
public class Car extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uniqueCarImgID;
    private String make;
    private String model;
    private String year;
    private String city;
    private String weeklyTarget;
    private boolean depositRequired;
    private boolean hasInsurance;
    private boolean hasTracker;
    private boolean activeOnHailingPlatforms;
    private boolean approved;
    private boolean available;
    private int numConnections;
    private int age;
    private int views;
    @Lob
    private String description;
    private Long ownerID;

    @Convert(converter = ListToStringConverter.class)
    @JsonIgnore
    private List<String> usersWhoViewed;

    public Car(String uniqueCarImgID, String make, String model,
               String year, String city, String weeklyTarget, String description,  boolean depositRequired,
               boolean hasInsurance, boolean hasTracker, boolean activeOnHailingPlatforms,
               Long ownerID
               ) {

        this.uniqueCarImgID = uniqueCarImgID;
        this.make = make;
        this.model = model;
        this.year = year;
        this.city = city;
        this.weeklyTarget = weeklyTarget;
        this.depositRequired = depositRequired;
        this.hasInsurance = hasInsurance;
        this.hasTracker = hasTracker;
        this.activeOnHailingPlatforms = activeOnHailingPlatforms;
        this.approved = false;
        this.available = true;
        this.views = 0;
        usersWhoViewed = new ArrayList<>();
        this.numConnections = 0;
        this.age = 0;
        this.ownerID = ownerID;
        this.description = description;
    }


}





