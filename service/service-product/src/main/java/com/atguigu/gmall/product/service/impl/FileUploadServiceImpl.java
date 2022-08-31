package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.product.config.MinioClientProperties;
import com.atguigu.gmall.product.service.FileUploadService;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private MinioClient minioClient;
    @Autowired
    private MinioClientProperties minioClientProperties;


    @Override
    public String upload(MultipartFile file) throws Exception {
        String bucketName = minioClientProperties.getBucketName();
        if (!minioClient.bucketExists(bucketName)) {
            //说明bucket不存在
            minioClient.makeBucket(bucketName);
        }
        //原文件名
        String originalFilename = file.getOriginalFilename();
        //生成日期文件夹
        String dateStr = DateUtil.formatDate(new Date());
        //生成存储对象名
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFilename;
        //设置存储参数
        PutObjectOptions putObjectOptions = new PutObjectOptions(file.getSize(), -1L);
        //得到文件流
        InputStream inputStream = file.getInputStream();
        //设置存储后的响应头
        putObjectOptions.setContentType(file.getContentType());
        //存储对象
        minioClient.putObject(bucketName, dateStr + "/" + fileName, inputStream, putObjectOptions);
        //得到URL
        String url = minioClientProperties.getEndpoint() + bucketName + "/" + dateStr + "/" + fileName;

        return url;

    }
}
