package com.carpool.carpool.controller.media;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Media", description = "Operaciones relacionadas con imagenes")
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    @PostMapping("/users/{ownerId}")
    public ResponseEntity<Long> upload(@PathVariable Long ownerId, @RequestParam("file") MultipartFile file){
        return null;
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<InputStreamResource> get(@PathVariable Long assetId) {
        return null;
    }
}
