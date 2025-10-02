package com.megamaker.study.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Slf4j
@RequestMapping("/image")
@RestController
public class CacheController {

    @GetMapping("/v0")
    public ResponseEntity<Resource> getImageV0(@RequestParam String imageName) throws IOException {
        Path imagePath = Paths.get("/images/" + imageName + ".jpg");

        try {
            UrlResource resource = new UrlResource(imagePath.toUri());

            // 이미지 파일을 못 찾을 때
            if (!resource.isReadable() || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // MediaType 설정
            String mediaTypeStr = Files.probeContentType(imagePath);
            MediaType mediaType = MediaType.parseMediaType(mediaTypeStr);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(Files.size(imagePath))
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("이미지 파일 없음", e);
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/v1")
    public ResponseEntity<Resource> getImageV1(@RequestParam String imageName) throws IOException {
        Path imagePath = Paths.get("/images/" + imageName + ".jpg");

        try {
            UrlResource resource = new UrlResource(imagePath.toUri());

            // 이미지 파일을 못 찾을 때
            if (!resource.isReadable() || !resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // MediaType 설정
            String mediaTypeStr = Files.probeContentType(imagePath);
            MediaType mediaType = MediaType.parseMediaType(mediaTypeStr);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(Files.size(imagePath))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
                    .body(resource);
        } catch (MalformedURLException e) {
            log.error("이미지 파일 없음", e);
            return ResponseEntity.notFound().build();
        }
    }

    private final Cache<String, byte[]> cacheV2 = Caffeine.newBuilder()
            .maximumSize(10)  // 최대 저장 갯수
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @GetMapping("/v2")
    public ResponseEntity<byte[]> getImageV2(@RequestParam String imageName, HttpServletResponse response) throws IOException {
        Path imagePath = Paths.get("/images/" + imageName + ".jpg");
        String mediaTypeStr = Files.probeContentType(imagePath);  // MediaType 설정
        MediaType mediaType = MediaType.parseMediaType(mediaTypeStr);

        byte[] cachedImg = cacheV2.getIfPresent(imageName);

        if (cachedImg != null) {  // 이미 캐시에 있을 때
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .contentLength(Files.size(imagePath))
                    .body(cachedImg);
        }

        // 캐시에 없을 때
        // 이미지 파일이 존재하는지 확인
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(imagePath);
        cacheV2.put(imageName, imageBytes);  // 이미지 캐시 처리

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(imagePath))
                .body(imageBytes);
    }

    private final Cache<String, Image> cacheV3 = Caffeine.newBuilder()
            .maximumSize(10)  // 최대 저장 갯수
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @GetMapping("/v3")
    public ResponseEntity<byte[]> getImageV3(@RequestParam String imageName, HttpServletResponse response) throws IOException {
        Image image = cacheV3.getIfPresent(imageName);

        if (image != null) {  // 이미 캐시에 있을 때
            return ResponseEntity.ok()
                    .contentType(image.getMediaType())
                    .contentLength(image.getSize())
                    .body(image.getBytes());
        }

        Path imagePath = Paths.get("/images/" + imageName + ".jpg");
        String mediaTypeStr = Files.probeContentType(imagePath);  // MediaType 설정
        MediaType mediaType = MediaType.parseMediaType(mediaTypeStr);
        byte[] imageBytes = Files.readAllBytes(imagePath);

        // 캐시에 없을 때
        // 이미지 파일이 존재하는지 확인
        if (!Files.exists(imagePath)) {
            return ResponseEntity.notFound().build();
        }

//        Image.builder()
//                        .bytes()
//                                .

//        cacheV3.put(imageName, imageBytes);  // 이미지 캐시 처리

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(Files.size(imagePath))
                .body(imageBytes);
    }
}
