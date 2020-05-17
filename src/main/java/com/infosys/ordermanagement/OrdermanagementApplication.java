package com.infosys.ordermanagement;

import java.io.*;
import java.util.Properties;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@ComponentScan({"com.infosys.ordermanagement"})
public class OrdermanagementApplication {

    public static Properties PROP;

    public static void main(String[] args) {
        PROP = readPropertiesFromS3();

        SpringApplication app = new SpringApplication(OrdermanagementApplication.class);
        app.setAdditionalProfiles("aws");
        app.run(args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    public static Properties readPropertiesFromS3() {

        String key_name = "application.properties";
        String bucket_name = "order-properties";
        Properties prop = new Properties();

        final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        S3Object object = s3.getObject(new GetObjectRequest(bucket_name, key_name));
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arrOfStr = line.split("=");
                prop.put(arrOfStr[0].trim(), arrOfStr[1].trim());
            }

            object.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return prop;

    }
}
