package com.bhasaka.newsportal.core.models;

import com.bhasaka.newsportal.core.services.ServiceResourceResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Model(
        adaptables = {Resource.class, SlingHttpServletRequest.class},
        resourceType = "newsportal/components/studentstiker",
        defaultInjectionStrategy = DefaultInjectionStrategy.REQUIRED
)
public class StudentModel {

    private static final Logger log = LoggerFactory.getLogger(StudentModel.class);

    @Reference
    private ServiceResourceResolver serviceResourceResolver;

    @ValueMapValue
    private String studentName;

    @ValueMapValue
    private String schoolName;

    private String newImagePath = null;

    private static final String SOURCE_IMAGE_PATH = "/content/dam/newsportal/SchoolStiker.jpg/jcr:content/renditions/original";
    private static final String IMAGE_FOLDER = "/content/dam/newsportal/images";

    @PostConstruct
    public void init() {
        ResourceResolver resourceResolver = null;
        try {
            // Obtain ResourceResolver from the service
            resourceResolver = serviceResourceResolver.getResourceResolver();
            if (resourceResolver == null) {
                log.error("ServiceResourceResolver returned a null ResourceResolver.");
                return;
            }

            // Validate source image resource
            Resource imageResource = resourceResolver.getResource(SOURCE_IMAGE_PATH);
            if (imageResource == null) {
                log.error("Source image resource not found at: {}", SOURCE_IMAGE_PATH);
                return;
            }

            InputStream imageStream = imageResource.adaptTo(InputStream.class);
            if (imageStream == null) {
                log.error("Unable to adapt source image resource to InputStream.");
                return;
            }

            // Default values for student and school names
            studentName = StringUtils.defaultIfEmpty(studentName, "Default Name");
            schoolName = StringUtils.defaultIfEmpty(schoolName, "Default School");

            // Process image and add text
            BufferedImage image = ImageIO.read(imageStream);
            Graphics2D graphics = image.createGraphics();
            graphics.setFont(new Font("Arial", Font.BOLD, 30));
            graphics.setColor(Color.BLACK);
            graphics.drawString("Name: " + studentName.toUpperCase(), 10, 40);
            graphics.drawString("School: " + schoolName.toUpperCase(), 10, 80);
            graphics.dispose();

            // Convert the modified image to an InputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            InputStream modifiedImageStream = new ByteArrayInputStream(outputStream.toByteArray());

            // Validate parent folder
            Resource parentResource = resourceResolver.getResource(IMAGE_FOLDER);
            if (parentResource == null) {
                log.error("Parent folder not found at: {}", IMAGE_FOLDER);
                return;
            }

            // Adapt ResourceResolver to JCR session
            Session session = resourceResolver.adaptTo(Session.class);
            if (session == null) {
                log.error("JCR Session could not be adapted from ResourceResolver.");
                return;
            }

            // Create binary from modified image stream
            Binary binary = session.getValueFactory().createBinary(modifiedImageStream);
            if (binary == null) {
                log.error("Binary could not be created for the image.");
                return;
            }

            // Create the new image resource in the repository
            String modifiedImageName = "modified-" + studentName.replaceAll("\\s", "_") + ".png";
            Map<String, Object> fileProperties = new HashMap<>();
            fileProperties.put("jcr:primaryType", "nt:file");
            Resource modifiedImageResource = resourceResolver.create(parentResource, modifiedImageName, fileProperties);

            // Add content node to the new image resource
            Map<String, Object> contentProperties = new HashMap<>();
            contentProperties.put("jcr:primaryType", "nt:resource");
            contentProperties.put("jcr:data", binary);
            contentProperties.put("jcr:mimeType", "image/png");
            resourceResolver.create(modifiedImageResource, "jcr:content", contentProperties);

            // Commit the changes
            resourceResolver.commit();
            newImagePath = modifiedImageResource.getPath();
            log.info("New image created successfully at path: {}", newImagePath);

        } catch (IOException e) {
            log.error("IOException occurred during image processing: ", e);
        } catch (RepositoryException e) {
            log.error("RepositoryException occurred while saving the image: ", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred: ", e);
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

    public String getImageStyle() {
        return "border: 3px solid #FF5733; border-radius: 12px; box-shadow: 5px 5px 15px rgba(0, 0, 0, 0.3); max-width: 500px; height: auto;";
    }

    public String getNewImagePath() {
        return newImagePath;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getSchoolName() {
        return schoolName;
    }
}
