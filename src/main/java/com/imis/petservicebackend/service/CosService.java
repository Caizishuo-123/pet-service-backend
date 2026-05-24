package com.imis.petservicebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CosService {

  long MAX_IMAGE_SIZE = 5L * 1024 * 1024;

  void validateImageFile(MultipartFile file);

  String upload(MultipartFile file, String dir) throws IOException;
}
