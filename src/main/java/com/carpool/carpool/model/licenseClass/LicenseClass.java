package com.carpool.carpool.model.licenseClass;

import com.carpool.carpool.model.driver.Driver;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name="license_Class")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LicenseClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2, unique = true)
    private String name;

    private String description;

    @OneToMany(mappedBy = "licenseClass")
    private List<Driver> drivers;
}
