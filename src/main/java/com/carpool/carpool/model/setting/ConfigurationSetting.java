package com.carpool.carpool.model.setting;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configurations_settings")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
public class ConfigurationSetting {

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
