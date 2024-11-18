package com.bhasaka.newsportal.core.servlets;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;

import javax.jcr.query.Query;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/newsportal/service/findresources")
public class FindResourceServlet extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resourceResolver = request.getResourceResolver();

        String query = "SELECT * \n" +
                "FROM [cq:Page] AS s \n" +
                "WHERE ISDESCENDANTNODE(s, '/content/newsportal') \n" +
                "AND s.[jcr:content/cq:lastReplicated] >= '2024-09-03T12:00:08.245+05:30'";

        Iterator<Resource> resources = resourceResolver.findResources(query, Query.JCR_SQL2);

        JsonArrayBuilder jsonPageArray = Json.createArrayBuilder();

        while (resources.hasNext()) {
            Resource resource = resources.next();
            Page page = resource.adaptTo(Page.class);

            if (page != null) {
                JsonObjectBuilder jsonObject = Json.createObjectBuilder();
                jsonObject.add("title", page.getTitle() != null ? page.getTitle() : "Untitled");
                jsonObject.add("path", page.getPath());

                jsonPageArray.add(jsonObject);
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonPageArray.build().toString());
    }
}
