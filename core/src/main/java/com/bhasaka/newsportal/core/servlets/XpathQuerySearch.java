package com.bhasaka.newsportal.core.servlets;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/newsportal/service/xpath")
public class XpathQuerySearch extends SlingSafeMethodsServlet
{
    @Reference
    QueryBuilder queryBuilder;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws  IOException
    {
        ResourceResolver resourceResolver = request.getResourceResolver();

        Map<String, String> queryMap = new HashMap<>();

        // Set parameters
        queryMap.put("path", "/content/newsportal");
        queryMap.put("type", "cq:Page");
        queryMap.put("group.1_property", "jcr:content/cq:template");
        queryMap.put("group.1_property.value", "/conf/newsportal/settings/wcm/templates/article-template");
        queryMap.put("group.2_property", "jcr:content/sling:resourceType");
        queryMap.put("group.2_property.value", "newsportal/components/article-page");
        queryMap.put("p.limit", "-1");

        Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), resourceResolver.adaptTo(Session.class));
        SearchResult result = query.getResult();
        List<Hit> hits = result.getHits();

        JsonArrayBuilder jsonPageArray = Json.createArrayBuilder();

        for(Hit hit : hits ){
            try {
                Page page  = hit.getResource().adaptTo(Page.class);
                JsonObjectBuilder jsonPage = Json.createObjectBuilder();

                jsonPage.add("Title", Objects.requireNonNull(page).getTitle());
                jsonPage.add("Path", page.getPath());

                jsonPageArray.add(jsonPage);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(jsonPageArray.build().toString());
    }
}
