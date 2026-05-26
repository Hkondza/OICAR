package hr.team16.booksy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;

@Service
public class R2StorageService {

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.bucket}")
    private String bucket;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    private S3Client getClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
    }


    public String uploadPropertyImage(Long propertyId, MultipartFile file) throws IOException {
        String key = "properties/" + propertyId + "/cover." + getExtension(file);
        return upload(key, file);
    }


    public String uploadRoomImage(Long propertyId, Long roomId, MultipartFile file) throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String key = "properties/" + propertyId + "/rooms/" + roomId + "/" + timestamp + "." + getExtension(file);
        return upload(key, file);
    }


    public void deleteImage(String imageUrl) {
        String key = imageUrl.replace(publicUrl + "/", "");
        getClient().deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }

    private String upload(String key, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        getClient().putObject(request,
                RequestBody.fromBytes(file.getBytes()));

        return publicUrl + "/" + key;
    }

    private String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        return "jpg";
    }
}