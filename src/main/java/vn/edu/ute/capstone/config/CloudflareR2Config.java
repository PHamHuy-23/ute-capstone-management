package vn.edu.ute.capstone.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Slf4j
@Getter
@Configuration
public class CloudflareR2Config {

    @Value("${cloudflare.r2.bucket-name:ute-data}")
    private String bucketName;

    @Value("${cloudflare.r2.access-key-id}")
    private String accessKeyId;

    @Value("${cloudflare.r2.secret-access-key}")
    private String secretAccessKey;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.public-url:}")
    private String publicUrl;

    @Bean
    public S3Client s3Client() {
        log.info("Khởi tạo S3Client kết nối Cloudflare R2: Endpoint={}, Bucket={}", endpoint, bucketName);

        // Chuẩn hóa Endpoint (bỏ suffix bucket nếu người dùng lỡ copy /ute-data)
        String cleanEndpoint = endpoint;
        if (cleanEndpoint.endsWith("/" + bucketName)) {
            cleanEndpoint = cleanEndpoint.substring(0, cleanEndpoint.length() - ("/" + bucketName).length());
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true) // Cloudflare R2 yêu cầu path-style access
                .build();

        return S3Client.builder()
                .endpointOverride(URI.create(cleanEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto")) // Cloudflare R2 dùng region 'auto'
                .serviceConfiguration(serviceConfiguration)
                .build();
    }
}
