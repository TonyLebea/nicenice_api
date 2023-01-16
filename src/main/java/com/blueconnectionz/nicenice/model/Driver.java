package com.blueconnectionz.nicenice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "phoneNumber")
        }
)
@ToString
@NoArgsConstructor
public class Driver extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String fullName;
    @NotBlank
    @Size(max = 10)
    String phoneNumber;
    @NotBlank
    private String location;
    private boolean approved;
    private boolean reported;
    private String uniqueDocumentId;
    private int creditBalance;
    private String platform;
    private String reference1;
    private String reference2;

    private boolean online;
    // The Driver is going to a user
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;
    private int views;

    public Driver(@NotBlank String fullName, @NotBlank @Size(max = 10) String phoneNumber,
                  @NotBlank String location, boolean approved, boolean suspended, String uniqueDocumentId,
                  int creditBalance, String platform, String reference1, String reference2, User user) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.approved = approved;
        this.reported = suspended;
        this.uniqueDocumentId = uniqueDocumentId;
        this.creditBalance = creditBalance;
        this.platform = platform;
        this.reference1 = reference1;
        this.reference2 = reference2;
        this.user = user;
        views = 0;
    }
}
