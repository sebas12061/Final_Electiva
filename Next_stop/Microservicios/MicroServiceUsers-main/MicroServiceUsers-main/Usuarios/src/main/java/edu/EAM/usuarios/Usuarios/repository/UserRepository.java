package edu.EAM.usuarios.Usuarios.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.EAM.usuarios.Usuarios.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {  // <Entidad, Tipo de ID>
    
    // Método personalizado para buscar por nombre (contiene)
    List<User> findByNameContainingIgnoreCase(String name);
    
    // Otros métodos CRUD ya están en JpaRepository (save, findById, findAll, deleteById, etc.)

}