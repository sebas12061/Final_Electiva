package edu.EAM.admin.Admin.repository;

import edu.EAM.admin.Admin.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {

    // Buscar por coincidencia parcial en el nombre (ignora mayúsculas/minúsculas)
    List<Admin> findByNameContainingIgnoreCase(String name);
}
