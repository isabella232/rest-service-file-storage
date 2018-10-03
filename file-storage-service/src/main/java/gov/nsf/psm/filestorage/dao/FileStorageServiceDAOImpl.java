package gov.nsf.psm.filestorage.dao;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SSEAlgorithm;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.IOUtils;

public class FileStorageServiceDAOImpl implements FileStorageServiceDAO {

    @Autowired
    private AmazonS3 amazonS3Client;

    @Autowired
    private TransferManager transferManager;

    private String bucketName;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass()); 

    @Override
    public boolean fileExists(String filePath) {
        boolean fileExists = false;

        // check if file exists
        try {
            fileExists = amazonS3Client.doesObjectExist(bucketName, filePath);
            LOGGER.debug("Check if object \"" + bucketName + "/" + filePath + "\" exists: " + fileExists);
        } catch (AmazonClientException e) {
            LOGGER.error("An issue occurred when checking if \"" + bucketName + "/" + filePath + "\" exists", e);
        }

        return fileExists;
    }

    @Override
    public boolean uploadFile(InputStream inputStream, String filePath) throws InterruptedException {
        ObjectMetadata metadata = new ObjectMetadata();
        // set metadata

        // enable server side encryption
        metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);  
        
        // upload the file
        try {
            Upload upload = transferManager.upload(new PutObjectRequest(bucketName, filePath, inputStream, metadata));
            upload.waitForCompletion();
            LOGGER.debug("\"" + bucketName + "/" + filePath + "\" was successfully uploaded.");
            return true;
        } catch (AmazonClientException e) {
            LOGGER.error("Unable to upload \"" + bucketName + "/" + filePath + "\", upload was aborted.", e);
        }
        return false;

    }

    @Override
    public boolean deleteFile(String filePath) {
        // delete existing object in the bucket
        try {
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, filePath));
            LOGGER.debug("Existing file {} was successfully deleted.", filePath);
            return true;
        } catch (AmazonClientException e) {
            LOGGER.error("Unable to delete \"" + bucketName + "/" + filePath + "\"", e);
        }
        return false;
    }

    @Override
    public byte[] getFile(String filePath) {
        ByteArrayOutputStream output = null;

        try {
            LOGGER.debug("Retrieving the following file : " + filePath);
            S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, filePath));
            InputStream input = object.getObjectContent();
            output = new ByteArrayOutputStream();
            IOUtils.copy(input, output);
            LOGGER.trace(output.toString());
            return output.toByteArray();
        } catch (AmazonServiceException e) {
            LOGGER.error("The request to AWS was rejected.", e);
        } catch (AmazonClientException e) {
            LOGGER.error("An AWS internal error occurred while trying to retrieve the file.", e);
        } catch (IOException e) {
            LOGGER.error("An IOException occurred while trying to retrieve the file.", e);
        } catch (NullPointerException e) {
            LOGGER.error("An NullPointerException occurred while trying to retrieve the file.", e);
        }

        return new byte[0];
    }

    @Override
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

}
