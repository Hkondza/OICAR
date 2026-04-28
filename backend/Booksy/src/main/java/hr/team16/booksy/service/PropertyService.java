package hr.team16.booksy.service;


import hr.team16.booksy.dto.PropertyRequest;
import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.User;
import hr.team16.booksy.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    public List<Property> getAll() {
        return propertyRepository.findAll();
    }

    public Property getById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NEMAA"));
    }

    public Property create(PropertyRequest req, User owner) {
        Property p = new Property();
        p.setOwner(owner);
        p.setName(req.getName());
        p.setAddress(req.getAddress());
        p.setCity(req.getCity());
        p.setCountry(req.getCountry());
        return propertyRepository.save(p);
    }

    public Property update(Long id, PropertyRequest req, User currentUser) {
        Property p = getById(id);
        if (!p.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Forbidden");
        }
        p.setName(req.getName());
        p.setAddress(req.getAddress());
        p.setCity(req.getCity());
        p.setCountry(req.getCountry());
        return propertyRepository.save(p);
    }

    public void delete(Long id, User currentUser) {
        Property p = getById(id);
        if (!p.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Forbidden");
        }
        propertyRepository.deleteById(id);
    }
}

