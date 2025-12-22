package com.carpool.carpool.model.state;

import com.carpool.carpool.enums.state.ScopeEnum;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="state")
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name="scope", nullable = false)
    private ScopeEnum scope;
}
