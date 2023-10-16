package com.backbase.modelbank.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SpringCloudSqsConfig {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate() {
        return new QueueMessagingTemplate(amazonSqsAsync());
    }

    public AmazonSQSAsync amazonSqsAsync() {

        AmazonSQSAsyncClientBuilder amazonSqsAsyncClientBuilder = AmazonSQSAsyncClientBuilder.standard();
        AmazonSQSAsync amazonSqsAsync = null;
        amazonSqsAsyncClientBuilder.withRegion(region);
        BasicAWSCredentials basicAwsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        amazonSqsAsyncClientBuilder.withCredentials(new AWSStaticCredentialsProvider(basicAwsCredentials));
        amazonSqsAsync = amazonSqsAsyncClientBuilder.build();
        return amazonSqsAsync;

    }

}
