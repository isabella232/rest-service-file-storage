package gov.nsf.psm.filestorage;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class FileStorageServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(FileStorageServiceApplication.class);
    }
    
    public static void main(String[] args) {
        setEmbeddedContainerEnvironmentProperties();
        SpringApplication.run(FileStorageServiceApplication.class, args);
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        setExternalContainerEnvironmentProperties();
        super.onStartup(servletContext);
    }

    private static void setEmbeddedContainerEnvironmentProperties() {
        setEnvironmentProperties();
        System.setProperty("server.context-path", "/file-storage-service");
    }

    private static void setExternalContainerEnvironmentProperties() {
        setEnvironmentProperties();
    }

    private static void setEnvironmentProperties() {
        System.setProperty("spring.config.name", "file-storage-service");
    }
}
