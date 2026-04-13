package com.carpool.carpool.model.driver;

import java.io.Serializable;
import java.time.LocalDate;

import com.carpool.carpool.enums.licenseStatus.LicenseStatusEnum;
import com.carpool.carpool.model.licenseClass.LicenseClass;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.user.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
@Entity
@Table(name = "driver")
public class Driver implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double rating;

    private LocalDate licenseExpirationDate;

    private String addressStreet;

    private String addressNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_status", nullable = false)
    private LicenseStatusEnum licenseStatus;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "license_class_id", nullable = false)
    private LicenseClass licenseClass;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
}