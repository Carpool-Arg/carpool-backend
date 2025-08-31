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

    @Column(nullable = false)
    private String domain;

    private String color;

    private String brand;

    private String model;

    private Integer year;

    private Integer availableSeats;

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