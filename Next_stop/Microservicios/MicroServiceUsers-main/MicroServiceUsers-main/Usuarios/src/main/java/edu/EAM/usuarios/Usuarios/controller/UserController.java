package edu.EAM.usuarios.Usuarios.controller;


import edu.EAM.usuarios.Usuarios.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import edu.EAM.usuarios.Usuarios.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con usuarios")
public class UserController {

    private final UserService service;

    @Autowired
    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Devuelve una lista con todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios encontrada")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @Operation(summary = "Buscar usuarios por nombre", description = "Filtra los usuarios por coincidencia en el nombre")
    @ApiResponse(responseCode = "200", description = "Usuarios encontrados")
    @GetMapping("/search")
    public ResponseEntity<List<User>> getUsersByName(@RequestParam String name) {
        return new ResponseEntity<>(service.findByName(name), HttpStatus.OK);
    }

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        User user = service.findById(id);
        return user != null ?
                new ResponseEntity<>(user, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Crear un nuevo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return new ResponseEntity<>(service.save(user), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un usuario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
        User existing = service.findById(id);
        if (existing != null) {
            user.setId(id);
            return new ResponseEntity<>(service.update(user), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Actualizar parcialmente un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUser(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        User updated = service.patch(id, updates);
        return updated != null ?
                new ResponseEntity<>(updated, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Eliminar un usuario por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        User existing = service.findById(id);
        if (existing != null) {
            service.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}