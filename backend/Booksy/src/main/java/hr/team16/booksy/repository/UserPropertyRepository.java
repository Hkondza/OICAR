package hr.team16.booksy.repository;

import hr.team16.booksy.model.UserProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPropertyRepository extends JpaRepository<UserProperty,Long> {
    List<UserProperty>findByStatus(String status);
}
