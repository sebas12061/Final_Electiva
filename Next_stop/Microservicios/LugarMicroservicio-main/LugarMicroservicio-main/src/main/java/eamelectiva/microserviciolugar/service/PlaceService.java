package eamelectiva.microserviciolugar.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;  // Import agregado para comunicación

import eamelectiva.microserviciolugar.model.Place;
import eamelectiva.microserviciolugar.model.PlaceStatus;
import eamelectiva.microserviciolugar.repository.PlaceRepository;

@Service
public class PlaceService {

    private final PlaceRepository repository;
    private final RestTemplate restTemplate;  // Agregado para comunicación

    @Autowired
    public PlaceService(PlaceRepository repository, RestTemplate restTemplate) {  // Inyección agregada
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    // Crear un nuevo lugar (de tu compañera, con default PENDING)
    public Place save(Place place) {
        if (place.getStatus() == null) {
            place.setStatus(PlaceStatus.pendiente);
        }
        return repository.save(place);
    }

    // Buscar lugar por ID
    public Place findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    // Obtener todos los lugares
    public List<Place> findAll() {
        return repository.findAll();
    }

    // Actualizar un lugar existente
    public Place update(Place place) {
        return repository.save(place);
    }

    // Actualizar parcialmente un lugar
    public Place patch(Long id, Map<String, Object> updates) {
        Place place = findById(id);
        if (place != null) {
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name" -> {
                        String newName = (String) value;
                        if (newName != null && !newName.trim().isEmpty()) {
                            place.setName(newName);
                        }
                    }
                    case "description" -> {
                        String newDescription = (String) value;
                        if (newDescription != null && !newDescription.trim().isEmpty()) {
                            place.setDescription(newDescription);
                        }
                    }
                    case "status" -> {
                        String newStatus = (String) value;
                        if (newStatus != null && !newStatus.trim().isEmpty()) {
                            try {
                                place.setStatus(PlaceStatus.valueOf(newStatus.toUpperCase()));
                            } catch (IllegalArgumentException e) {
                            }
                        }
                    }
                }
            });
            return repository.save(place);
        }
        return null;
    }

    // Eliminar un lugar por ID
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // Método para aceptar un lugar
    public boolean acceptPlace(Long placeId) {
        Place place = findById(placeId);
        if (place != null && place.getStatus() == PlaceStatus.pendiente) {
            place.setStatus(PlaceStatus.aceptada);
            repository.save(place);
            return true;
        }
        return false;
    }

    // Método para rechazar un lugar
    public boolean rejectPlace(Long placeId) {
        Place place = findById(placeId);
        if (place != null && place.getStatus() == PlaceStatus.pendiente) {
            place.setStatus(PlaceStatus.rechazada);
            repository.save(place);
            return true;
        }
        return false;
    }

    // Buscar lugares pendientes
    public List<Place> findPendingPlaces() {
        return repository.findAll().stream()
                .filter(place -> place.getStatus() == PlaceStatus.pendiente)
                .toList();
    }

    // Buscar lugares aceptados
    public List<Place> findAcceptedPlaces() {
        return repository.findAll().stream()
                .filter(place -> place.getStatus() == PlaceStatus.aceptada)
                .toList();
    }

    // Buscar lugares rechazados
    public List<Place> findRejectedPlaces() {
        return repository.findAll().stream()
                .filter(place -> place.getStatus() == PlaceStatus.rechazada)
                .toList();
    }

    // Obtener solicitudes pendientes desde el microservicio de solicitudes (nuestra adición)
    public List<Map<String, Object>> obtenerSolicitudesPendientes() {
        String url = "http://solicitudes:5007/solicitudes/pendientes";
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody();
        } catch (RestClientException | IllegalArgumentException e) {
            System.err.println("Error al llamar a solicitudes: " + e.getMessage());
            return List.of();
        }
    }

    // Crear solicitud automáticamente al guardar un lugar (nuestra adición)
    public Place saveWithSolicitud(Place place) {
        // Primero, guarda el lugar (usando el método de tu compañera)
        Place savedPlace = save(place);
        
        // Luego, crea la solicitud en el microservicio de solicitudes
        String url = "http://solicitudes:5007/solicitudes";
        Map<String, Object> solicitudData = Map.of(
            "nombre", place.getName(),
            "categoria", "Turístico",
            "ubicacion", "Desconocida"
        );
        
        try {
            restTemplate.postForObject(url, solicitudData, String.class);
            System.out.println("Solicitud creada automáticamente para el lugar: " + place.getName());
        } catch (RestClientException | IllegalArgumentException e) {
            System.err.println("Error al crear solicitud: " + e.getMessage());
        }
        
        return savedPlace;
    }
}