package com.abcbank.loan_processing.common;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;

    public void updateFrom(Address address) {
        this.addressLine1 = address.addressLine1;
        this.addressLine2 = address.addressLine2;
        this.city = address.city;
        this.state = address.state;
        this.postalCode = address.postalCode;
    }
}
