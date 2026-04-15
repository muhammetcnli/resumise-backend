package com.resumise.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "user")
    private List<AuthAccount> authAccounts = new ArrayList<>();

    @OneToOne(mappedBy = "user")
    private Credential credential;

    @OneToMany(mappedBy = "user")
    private List<Cv> cvs = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<JobPosting> jobPostings = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<AnalysisRequest> analysisRequests = new ArrayList<>();
}
