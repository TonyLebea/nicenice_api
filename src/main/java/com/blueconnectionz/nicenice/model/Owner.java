package com.blueconnectionz.nicenice.model;

import com.blueconnectionz.nicenice.utils.ListToStringConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Table(
        name = "owners",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "phoneNumber")
        }
)
@ToString
@NoArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Owner extends BaseEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @NotBlank
    @Size(max = 10)
    private String phoneNumber;

    private boolean approved;

    private String uniqueDocumentId;
    private boolean reported;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;
    private int creditBalance;

    @Convert(converter = ListToStringConverter.class)
    @JsonIgnore
    private List<String> driversConnectedWith;

    public Owner(@NotBlank @Size(max = 10) String phoneNumber, boolean approved, boolean reported, String uniqueDocumentId, User user) {
        this.phoneNumber = phoneNumber;
        this.approved = approved;
        this.reported = reported;
        this.user = user;
        this.uniqueDocumentId = uniqueDocumentId;
        this.creditBalance = 0;
    }
}
