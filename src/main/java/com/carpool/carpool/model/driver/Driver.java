package com.carpool.carpool.model.driver;

import java.time.LocalDate;

import com.carpool.carpool.model.province.city.City;
import com.carpool.carpool.model.user.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

    @DecimalMax(value = "5.0", message = "La calificación máxima es 5.0.")
    @DecimalMin(value = "0.0", message = "La calificación mínima es 0.0.")
    private Double rating;

    @Column(nullable = false, length = 2)
    private String licenseClass;

    @Column(nullable = false)
    private LocalDate licenseExpirationDate;

    @Column(nullable = false)
    private String addressStreet;

    @Column(nullable = false)
    private String addressNumber;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city; 

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;
}
