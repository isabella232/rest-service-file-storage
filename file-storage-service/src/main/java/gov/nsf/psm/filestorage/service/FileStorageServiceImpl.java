package gov.nsf.psm.filestorage.service;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nsf.psm.filestorage.common.constants.Constants;
import gov.nsf.psm.filestorage.dao.FileStorageServiceDAO;
import gov.nsf.psm.foundation.exception.CommonUtilException;

public class FileStorageServiceImpl implements FileStorageService {
    
    @Autowired
    FileStorageServiceDAO fileStorageServiceDao;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass()); 

    @Override
    public boolean uploadFile(InputStream inputStream, String filePath) throws CommonUtilException {
        
        boolean uploadSuccessful = false;
        boolean fileExists = false;
        
        try {
            //check if file exists
            fileExists = fileStorageServiceDao.fileExists(filePath);
            
            //if file exists, delete it before upload
            if(fileExists) {
                fileStorageServiceDao.deleteFile(filePath);
            }
            
            //upload file
            uploadSuccessful =  fileStorageServiceDao.uploadFile(inputStream, filePath);
            
        } catch (InterruptedException e) {
        	LOGGER.info("File Path: " + filePath);
            throw new CommonUtilException(Constants.UPLOAD_FILE_SERVICE_EXCEPTION_MESSAGE, e);
        }
        
        return uploadSuccessful;
    }
    
    @Override
    public boolean deleteFile(String filePath) throws CommonUtilException {
        if(fileStorageServiceDao.fileExists(filePath)) {
        	try{
        		return fileStorageServiceDao.deleteFile(filePath);
        	} catch(Exception e) {
        		LOGGER.info("File Path: " + filePath);
        		throw new CommonUtilException(Constants.DELETE_FILE_SERVICE_EXCEPTION_MESSAGE, e);
        	}
        }
        return false;
    }
    
    @Override
    public byte[] getFile(String filePath) throws CommonUtilException {
        byte[] fileByteArray = null;
        try {
        	if(fileStorageServiceDao.fileExists(filePath)) {
                fileByteArray = fileStorageServiceDao.getFile(filePath);
            } 
            if(fileByteArray != null && fileByteArray.length != 0){
                return fileByteArray;
            }
            else {
                LOGGER.info("No file found at path \"" + filePath);
                throw new CommonUtilException(Constants.GET_FILE_SERVICE_EXCEPTION_MESSAGE);
            }
        } catch(Exception e){
        	LOGGER.info("No file found at path \"" + filePath);
            throw new CommonUtilException(Constants.GET_FILE_SERVICE_EXCEPTION_MESSAGE, e);
        }  
    }

}
