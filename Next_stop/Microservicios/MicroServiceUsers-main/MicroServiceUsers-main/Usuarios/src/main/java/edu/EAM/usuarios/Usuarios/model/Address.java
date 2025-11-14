package edu.EAM.usuarios.Usuarios.model;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor  // Necesario para JPA
@Embeddable
public class Address {
    private String street;
    private String number;
    private String neighborhood;  // barrio
    private String city;
    private String postalCode;
}