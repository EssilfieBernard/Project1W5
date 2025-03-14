package com.essilfie.UploadToS3;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = imageService.uploadImage(file);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Image uploaded successfully: " + fileName);
            response.put("url", fileName);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Upload failed: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/{fileName}")
    public ResponseEntity<String> getImage(@PathVariable String fileName) {
        try {
            String url = imageService.getImageUrl(fileName);
            return new ResponseEntity<>(url, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Fetch failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(imageService.listAllImagesWithPagination(page, size));
    }

    @DeleteMapping("/{fileName}")
    public void deleteImage(@PathVariable String fileName) {
        imageService.deleteImage(fileName);
    }
}