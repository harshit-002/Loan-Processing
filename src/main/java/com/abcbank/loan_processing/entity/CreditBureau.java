package com.abcbank.loan_processing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CreditBureau {
    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "delinq_2yrs")
    private Integer delinq2yrs;

    @Column(name = "inq_last_6mths")
    private Integer inqLast6mths;

    @Column(name = "mths_since_last_delinq")
    private Integer mthsSinceLastDelinq;

    @Column(name = "mths_since_last_record")
    private Integer mthsSinceLastRecord;

    @Column(name = "open_acc")
    private Integer openAcc;

    @Column(name = "pub_rec")
    private Integer pubRec;

    @Column(name = "revol_bal")
    private Long revolBal;

    @Column(name = "revol_util")
    private Double revolUtil;

    @Column(name = "total_acc")
    private Integer totalAcc;

    @Column(name = "earliest_cr_line")
    private LocalDate earliestCrLine;
}
