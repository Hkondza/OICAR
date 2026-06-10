package hr.team16.booksy.controller;

import hr.team16.booksy.model.Property;
import hr.team16.booksy.model.Room;
import hr.team16.booksy.repository.PropertyRepository;
import hr.team16.booksy.repository.RoomRepository;
import hr.team16.booksy.service.R2StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images")
public class ImageController {

    private final R2StorageService r2StorageService;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final ObjectMapper objectMapper;


    @PostMapping(value = "/property/{propertyId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload property image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> uploadPropertyImage(
            @PathVariable Long propertyId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long userId) {
        try {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));



            String imageUrl = r2StorageService.uploadPropertyImage(propertyId, file);
            property.setImageUrl(imageUrl);
            propertyRepository.save(property);

            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }


    @PostMapping(value = "/room/{roomId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload room images",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<String>> uploadRoomImages(
            @PathVariable Long roomId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal Long userId) {
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            if (room.getProperty().getOwner() == null ||
                    !room.getProperty().getOwner().getId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }

            Long propertyId = room.getProperty().getId();
            List<String> newUrls = new ArrayList<>();

            for (MultipartFile file : files) {
                String imageUrl = r2StorageService.uploadRoomImage(propertyId, roomId, file);
                newUrls.add(imageUrl);
            }

            List<String> urls = new ArrayList<>();
            if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
                urls = objectMapper.readValue(
                        room.getImageUrls(),
                        new TypeReference<List<String>>() {}
                );
            }
            urls.addAll(newUrls);
            room.setImageUrls(objectMapper.writeValueAsString(urls));
            roomRepository.save(room);

            return ResponseEntity.ok(newUrls);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/room/{roomId}")
    @Operation(summary = "Delete room image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> deleteRoomImage(
            @PathVariable Long roomId,
            @RequestParam("imageUrl") String imageUrl,
            @AuthenticationPrincipal Long userId) {
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            if (room.getProperty().getOwner() == null ||
                    !room.getProperty().getOwner().getId().equals(userId)) {
                return ResponseEntity.status(403).body("Not your room");
            }

            r2StorageService.deleteImage(imageUrl);

            List<String> urls = new ArrayList<>();
            if (room.getImageUrls() != null && !room.getImageUrls().isEmpty()) {
                urls = objectMapper.readValue(
                        room.getImageUrls(),
                        new TypeReference<List<String>>() {}
                );
            }
            urls.remove(imageUrl);
            room.setImageUrls(objectMapper.writeValueAsString(urls));
            roomRepository.save(room);

            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Delete failed: " + e.getMessage());
        }
    }


    @DeleteMapping("/property/{propertyId}")
    @Operation(summary = "Delete property image",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<String> deletePropertyImage(
            @PathVariable Long propertyId,
            @AuthenticationPrincipal Long userId) {
        try {
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found"));

            if (property.getOwner() == null ||
                    !property.getOwner().getId().equals(userId)) {
                return ResponseEntity.status(403).body("Not your property");
            }

            if (property.getImageUrl() != null) {
                r2StorageService.deleteImage(property.getImageUrl());
                property.setImageUrl(null);
                propertyRepository.save(property);
            }

            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Delete failed: " + e.getMessage());
        }
    }
}