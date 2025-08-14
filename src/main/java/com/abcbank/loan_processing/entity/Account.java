package com.abcbank.loan_processing.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @Nonnull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Nonnull
    @Column(unique = true, nullable = false)
    private String username;

    @Nonnull
    @Column(unique = true, nullable = false)
    private String ssnNumber;

    @Nonnull
    @Column(unique = true, nullable = false)
    private String password;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;
}
