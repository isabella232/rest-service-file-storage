package gov.nsf.psm.filestorage.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.apache.catalina.core.ApplicationPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import gov.nsf.psm.filestorage.common.constants.Constants;
import gov.nsf.psm.filestorage.service.FileStorageService;
import gov.nsf.psm.foundation.controller.PsmBaseController;
import gov.nsf.psm.foundation.ember.model.EmberModel;
import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.filestorage.DeleteFileResponse;
import gov.nsf.psm.foundation.model.filestorage.GetFileResponse;
import gov.nsf.psm.foundation.model.filestorage.UploadFileResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@CrossOrigin
@RestController
@RequestMapping(path = "/api/v1")
@ApiResponses(value = { @ApiResponse(code = 404, message = "Resource not found"),
        @ApiResponse(code = 500, message = "Internal server error") })
public class FileStorageServiceController extends PsmBaseController {

    @Autowired
    FileStorageService fileStorageService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private final String FILE_KEY = "filekey";

    @ApiOperation(value = "Upload a proposal section file", notes = "This API takes a file and uploads it to Amazon S3 storage", response = UploadFileResponse.class)
    @RequestMapping(path = "/upload/file", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel uploadFile(@ApiParam(value = "multipartRequest") MultipartHttpServletRequest request) throws CommonUtilException {

        boolean fileUploadSuccess = false;
        UploadFileResponse response = new UploadFileResponse();
       
        // Insert authentication/authorization check here

        try {
            InputStream inputStream = ((ApplicationPart) request.getParts().toArray()[0]).getInputStream();
        	String fileKey = request.getParameter(FILE_KEY);
           
            fileUploadSuccess = fileStorageService.uploadFile(inputStream, fileKey);
            
            response.setUploadSuccessful(fileUploadSuccess);
            response.setFilePath(fileKey);
        } catch (IOException e) {
            throw new CommonUtilException(Constants.UPLOAD_FILE_SERVICE_IOEXCEPTION_MESSAGE, e);
        } catch (ServletException e) {
            throw new CommonUtilException(Constants.UPLOAD_FILE_SERVICE_SERVLETEXCEPTION_MESSAGE, e);
        }
        
        LOGGER.trace(response.toString());
        return new EmberModel.Builder<>(UploadFileResponse.getClassCamelCaseName(), response).build();
    }

    @ApiOperation(value = "Delete a proposal section file", notes = "This API allows one to delete a file on Amazon S3 storage", response = DeleteFileResponse.class)
    @RequestMapping(path = "/delete/file", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel deleteFile(@ApiParam(FILE_KEY) @RequestParam(FILE_KEY) String fileKey) throws CommonUtilException {

        DeleteFileResponse response = new DeleteFileResponse();
        boolean deleteSuccessful = false;
        // Insert authentication/authorization check here
        // send to service layer
        deleteSuccessful = fileStorageService.deleteFile(fileKey);
        response.setDeleteSuccessful(deleteSuccessful);
        
        LOGGER.trace(response.toString());
        return new EmberModel.Builder<>(DeleteFileResponse.getClassCamelCaseName(), response).build();
    }

    @ApiOperation(value = "Get a proposal section file", notes = "This API retrieves a file from Amazon S3 storage", response = GetFileResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful retrieval of file", response = UploadFileResponse.class) })
    @RequestMapping(path = "/get/file", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EmberModel getFile(@ApiParam(FILE_KEY) @RequestParam(FILE_KEY) String fileKey) throws CommonUtilException {

        GetFileResponse response = new GetFileResponse();
        byte[] fileByteArray;
        // Insert authentication/authorization check here
        // send to service layer
		fileByteArray = fileStorageService.getFile(fileKey);

		response.setFile(fileByteArray);
		response.setFilePath(fileKey);
		response.setGetSuccessful(true);
		
		LOGGER.trace(response.toString());
		return new EmberModel.Builder<>(GetFileResponse.getClassCamelCaseName(), response).build();
    }
}
