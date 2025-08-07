package com.abcbank.loan_processing.common;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ContactInfo {
    private String homePhone;
    private String officePhone;
    private String mobile;
    private String email;

    public void updateFrom(ContactInfo contactInfo) {
        this.homePhone = contactInfo.homePhone;
        this.officePhone = contactInfo.officePhone;
        this.mobile = contactInfo.mobile;
        this.email = contactInfo.email;    }
}
