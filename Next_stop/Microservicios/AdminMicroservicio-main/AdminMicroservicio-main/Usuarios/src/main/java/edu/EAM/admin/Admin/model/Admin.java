package edu.EAM.admin.Admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "admins") // Nombre de la tabla en la base de datos
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Genera IDs automáticos tipo UUID
    private String id;

    @Column(nullable = false)
    private String name;

    private String gender;

    @Column(unique = true) // Email debe ser único
    private String email;

    private String phoneNumber;

    @Embedded  // Address se guardará como parte de Admin en la misma tabla
    private Address address;
}
