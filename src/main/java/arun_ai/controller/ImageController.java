package arun_ai.controller;

import arun_ai.service.PhotoOutpaintingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    private final PhotoOutpaintingService photoOutpaintingService;

    public ImageController(PhotoOutpaintingService photoOutpaintingService) {
        this.photoOutpaintingService = photoOutpaintingService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") String id) {
        String cleanId = id.replace(".jpg", "").replace(".png", "");
        byte[] bytes = photoOutpaintingService.getImageBytes(cleanId);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Cache-Control", "public, max-age=86400");
        headers.set("Content-Disposition", "inline; filename=\"" + id + "\"");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();

    @GetMapping("/proxy")
    public ResponseEntity<byte[]> proxyImage(
            @RequestParam("url") String url,
            @RequestParam(value = "filename", required = false, defaultValue = "ArunAI-HD-Picture.jpg") String filename) {
        try {
            var request = java.net.http.HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(java.time.Duration.ofSeconds(15))
                    .GET()
                    .build();

            var response = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                HttpHeaders headers = new HttpHeaders();
                String contentType = response.headers().firstValue("Content-Type").orElse(MediaType.IMAGE_JPEG_VALUE);
                headers.setContentType(MediaType.parseMediaType(contentType));
                headers.set("Access-Control-Allow-Origin", "*");
                headers.set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                return new ResponseEntity<>(response.body(), headers, HttpStatus.OK);
            }
            return ResponseEntity.status(response.statusCode()).build();
        } catch (Exception e) {
            log.error("Image proxy failed for URL: {}", url, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
