package com.abcbank.loan_processing.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class ContactInfo {
    private String homePhone;
    private String officePhone;
    private String mobile;
    private String email;
}
