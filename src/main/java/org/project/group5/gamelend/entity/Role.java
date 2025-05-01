package org.project.group5.gamelend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role") // nombre en la base de datos
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"users"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrementable
    @Column(name = "id_role") // nombre en la base de datos
    private Long idRole;

    @Column(nullable = false, unique = true)
    private String name;
}