package gov.nsf.psm.filestorage.service;

import java.io.InputStream;

import gov.nsf.psm.foundation.exception.CommonUtilException;

public interface FileStorageService {

	public boolean uploadFile(InputStream inputStream, String filePath) throws CommonUtilException;

	public boolean deleteFile(String filePath) throws CommonUtilException;

    public byte[] getFile(String filePath) throws CommonUtilException;
        
}
