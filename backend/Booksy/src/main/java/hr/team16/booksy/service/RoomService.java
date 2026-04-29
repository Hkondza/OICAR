package hr.team16.booksy.service;

import hr.team16.booksy.dto.RoomRequest;
import hr.team16.booksy.dto.RoomResponse;
import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.Room;
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


    public RoomResponse addRoom(Long propertyId, RoomRequest request, String email) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));


        if (property.getOwner() == null ||
                !property.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Not your property");
        }

        Room room = new Room();
        room.setProperty(property);
        room.setName(request.getName());
        room.setCapacity(request.getCapacity());
        room.setPricePerNight(request.getPricePerNight());
        room.setAvailableFrom(request.getAvailableFrom());
        room.setAvailableTo(request.getAvailableTo());
        roomRepository.save(room);

        return mapToResponse(room);
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<RoomResponse> getRoomsByProperty(Long propertyId) {
        return roomRepository.findByPropertyId(propertyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return mapToResponse(room);
    }

    public RoomResponse updateRoom(Long roomId, RoomRequest request, String email) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));


        if (room.getProperty().getOwner() == null ||
                !room.getProperty().getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Not your room");
        }

        room.setName(request.getName());
        room.setCapacity(request.getCapacity());
        room.setPricePerNight(request.getPricePerNight());
        room.setAvailableFrom(request.getAvailableFrom());
        room.setAvailableTo(request.getAvailableTo());
        roomRepository.save(room);

        return mapToResponse(room);
    }

    public void deleteRoom(Long roomId, String email) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Provjeri je li ovo vlasnikov room
        if (room.getProperty().getOwner() == null ||
                !room.getProperty().getOwner().getEmail().equals(email)) {
            throw new RuntimeException("Not your room");
        }

        roomRepository.deleteById(roomId);
    }

    private RoomResponse mapToResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setName(room.getName());
        response.setCapacity(room.getCapacity());
        response.setPricePerNight(room.getPricePerNight());
        response.setAvailableFrom(room.getAvailableFrom());
        response.setAvailableTo(room.getAvailableTo());
        response.setPropertyId(room.getProperty().getId());
        response.setPropertyName(room.getProperty().getName());
        response.setCity(room.getProperty().getCity());
        return response;
    }
}