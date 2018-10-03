package gov.nsf.psm.filestorage.config;

import static com.amazonaws.services.s3.internal.Constants.MB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;

import gov.nsf.psm.filestorage.dao.FileStorageServiceDAO;
import gov.nsf.psm.filestorage.dao.FileStorageServiceDAOImpl;
import gov.nsf.psm.filestorage.service.FileStorageService;
import gov.nsf.psm.filestorage.service.FileStorageServiceImpl;

@Configuration
@EnableAspectJAutoProxy
public class FileStorageServiceConfig {

    @Value("${aws.accesskey}")
    private String awsAccessKey;

    @Value("${aws.secretkey}")
    private String awsSecretKey;

    @Value("${aws.s3.region}")
    private String awsS3Region;

    @Value("${aws.s3.bucketname}")
    private String awsBucketName;

    @Value("${psm.s3.max-retry}")
    private Integer maxRetry;

    @Value("${psm.s3.transfer.partsize}")
    private Long partSize;

    @Value("${psm.s3.multipart.max-filesize}")
    private Long maxFileSize;

    @Bean
    public AmazonS3 amazonS3() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setMaxErrorRetry(maxRetry);
        AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        AmazonS3Client client = new AmazonS3Client(credentials);
        client.setRegion(Region.getRegion(Regions.valueOf(awsS3Region)));
        return client;
    }

    @Bean
    public TransferManager transferManager() {
        TransferManager transferManager = new TransferManager(amazonS3());
        TransferManagerConfiguration tmConfig = new TransferManagerConfiguration();
        tmConfig.setMinimumUploadPartSize(partSize * MB);
        tmConfig.setMultipartUploadThreshold(maxFileSize * MB);
        transferManager.setConfiguration(tmConfig);
        return transferManager;
    }

    @Bean
    public FileStorageServiceDAO fileStorageServiceDAO() {
        FileStorageServiceDAO dao = new FileStorageServiceDAOImpl();
        dao.setBucketName(awsBucketName);
        return dao;
    }

    @Bean
    public FileStorageService fileStorageService() {
        return new FileStorageServiceImpl();
    }


    @Bean
    BasicAuthenticationFilter basicAuthFilter(AuthenticationManager authenticationManager, BasicAuthenticationEntryPoint basicAuthEntryPoint) {
      return new BasicAuthenticationFilter(authenticationManager, basicAuthEntryPoint());
    }
    
    @Bean
    BasicAuthenticationEntryPoint basicAuthEntryPoint() {
      BasicAuthenticationEntryPoint bauth = new BasicAuthenticationEntryPoint();
      bauth.setRealmName("file-storage-service");
      return bauth;
    }
    
}
