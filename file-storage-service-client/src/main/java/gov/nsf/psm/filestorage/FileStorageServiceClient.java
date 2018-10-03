package gov.nsf.psm.filestorage;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.filestorage.DeleteFileResponse;
import gov.nsf.psm.foundation.model.filestorage.GetFileResponse;
import gov.nsf.psm.foundation.model.filestorage.UploadFileResponse;

public interface FileStorageServiceClient {

    
    public UploadFileResponse uploadFile(String fileKey, byte[] fileByteArray) throws CommonUtilException;

	public DeleteFileResponse deleteFile(String fileKey) throws CommonUtilException;

	public GetFileResponse getFile(String fileKey) throws CommonUtilException;
 
}
