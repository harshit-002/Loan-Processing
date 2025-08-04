package com.abcbank.loan_processing.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;

    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String ssnNumber;

    @Embedded
    private Address address;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "homePhone", column = @Column(name = "home_phone")),
            @AttributeOverride(name = "officePhone", column = @Column(name = "office_phone")),
            @AttributeOverride(name = "mobile", column = @Column(name = "mobile")),
            @AttributeOverride(name = "email", column = @Column(name = "email"))
    })
    private ContactInfo contactInfo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Application> applications;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private EmploymentDetails employmentDetails;

}
