package com.abhi.aws.sns.config;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AwsSnsProperties.class)
public class AwsConfig {
    
    private final AwsSnsProperties awsSnsProperties;
    
    @Autowired
    public AwsConfig(AwsSnsProperties awsSnsProperties) {
        this.awsSnsProperties = awsSnsProperties;
    }

    @Bean
    public SnsClient snsClient() {
        String accessKey = awsSnsProperties.getAccessKey();
        String secretKey = awsSnsProperties.getSecretKey();
        
        // Check if explicit credentials are provided (local development with .env)
        if (accessKey != null && !accessKey.trim().isEmpty() 
            && secretKey != null && !secretKey.trim().isEmpty()) {
            // Local: Use explicit credentials from .env file
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            return SnsClient.builder()
                    .region(Region.of(awsSnsProperties.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .build();
        } else {
            // EC2: Use default credential chain (IAM role from instance metadata)
            return SnsClient.builder()
                    .region(Region.of(awsSnsProperties.getRegion()))
                    .build();
        }
    }
}
