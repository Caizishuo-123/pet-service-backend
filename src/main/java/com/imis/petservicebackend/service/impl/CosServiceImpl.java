package com.imis.petservicebackend.service.impl;

import com.imis.petservicebackend.config.CosProperties;
import com.imis.petservicebackend.service.CosService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CosServiceImpl implements CosService {

  private final COSClient cosClient;
  private final CosProperties props;

  /**
   * 上传文件到 COS
   *
   * @param file 要上传的文件
   * @param dir  存储目录，如 upload/head
   * @return 文件的相对路径，如 upload/head/uuid.jpg（不含域名前缀）
   */
  @Override
  public String upload(MultipartFile file, String dir) throws IOException {
    // 生成唯一文件名
    String originalFilename = file.getOriginalFilename();
    String suffix = "";
    if (originalFilename != null && originalFilename.contains(".")) {
      suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
    }
    String fileName = UUID.randomUUID().toString() + suffix;

    // 完整的 key（相对路径）
    String key = dir + "/" + fileName;

    // 设置文件元数据
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    metadata.setContentType(file.getContentType());

    // 上传到 COS
    PutObjectRequest request = new PutObjectRequest(
        props.getBucketName(),
        key,
        file.getInputStream(),
        metadata);
    cosClient.putObject(request);

    // 返回相对路径（不含域名），便于存入数据库
    return key;
  }
}
