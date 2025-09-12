package com.carpool.carpool.model.vehicle.type;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name="VehicleType")
public class VehicleType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; 

    private String name; 

    private String description;
}
