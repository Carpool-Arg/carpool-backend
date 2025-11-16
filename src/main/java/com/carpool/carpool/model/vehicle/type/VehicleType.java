package com.carpool.carpool.model.vehicle.type;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
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
