package com.abcbank.loan_processing.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
