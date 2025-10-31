package com.demo.demo.controllers;

import com.demo.demo.DTO.RegisterDto;
import com.demo.demo.services.UserInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserInterface userInterface;

    private static final String PDP_DIR = "PDP";

    @PostMapping("/addwithconfpassword")
    public ResponseEntity<?> addUserWithConfPassword(
            @RequestPart("user") RegisterDto userDto,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            // Crée le dossier PDP s’il n’existe pas
            File dir = new File(PDP_DIR);
            if (!dir.exists()) dir.mkdirs();

            if (imageFile != null && !imageFile.isEmpty()) {
                // 🔹 Détermine l’extension de l’image
                String originalName = imageFile.getOriginalFilename();
                String extension = originalName.substring(originalName.lastIndexOf("."));
                if (extension == null || extension.isEmpty()) {
                    extension = ".png"; // fallback
                }

                // 🔹 Nom final = username + extension
                String fileName = userDto.getUsername() + extension;

                // 🔹 Copie du fichier
                String filePath = PDP_DIR + File.separator + fileName;
                Files.copy(imageFile.getInputStream(), Paths.get(filePath));

                // 🔹 On stocke seulement le nom du fichier
                userDto.setImageUrl(fileName);
            }

            String result = userInterface.addUserWTCP(userDto);
            return ResponseEntity.ok(result);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création du compte : " + e.getMessage());
        }
    }
}
