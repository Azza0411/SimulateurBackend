package com.demo.demo.services;

import com.demo.demo.entities.CoursModule;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CoursModuleInterface {
    CoursModule createModule(CoursModule module, MultipartFile imageFile, MultipartFile videoFile) throws IOException;
    List<CoursModule> getAllModules();
    Optional<CoursModule> getModuleById(Integer id);
CoursModule updateModule(Integer id, CoursModule module, MultipartFile imageFile, MultipartFile videoFile) throws IOException;
    void deleteModule(Integer id);}
