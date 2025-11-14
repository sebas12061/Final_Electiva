package edu.EAM.admin.Admin.controller;

import edu.EAM.admin.Admin.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.EAM.admin.Admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
@Tag(name = "Administradores", description = "Operaciones relacionadas con administradores")
public class AdminController {

    private final AdminService service;

    @Autowired
    public AdminController(AdminService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener todos los administradores", description = "Devuelve una lista con todos los administradores registrados")
    @ApiResponse(responseCode = "200", description = "Lista de administradores encontrada")
    @GetMapping
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @Operation(summary = "Buscar administradores por nombre", description = "Filtra los administradores por coincidencia en el nombre")
    @ApiResponse(responseCode = "200", description = "Administradores encontrados")
    @GetMapping("/search")
    public ResponseEntity<List<Admin>> getAdminsByName(@RequestParam String name) {
        return new ResponseEntity<>(service.findByName(name), HttpStatus.OK);
    }

    @Operation(summary = "Obtener administrador por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrador encontrado"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String id) {
        Admin admin = service.findById(id);
        return admin != null ?
                new ResponseEntity<>(admin, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Crear un nuevo administrador")
    @ApiResponse(responseCode = "201", description = "Administrador creado exitosamente")
    @PostMapping
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        return new ResponseEntity<>(service.save(admin), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un administrador existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrador actualizado"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable String id, @RequestBody Admin admin) {
        Admin existing = service.findById(id);
        if (existing != null) {
            admin.setId(id);
            return new ResponseEntity<>(service.update(admin), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Actualizar parcialmente un administrador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Administrador actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Admin> patchAdmin(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        Admin updated = service.patch(id, updates);
        return updated != null ?
                new ResponseEntity<>(updated, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Eliminar un administrador por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Administrador eliminado"),
            @ApiResponse(responseCode = "404", description = "Administrador no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String id) {
        Admin existing = service.findById(id);
        if (existing != null) {
            service.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}