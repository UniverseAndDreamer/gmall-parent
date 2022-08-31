package com.atguigu.gmall.product.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioClientAutoConfiguration {

    @Autowired
    private MinioClientProperties minioClientProperties;

    @Bean
    public MinioClient getMinioClient() throws Exception {
        MinioClient minioClient = new MinioClient(
                minioClientProperties.getEndpoint(),
                minioClientProperties.getAccessKey(),
                minioClientProperties.getSecretKey());
        return minioClient;
    }
}
