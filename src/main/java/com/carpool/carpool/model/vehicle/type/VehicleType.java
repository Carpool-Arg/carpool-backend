package com.carpool.carpool.model.vehicle.type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name="VehicleType")
public class VehicleType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; 

    @NotBlank(message = "El nombre no puede quedar en blanco.")
    @Size(min = 1, message = "El nombre debe tener al menos un caracter")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "El nombre debe contener sólo letras y espacios.")
    @Column(unique = true)
    private String name; 

    @NotBlank(message = "La descripción no puede quedar en blanco.")
    @Size(min = 1, message = "La descripción debe tener al menos un caracter.")
    private String description;
    
}
