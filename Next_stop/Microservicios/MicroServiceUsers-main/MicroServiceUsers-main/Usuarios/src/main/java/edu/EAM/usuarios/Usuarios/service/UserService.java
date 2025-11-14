package edu.EAM.usuarios.Usuarios.service;

import edu.EAM.usuarios.Usuarios.model.Address;
import edu.EAM.usuarios.Usuarios.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.EAM.usuarios.Usuarios.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
        // Elimina initSampleData() o muévelo a un script SQL si necesitas datos iniciales
    }

    public User save(User user) {
        // JPA maneja la generación de ID automáticamente si es null
        return repository.save(user);
    }

    public User findById(String id) {
        Optional<User> user = repository.findById(id);
        return user.orElse(null);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public List<User> findByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }

    public User update(User user) {
        // save() en JPA hace upsert (inserta si no existe, actualiza si sí)
        return repository.save(user);
    }

    public User patch(String id, Map<String, Object> updates) {
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            updates.forEach((key, value) -> {
                switch (key) {
                    case "name" -> {
                        String newName = (String) value;
                        if (newName != null && !newName.trim().isEmpty()) {
                            user.setName(newName);
                        }
                    }
                    case "gender" -> {
                        String newGender = (String) value;
                        if (newGender != null && !newGender.trim().isEmpty()) {
                            user.setGender(newGender);
                        }
                    }
                    case "email" -> {
                        String newEmail = (String) value;
                        if (newEmail != null && !newEmail.trim().isEmpty()) {
                            user.setEmail(newEmail);
                        }
                    }
                    case "phoneNumber" -> {
                        String newPhone = (String) value;
                        if (newPhone != null && !newPhone.trim().isEmpty()) {
                            user.setPhoneNumber(newPhone);
                        }
                    }
                    case "address" -> {
                        if (value instanceof Map<?, ?> addrMap) {
                            Address current = user.getAddress();
                            if (current == null) {
                                current = new Address();
                                user.setAddress(current);
                            }
                            String street = (String) addrMap.get("street");
                            if (street != null && !street.trim().isEmpty()) {
                                current.setStreet(street);
                            }
                            String number = (String) addrMap.get("number");
                            if (number != null && !number.trim().isEmpty()) {
                                current.setNumber(number);
                            }
                            String neighborhood = (String) addrMap.get("neighborhood");
                            if (neighborhood != null && !neighborhood.trim().isEmpty()) {
                                current.setNeighborhood(neighborhood);
                            }
                            String city = (String) addrMap.get("city");
                            if (city != null && !city.trim().isEmpty()) {
                                current.setCity(city);
                            }
                            String postalCode = (String) addrMap.get("postalCode");
                            if (postalCode != null && !postalCode.trim().isEmpty()) {
                                current.setPostalCode(postalCode);
                            }
                        }
                    }
                }
            });
            return repository.save(user);  // Guarda los cambios
        }
        return null;
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }
}