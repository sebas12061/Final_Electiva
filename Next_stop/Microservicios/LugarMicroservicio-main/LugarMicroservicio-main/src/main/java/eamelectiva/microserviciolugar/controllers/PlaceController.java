package eamelectiva.microserviciolugar.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eamelectiva.microserviciolugar.model.Place;
import eamelectiva.microserviciolugar.model.PlaceStatus;
import eamelectiva.microserviciolugar.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/places")
@Tag(name = "Lugares", description = "Operaciones relacionadas con los lugares")
@CrossOrigin  // De tu compañera
public class PlaceController {

    private final PlaceService service;

    @Autowired
    public PlaceController(PlaceService service) {
        this.service = service;
    }

    // Obtener todos los lugares
    @Operation(summary = "Obtener todos los lugares", description = "Devuelve una lista con todos los lugares registrados")
    @ApiResponse(responseCode = "200", description = "Lista de lugares encontrada")
    @GetMapping
    public ResponseEntity<List<Place>> getAllPlaces() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    // Obtener lugar por ID
    @Operation(summary = "Obtener lugar por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar encontrado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Place> getPlaceById(@PathVariable Long id) {
        Place place = service.findById(id);
        return place != null ?
                new ResponseEntity<>(place, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Crear un nuevo lugar
    @Operation(summary = "Crear un nuevo lugar")
    @ApiResponse(responseCode = "201", description = "Lugar creado exitosamente")
    @PostMapping
    public ResponseEntity<Place> createPlace(@RequestBody Place place) {
        // Cuando se crea un lugar vía API, también creamos la solicitud en microservicio de solicitudes
        Place saved = service.saveWithSolicitud(place);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // Actualizar un lugar existente
    @Operation(summary = "Actualizar un lugar existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Place> updatePlace(@PathVariable Long id, @RequestBody Place place) {
        Place existing = service.findById(id);
        if (existing != null) {
            place.setId(id);
            return new ResponseEntity<>(service.update(place), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Actualizar parcialmente un lugar
    @Operation(summary = "Actualizar parcialmente un lugar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar actualizado parcialmente"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Place> patchPlace(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Place updated = service.patch(id, updates);
        return updated != null ?
                new ResponseEntity<>(updated, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Eliminar un lugar por ID
    @Operation(summary = "Eliminar un lugar por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Lugar eliminado"),
            @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlace(@PathVariable Long id) {
        Place existing = service.findById(id);
        if (existing != null) {
            service.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Aceptar un lugar
    @Operation(summary = "Aceptar un lugar", description = "Cambia el estado de un lugar a ACEPTADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar aceptado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se pudo aceptar el lugar (ya estaba aceptado o no existe)")
    })
    @PostMapping("/{placeId}/aceptada")
    public ResponseEntity<String> acceptPlace(@PathVariable Long placeId) {
        if (service.acceptPlace(placeId)) {
            return new ResponseEntity<>("Lugar aceptado.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al aceptar el lugar.", HttpStatus.BAD_REQUEST);
        }
    }

    // Rechazar un lugar
    @Operation(summary = "Rechazar un lugar", description = "Cambia el estado de un lugar a RECHAZADO")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lugar rechazado exitosamente"),
            @ApiResponse(responseCode = "400", description = "No se pudo rechazar el lugar (ya estaba rechazado o no existe)")
    })
    @PostMapping("/{placeId}/rechazada")
    public ResponseEntity<String> rejectPlace(@PathVariable Long placeId) {
        if (service.rejectPlace(placeId)) {
            return new ResponseEntity<>("Lugar rechazado.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error al rechazar el lugar.", HttpStatus.BAD_REQUEST);
        }
    }

    // Mostrar los lugares pendientes
    @Operation(summary = "Mostrar los lugares pendientes", description = "Devuelve una lista con los lugares que están en estado PENDING")
    @ApiResponse(responseCode = "200", description = "Lista de lugares pendientes encontrada")
    @ApiResponse(responseCode = "404", description = "No se encontraron lugares pendientes")
    @GetMapping("/pendientes")  // Cambia de "/pending" a "/pendientes"
    public ResponseEntity<List<Place>> getPendingPlaces() {
        List<Place> allPlaces = service.findAll();
        List<Place> pendingPlaces = allPlaces.stream()
                .filter(place -> place.getStatus() == PlaceStatus.pendiente)  // Asegúrate de que sea pendiete
                .toList();
        return new ResponseEntity<>(pendingPlaces, HttpStatus.OK);
    }


    // Mostrar los lugares aceptados
    @Operation(summary = "Mostrar los lugares aceptados", description = "Devuelve una lista con los lugares que están en estado ACEPTADO")
    @ApiResponse(responseCode = "200", description = "Lista de lugares aceptados encontrada")
    @ApiResponse(responseCode = "404", description = "No se encontraron lugares aceptados")
    @GetMapping("/aceptada")
    public ResponseEntity<List<Place>> getAcceptedPlaces() {
        List<Place> allPlaces = service.findAll();
        List<Place> acceptedPlaces = allPlaces.stream()
                .filter(place -> place.getStatus() == PlaceStatus.aceptada)
                .toList();
        return !acceptedPlaces.isEmpty() ?
                new ResponseEntity<>(acceptedPlaces, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Mostrar los lugares rechazados
    @Operation(summary = "Mostrar los lugares rechazados", description = "Devuelve una lista con los lugares que están en estado RECHAZADO")
    @ApiResponse(responseCode = "200", description = "Lista de lugares rechazados encontrada")
    @ApiResponse(responseCode = "404", description = "No se encontraron lugares rechazados")
    @GetMapping("/rechazada")
    public ResponseEntity<List<Place>> getRejectedPlaces() {
        List<Place> allPlaces = service.findAll();
        List<Place> rejectedPlaces = allPlaces.stream()
                .filter(place -> place.getStatus() == PlaceStatus.rechazada)
                .toList();
        return !rejectedPlaces.isEmpty() ?
                new ResponseEntity<>(rejectedPlaces, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Obtener lugares con solicitudes pendientes (nuestra adición)
    @GetMapping("/con-solicitudes")
    public ResponseEntity<Map<String, Object>> getPlacesWithSolicitudes() {
        List<Place> places = service.findAll();
        List<Map<String, Object>> solicitudes = service.obtenerSolicitudesPendientes();
        Map<String, Object> response = Map.of(
            "lugares", places,
            "solicitudes_pendientes", solicitudes
        );
        return ResponseEntity.ok(response);
    }
}