package gov.nsf.psm.filestorage;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import gov.nsf.psm.foundation.exception.CommonUtilException;
import gov.nsf.psm.foundation.model.filestorage.DeleteFileResponse;
import gov.nsf.psm.foundation.model.filestorage.DeleteFileResponseWrapper;
import gov.nsf.psm.foundation.model.filestorage.GetFileResponse;
import gov.nsf.psm.foundation.model.filestorage.GetFileResponseWrapper;
import gov.nsf.psm.foundation.model.filestorage.UploadFileResponse;
import gov.nsf.psm.foundation.model.filestorage.UploadFileResponseWrapper;
import gov.nsf.psm.foundation.restclient.NsfRestTemplate;

public class FileStorageServiceClientImpl implements FileStorageServiceClient {

    private String serverURL;
    private String username;
    private String password;
    private boolean authenticationRequired;
    private int requestTimeout;
    private final String uploadFileURL = "/upload/file";
    private final String deleteFileURL = "/delete/file";
    private final String getFileURL = "/get/file";
    private final String qFileKey = "filekey";
    private final String filePartKey = "file";

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAuthenticationRequired() {
        return authenticationRequired;
    }

    public void setAuthenticationRequired(boolean authenticationRequired) {
        this.authenticationRequired = authenticationRequired;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
    
	private HttpEntity<String> getHttpEntity(boolean authRequired) {
		return authRequired ? NsfRestTemplate.createHttpEntityWithAuthentication(username, password) : null; 
	}
	
	private HttpHeaders addAuthToHeader(String uname, String pwd, HttpHeaders headers){
		String auth = uname + ":" + pwd;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", authHeader);
        return headers;
	}
    
    @Override
    public UploadFileResponse uploadFile(String fileKey,
            byte[] fileByteArray) throws CommonUtilException {
        try {
            RestTemplate fileStorageServiceClient = NsfRestTemplate.setupRestTemplate(authenticationRequired,
                    requestTimeout);
            MultiValueMap<String, Object> requestParts = new LinkedMultiValueMap<>();
            requestParts.add(filePartKey, fileByteArray);
            requestParts.add(qFileKey, fileKey);

            StringBuilder endpointUrl = new StringBuilder(getServerURL());
            endpointUrl.append(uploadFileURL);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if( authenticationRequired ) {
            	addAuthToHeader(username, password, headers);
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(
                    requestParts, headers);

            ResponseEntity<UploadFileResponseWrapper> response = null;

            LOGGER.debug("Executing POST request to upload file on: " + endpointUrl);      
            response = fileStorageServiceClient.exchange(endpointUrl.toString(), HttpMethod.POST, requestEntity,
                    UploadFileResponseWrapper.class);

            return response.getBody().getUploadFileResponse();
        } catch (RestClientException e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public DeleteFileResponse deleteFile(String fileKey)
            throws CommonUtilException {
        try {
            RestTemplate fileStorageServiceClient = NsfRestTemplate.setupRestTemplate(authenticationRequired,
                    requestTimeout);
            StringBuilder endpointUrl = new StringBuilder(getServerURL());
            endpointUrl.append(deleteFileURL);
            
            //Pass the file key as a query parameter
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpointUrl.toString());
            builder.queryParam(qFileKey, fileKey);
            
            ResponseEntity<DeleteFileResponseWrapper> response = null;
            HttpEntity<String> httpEntity = getHttpEntity(authenticationRequired);

            LOGGER.debug("Executing DELETE request to delete file on: " + endpointUrl); 
            response = fileStorageServiceClient.exchange(builder.build().encode().toUri(), HttpMethod.DELETE, httpEntity,
                    DeleteFileResponseWrapper.class);
            return response.getBody().getDeleteFileResponse();
        } catch (RestClientException e) {
            throw new CommonUtilException(e);
        }
    }

    @Override
    public GetFileResponse getFile(String fileKey)
            throws CommonUtilException {
        try {
            RestTemplate fileStorageServiceClient = NsfRestTemplate.setupRestTemplate(authenticationRequired,
                    requestTimeout);
            StringBuilder endpointUrl = new StringBuilder(getServerURL());
            endpointUrl.append(getFileURL);
            
            //Pass the file key as a query parameter
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(endpointUrl.toString());
            builder.queryParam(qFileKey, fileKey);
            
            ResponseEntity<GetFileResponseWrapper> response = null;
            HttpEntity<String> httpEntity = getHttpEntity(authenticationRequired);

            LOGGER.debug("Executing GET request to get file on: " + endpointUrl); 
            response = fileStorageServiceClient.exchange(builder.build().encode().toUri(), HttpMethod.GET, httpEntity,
                    GetFileResponseWrapper.class);
            return response.getBody().getGetFileResponse();
        } catch (RestClientException e) {
            throw new CommonUtilException(e);
        }
    }

}
