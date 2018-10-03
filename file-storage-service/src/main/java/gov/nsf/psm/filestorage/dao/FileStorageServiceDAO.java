package gov.nsf.psm.filestorage.dao;

import java.io.InputStream;

public interface FileStorageServiceDAO {

    boolean fileExists(String filePath);
    public boolean uploadFile(InputStream inputStream, String filePath) throws InterruptedException;
    public boolean deleteFile(String filePath) ;
    public void setBucketName(String bucketName);
    public byte[] getFile(String filePath);
}
