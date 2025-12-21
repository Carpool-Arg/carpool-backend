package com.carpool.carpool.model.province.city;

import com.carpool.carpool.model.province.Province;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name="city")
public class City {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre de la ciudad no puede ser nulo")
    private String name;

    @Column(name="zip_code", nullable = false)
    private int zipCode;

    @Column(name="latitude", nullable = false)
    private double latitude;

    @Column(name="longitude", nullable = false)
    private double longitude;

    @ManyToOne
    @JoinColumn( name = "province_id", referencedColumnName = "id", nullable = false)
    private Province province;

}
