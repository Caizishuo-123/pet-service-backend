package com.imis.petservicebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CosService {

  String upload(MultipartFile file, String dir) throws IOException;
}
