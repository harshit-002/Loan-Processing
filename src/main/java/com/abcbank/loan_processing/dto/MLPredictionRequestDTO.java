package com.abcbank.loan_processing.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MLPredictionRequestDTO {
    private BigDecimal loanAmnt;
    private String purpose;
    private String desc;
    private Integer empLength;
    private BigDecimal annualInc;

    @JsonProperty("delinq_2yrs")
    private Integer delinq2yrs;

    @JsonProperty("inq_last_6mths")
    private Integer inqLast6mths;

    private Integer mthsSinceLastDelinq;
    private Integer mthsSinceLastRecord;
    private Integer openAcc;
    private Integer pubRec;
    private Long revolBal;
    private Double revolUtil;
    private Integer totalAcc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate earliestCrLine;
}
