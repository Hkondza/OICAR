package hr.team16.booksy.service;

import hr.team16.booksy.dto.RoomRequest;
import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.Room;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.PropertyRepository;
import hr.team16.booksy.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final PropertyRepository propertyRepository;

    public List<Room> getAll() {
        return roomRepository.findAll();
    }

    public List<Room> getByProperty(Long propertyId) {
        return roomRepository.findByPropertyId(propertyId);
    }

    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public Room create(RoomRequest req, User owner) {
        Property property = propertyRepository.findById(req.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));
        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden");
        }
        Room room = new Room();
        room.setProperty(property);
        room.setName(req.getName());
        room.setCapacity(req.getCapacity());
        room.setPricePerNight(req.getPricePerNight());
        room.setAvailableFrom(req.getAvailableFrom());
        room.setAvailableTo(req.getAvailableTo());
        return roomRepository.save(room);
    }

    public Room update(Long id, RoomRequest req, User owner) {
        Room room = getById(id);
        if (!room.getProperty().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden");
        }
        room.setName(req.getName());
        room.setCapacity(req.getCapacity());
        room.setPricePerNight(req.getPricePerNight());
        room.setAvailableFrom(req.getAvailableFrom());
        room.setAvailableTo(req.getAvailableTo());
        return roomRepository.save(room);
    }

    public void delete(Long id, User owner) {
        Room room = getById(id);
        if (!room.getProperty().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden");
        }
        roomRepository.deleteById(id);
    }
}
