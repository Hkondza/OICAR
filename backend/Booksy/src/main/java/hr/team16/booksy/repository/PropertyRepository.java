package hr.team16.booksy.repository;

import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property,Long> {
    List<Property>findByOwner(User owner);
}
