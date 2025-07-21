package com.carpool.carpool.model.vehicle;

import java.time.LocalDateTime;

import com.carpool.carpool.model.driver.Driver;
import com.carpool.carpool.model.vehicle.type.VehicleType;
import com.carpool.carpool.validators.yearNotInFuture.YearNotInFuture;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La patente no puede estar en blanco.")
    @Size(min = 6, max = 7, message = "La patente debe tener entre 6 y 7 caracteres.") 
    @Column(name = "domain", unique = true, nullable = false)
    private String domain;

    @NotBlank(message = "El color del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El color debe contener sólo letras y espacios.")
    private String color;

    @NotBlank(message = "La marca del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "La marca debe contener sólo letras, números y espacios.") 
    private String brand;

    @NotBlank(message = "El modelo del vehiculo no puede estar en blanco.")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "El modelo debe contener sólo letras, números y espacios.")
    private String model;

    @NotNull(message = "El año del vehiculo no puede estar en blanco.")
    @Min(value = 1900, message = "El año debe ser posterior a 1900.") 
    @YearNotInFuture
    private Integer year;

    @NotNull(message = "La cantidad de asientos disponibles no puede estar en blanco.")
    @Min(value = 1, message = "La cantidad de asientos disponibles debe ser al menos 1.")
    private Integer availableSeats; 

    /*
     * PositiveOrZero permite que la capacidad de equipaje sea cero, lo cual es válido si el vehículo no tiene espacio para equipaje.
     */
    @NotNull(message = "La cantidad de equipaje disponible no puede estar en blanco.")
    @PositiveOrZero(message = "La cantidad de equipaje disponible debe ser un número positivo o cero.")
    private Float luggageCapacity;

    @ManyToOne
    @JoinColumn(name = "vehicle_type_id", nullable = false) // Eliminado unique = true
    private VehicleType vehicleType;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false) // Eliminado unique = true
    private Driver driver;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deleted_by;

    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updated_at = LocalDateTime.now();
    }

    public boolean isEnabled(){
        return deletedAt == null;
    }
}