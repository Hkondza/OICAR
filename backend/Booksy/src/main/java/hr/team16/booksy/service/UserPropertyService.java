package hr.team16.booksy.service;

import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.User;
import hr.team16.booksy.model.UserProperty;
import hr.team16.booksy.repository.PropertyRepository;
import hr.team16.booksy.repository.UserPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.configuration.oauth2.SpringDocOidcProviderConfiguration;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPropertyService {
    private final UserPropertyRepository userPropertyRepository;
    private final PropertyRepository propertyRepository;

    public UserProperty requestAccess(Long propertyId, User user) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        UserProperty up = new UserProperty();
        up.setUser(user);
        up.setProperty(property);
        up.setStatus("PENDING");
        return userPropertyRepository.save(up);
    }

    public List<UserProperty> getPending() {
        return userPropertyRepository.findByStatus("PENDING");
    }

    public UserProperty updateStatus(Long id, String status) {
        UserProperty up = userPropertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        up.setStatus(status);
        up.setReviewedAt(LocalDateTime.now());
        return userPropertyRepository.save(up);
    }
}

