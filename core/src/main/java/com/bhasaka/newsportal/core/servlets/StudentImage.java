package com.bhasaka.newsportal.core.servlets;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component(service = { Servlet.class })
@SlingServletPaths(value = "/bin/pledge")
public class StudentImage extends SlingAllMethodsServlet {

    private static final Logger LOG = LoggerFactory.getLogger(StudentImage.class);

    private static final String ORIGINAL_ASSET_PATH = "/content/dam/newsportal/SchoolStiker.jpg";

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"This is a normal servlet with no S3 integration.\"}");
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        if (name == null || name.isEmpty()) {
            response.getWriter().write("{\"error\":\"Name is required.\"}");
            return;
        }

        try {
            // Load a local image file from DAM (or some other local source)
            InputStream assetStream = getClass().getResourceAsStream("content/dam/newsportal/SchoolStiker.jpg");
            if (assetStream == null) {
                throw new IOException("Original asset not found.");
            }
            byte[] imageBytes = IOUtils.toByteArray(assetStream);

            // Create a personalized image
            byte[] personalizedImageBytes = createPersonalizedImage(imageBytes, name);

            // Write the personalized image to the response
            ByteArrayInputStream imageInputStream = new ByteArrayInputStream(personalizedImageBytes);
            BufferedImage personalizedImage = ImageIO.read(imageInputStream);

            response.setContentType("image/jpg");
            String newAssetName = name + "SchoolStiker-jpg";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + newAssetName + "\"");
            ImageIO.write(personalizedImage, "jpg", response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            LOG.error("Error generating pledge image: ", e);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private byte[] createPersonalizedImage(byte[] originalImageBytes, String name) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageBytes);
        BufferedImage originalImage = ImageIO.read(inputStream);

        Graphics2D g = originalImage.createGraphics();

        // Customize the font and drawing
        Font font = new Font("Arial", Font.BOLD, 24);
        g.setFont(font);
        g.setColor(new Color(52, 64, 40));

        // Position the text on the image
        int nameX = 50; // Adjust X-coordinate as needed
        int nameY = 295; // Adjust Y-coordinate as needed
        g.drawString(name.toUpperCase(), nameX, nameY);

        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        return baos.toByteArray();
    }
}