package com.abcbank.loan_processing.entity;
import com.abcbank.loan_processing.common.Address;
import com.abcbank.loan_processing.common.ContactInfo;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nonnull
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;

    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String ssnNumber;
    private Integer score;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LoanInfo> loanInfos;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private EmploymentDetails employmentDetails;

    public void updateFrom(User incomingUser) {
        this.firstName = incomingUser.firstName;
        this.middleName = incomingUser.middleName;
        this.lastName = incomingUser.lastName;
        this.dateOfBirth = incomingUser.dateOfBirth;
        this.maritalStatus = incomingUser.maritalStatus;

        if (this.contactInfo == null) {
            this.contactInfo = new ContactInfo();
        }
        this.contactInfo.updateFrom(incomingUser.getContactInfo());

        if (this.address == null) {
            this.address = new Address();
        }
        this.address.updateFrom(incomingUser.getAddress());
    }
}
