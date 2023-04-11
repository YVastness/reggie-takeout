package com.yinhaoyu.controller;

import com.yinhaoyu.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("common")
public class CommonController {
    @Value("${reggie.img-path}")
    private String basePath;

    @PostMapping("upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return null;
        }
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // file时临时文件，需要保存到指定位置，本次请求完会被删除
        file.transferTo(new File(basePath + fileName));
        return Result.success(fileName);
    }

    @GetMapping("download")
    public void download(String name, HttpServletResponse response) {
        try {
            FileInputStream fileInputStream = new FileInputStream(basePath + name);
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");
            byte[] bytes = new byte[1024 * 1024];
            while (fileInputStream.read(bytes) != -1) {
                outputStream.write(bytes);
                outputStream.flush();
            }
            outputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
