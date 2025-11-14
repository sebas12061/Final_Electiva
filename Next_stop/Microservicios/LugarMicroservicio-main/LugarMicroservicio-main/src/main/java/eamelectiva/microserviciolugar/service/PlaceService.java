package eamelectiva.microserviciolugar.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eamelectiva.microserviciolugar.model.Place;
import eamelectiva.microserviciolugar.model.PlaceStatus;
import eamelectiva.microserviciolugar.repository.PlaceRepository;

@Service
public class PlaceService {

    private final PlaceRepository repository;

    @Autowired
    public PlaceService(PlaceRepository repository) {
        this.repository = repository;
    }

    // Crear un nuevo lugar
    public Place save(Place place) {
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
        if (place != null && place.getStatus() == PlaceStatus.PENDING) {
            place.setStatus(PlaceStatus.ACCEPTED);
            repository.save(place);
            return true;
        }
        return false;
    }

    // Método para rechazar un lugar
    public boolean rejectPlace(Long placeId) {
        Place place = findById(placeId);
        if (place != null && place.getStatus() == PlaceStatus.PENDING) {
            place.setStatus(PlaceStatus.REJECTED);
            repository.save(place);
            return true;
        }
        return false;
    }

    // Buscar lugares pendientes
    public List<Place> findPendingPlaces() {
        return repository.findAll().stream()
                .filter(place -> place.getStatus() == PlaceStatus.PENDING)
                .toList();
    }

    // Buscar lugares aceptados
    public List<Place> findAcceptedPlaces() {
        return repository.findAll().stream()
                .filter(place -> place.getStatus() == PlaceStatus.ACCEPTED)
                .toList();
    }

    // Buscar lugares rechazados
    public List<Place> findRejectedPlaces() {
        return repository.findAll().stream()
                .filter(place -> place.getStatus() == PlaceStatus.REJECTED)
                .toList();
    }
}
