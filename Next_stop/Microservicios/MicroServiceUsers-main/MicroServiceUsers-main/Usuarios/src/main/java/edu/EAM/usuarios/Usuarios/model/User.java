package edu.EAM.usuarios.Usuarios.model;
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
@NoArgsConstructor  // Necesario para JPA
@Entity
@Table(name = "users")  // Nombre de la tabla en DB
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // Genera UUID automáticamente
    private String id;
    
    private String name;
    private String gender;
    
    @Column(name = "email")  // Mapea a columna 'email'
    private String Email;
    
    @Column(name = "phone_number")  // Mapea a columna 'phone_number'
    private String PhoneNumber;
    
    @Embedded  // Embebe Address en User
    private Address address;
}