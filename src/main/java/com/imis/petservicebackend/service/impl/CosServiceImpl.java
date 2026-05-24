package com.imis.petservicebackend.service.impl;

import com.imis.petservicebackend.common.BusinessException;
import com.imis.petservicebackend.config.CosProperties;
import com.imis.petservicebackend.service.CosService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CosServiceImpl implements CosService {

  private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(
      Arrays.asList(".jpg", ".jpeg", ".png", ".webp"));

  private static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = new HashSet<>(
      Arrays.asList("image/jpeg", "image/png", "image/webp"));

  private final COSClient cosClient;
  private final CosProperties props;

  @Override
  public void validateImageFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new BusinessException("上传文件不能为空");
    }
    if (file.getSize() > MAX_IMAGE_SIZE) {
      throw new BusinessException("图片大小不能超过 5MB");
    }

    String originalFilename = file.getOriginalFilename();
    String extension = extractExtension(originalFilename);
    if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
      throw new BusinessException("仅支持 JPG、PNG、WEBP 格式图片");
    }

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
      throw new BusinessException("图片类型无效，请上传 JPG、PNG 或 WEBP 图片");
    }
  }

  /**
   * 上传文件到 COS
   *
   * @param file 要上传的文件
   * @param dir  存储目录，如 upload/head
   * @return 文件的相对路径，如 upload/head/uuid.jpg（不含域名前缀）
   */
  @Override
  public String upload(MultipartFile file, String dir) throws IOException {
    validateImageFile(file);

    // 生成唯一文件名
    String originalFilename = file.getOriginalFilename();
    String suffix = extractExtension(originalFilename);
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

  private String extractExtension(String originalFilename) {
    if (originalFilename == null || !originalFilename.contains(".")) {
      return "";
    }
    return originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
  }
}
