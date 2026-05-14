package hr.team16.booksy.service;

import hr.team16.booksy.dto.PropertyRequest;
import hr.team16.booksy.dto.PropertyResponse;
import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.User;
import hr.team16.booksy.model.UserProperty;
import hr.team16.booksy.repository.PropertyRepository;
import hr.team16.booksy.repository.UserPropertyRepository;
import hr.team16.booksy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final UserRepository userRepository;

    public PropertyResponse submitProperty(PropertyRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Property property = new Property();
        property.setName(request.getName());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setCountry(request.getCountry());
        propertyRepository.save(property);

        UserProperty userProperty = new UserProperty();
        userProperty.setUser(owner);
        userProperty.setProperty(property);
        userProperty.setStatus("PENDING");
        userPropertyRepository.save(userProperty);

        return mapToResponse(property, "PENDING", owner.getEmail());
    }

    public List<PropertyResponse> getPendingRequests() {
        return userPropertyRepository.findByStatus("PENDING").stream()
                .map(up -> mapToResponse(
                        up.getProperty(),
                        up.getStatus(),
                        up.getUser().getEmail()))
                .toList();
    }

    public PropertyResponse approveProperty(Long propertyId) {
        UserProperty userProperty = userPropertyRepository.findByPropertyId(propertyId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        userProperty.setStatus("ACCEPTED");
        userProperty.setReviewedAt(LocalDateTime.now());
        userPropertyRepository.save(userProperty);

        Property property = userProperty.getProperty();
        property.setOwner(userProperty.getUser());
        propertyRepository.save(property);

        return mapToResponse(property, "ACCEPTED", userProperty.getUser().getEmail());
    }

    public PropertyResponse denyProperty(Long propertyId) {
        UserProperty userProperty = userPropertyRepository.findByPropertyId(propertyId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        userProperty.setStatus("DENIED");
        userProperty.setReviewedAt(LocalDateTime.now());
        userPropertyRepository.save(userProperty);

        return mapToResponse(
                userProperty.getProperty(),
                "DENIED",
                userProperty.getUser().getEmail());
    }

    public List<PropertyResponse> getApprovedProperties() {
        return propertyRepository.findAll().stream()
                .filter(p -> p.getOwner() != null)
                .map(p -> mapToResponse(p, "ACCEPTED", p.getOwner().getEmail()))
                .toList();
    }

    public List<PropertyResponse> getOwnerProperties(Long userId) {
        return userPropertyRepository.findByUserId(userId).stream()
                .map(up -> mapToResponse(up.getProperty(), up.getStatus(), up.getUser().getEmail()))
                .toList();
    }

    private PropertyResponse mapToResponse(Property property, String status, String ownerEmail) {
        PropertyResponse response = new PropertyResponse();
        response.setId(property.getId());
        response.setName(property.getName());
        response.setAddress(property.getAddress());
        response.setCity(property.getCity());
        response.setCountry(property.getCountry());
        response.setStatus(status);
        response.setOwnerEmail(ownerEmail);
        return response;
    }

    public PropertyResponse getById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        String status = property.getOwner() != null ? "ACCEPTED" : "PENDING";
        String ownerEmail = property.getOwner() != null ?
                property.getOwner().getEmail() : "pending";
        return mapToResponse(property, status, ownerEmail);
    }

    public PropertyResponse update(Long id, PropertyRequest request,Long userId) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (property.getOwner() == null ||
                !property.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not your property");
        }
        property.setName(request.getName());
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setCountry(request.getCountry());
        propertyRepository.save(property);
        return mapToResponse(property, "ACCEPTED", property.getOwner().getEmail());
    }

    public void delete(Long id, Long userId) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        if (property.getOwner() == null ||
                !property.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not your property");
        }
        propertyRepository.delete(property);
    }
}