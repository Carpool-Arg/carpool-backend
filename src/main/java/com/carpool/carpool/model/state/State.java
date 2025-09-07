package com.carpool.carpool.model.state;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="state")
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="scope", nullable = false)
    private String scope;
}
