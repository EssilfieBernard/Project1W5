package com.essilfie.UploadToS3;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static software.amazon.awssdk.services.s3.model.ObjectCannedACL.PUBLIC_READ;

@Service
public class AmazonS3Service {

    @Autowired
    private S3Client s3Client;

    private final String bucketName = "essilfie";

    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = generateFileName(file);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .acl(PUBLIC_READ)
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        return s3Client.utilities().getUrl(request).toString();
    }

    public String getImageUrl(String fileName) {
        GetUrlRequest request = GetUrlRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        return s3Client.utilities().getUrl(request).toString();
    }

    public Map<String, Object> listAllImagesWithPagination(int page, int size) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(10000)
                .build();

        ListObjectsV2Response result = s3Client.listObjectsV2(listObjectsRequest);
        List<S3Object> objects = result.contents();

        int totalElements = objects.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Map<String, Object>> pageContent = new ArrayList<>();
        if (startIndex < totalElements) {
            pageContent = objects.subList(startIndex, endIndex).stream()
                    .map(object -> {
                        Map<String, Object> imageData = new HashMap<>();

                        GetUrlRequest request = GetUrlRequest.builder()
                                .bucket(bucketName)
                                .key(object.key())
                                .build();

                        imageData.put("url", s3Client.utilities().getUrl(request).toString());
                        imageData.put("filename", object.key());
                        imageData.put("lastModified", object.lastModified().toEpochMilli());
                        imageData.put("size", object.size());
                        return imageData;
                    })
                    .collect(Collectors.toList());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageContent);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("totalElements", totalElements);
        response.put("size", pageContent.size());

        return response;
    }

    public void deleteImage(String fileName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(request);
    }

    private String generateFileName(MultipartFile file) {
        return new Date().getTime() + "-" + Objects.requireNonNull(file.getOriginalFilename()).replace(" ", "_");
    }
}
