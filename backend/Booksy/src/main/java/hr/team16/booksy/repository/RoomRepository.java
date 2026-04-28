package hr.team16.booksy.repository;

import hr.team16.booksy.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
  List<Room>findByPropertyId(Long propertyId);
}