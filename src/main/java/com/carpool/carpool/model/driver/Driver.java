package com.carpool.carpool.model.driver;

import java.time.LocalDate;

import com.carpool.carpool.model.licenseClass.LicenseClass;
import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@ToString
@Entity
@Table(name = "driver")
public class Driver {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double rating;

    private LocalDate licenseExpirationDate;

    private String addressStreet;

    private String addressNumber;

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
