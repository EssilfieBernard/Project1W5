package com.essilfie.UploadToS3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AmazonS3Service {

    @Autowired
    private AmazonS3 amazonS3;

    private final String bucketName = "essilfie";

    public String uploadImage(MultipartFile file) throws IOException {
        String fileName = generateFileName(file);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(new PutObjectRequest(
                bucketName,
                fileName,
                file.getInputStream(),
                metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    public String getImageUrl(String fileName) {
        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    public Map<String, Object> listAllImagesWithPagination(int page, int size) {
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withMaxKeys(10000);

        ListObjectsV2Result result = amazonS3.listObjectsV2(listObjectsRequest);
        List<S3ObjectSummary> objectSummaries = result.getObjectSummaries();

        int totalElements = objectSummaries.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        List<Map<String, Object>> pageContent = new ArrayList<>();
        if (startIndex < totalElements) {
            pageContent = objectSummaries.subList(startIndex, endIndex).stream()
                    .map(summary -> {
                        Map<String, Object> imageData = new HashMap<>();
                        imageData.put("url", amazonS3.getUrl(bucketName, summary.getKey()).toString());
                        imageData.put("fileName", summary.getKey());
                        imageData.put("lastModified", summary.getLastModified().getTime());
                        imageData.put("size", summary.getSize());
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
        amazonS3.deleteObject(bucketName, fileName);
    }

    private String generateFileName(MultipartFile file) {
        return new Date().getTime() + "-" + Objects.requireNonNull(file.getOriginalFilename()).replace(" ", "_");
    }
}
