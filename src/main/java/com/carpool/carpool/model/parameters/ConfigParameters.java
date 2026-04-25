package com.carpool.carpool.model.parameters;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "config_parameters")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ConfigParameters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_name", nullable = false, unique = true)
    private String keyName;

    @Column(name = "key_value", nullable = false)
    private String keyValue;

    @Column(name = "description")
    private String description;
}
