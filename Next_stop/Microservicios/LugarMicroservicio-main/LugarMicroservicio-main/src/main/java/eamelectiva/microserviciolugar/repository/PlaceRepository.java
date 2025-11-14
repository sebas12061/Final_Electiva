package eamelectiva.microserviciolugar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import eamelectiva.microserviciolugar.model.Place;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long>{

}
