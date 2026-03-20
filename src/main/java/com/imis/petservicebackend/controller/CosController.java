package com.imis.petservicebackend.controller;

import com.imis.petservicebackend.common.Result;
import com.imis.petservicebackend.service.CosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * COS 对象存储上传接口
 * 参考管理端实现，用于用户端上传图片到 COS
 */
@RestController
@RequestMapping("/cos")
@RequiredArgsConstructor
@Slf4j
public class CosController {

    private final CosService cosService;

    /**
     * 通用文件上传接口
     *
     * @param file 上传的文件
     * @param type 上传类型：head(头像), pet(宠物图片), service(服务图片), post(帖子图片)
     * @return 文件在 COS 中的相对路径，如 upload/pet/uuid.jpg
     */
    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {

        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        try {
            String dir;
            switch (type) {
                case "head":
                    dir = "upload/head";
                    break;
                case "pet":
                    dir = "upload/pet";
                    break;
                case "service":
                    dir = "upload/pet-service";
                    break;
                case "post":
                    dir = "upload/community";
                    break;
                default:
                    dir = "upload/other";
            }

            String relativePath = cosService.upload(file, dir);
            log.info("文件上传成功，相对路径: {}", relativePath);

            return Result.success(relativePath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }
}
