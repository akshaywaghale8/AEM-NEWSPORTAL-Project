package com.bhasaka.newsportal.core.services;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

@Component(service = ServiceResourceResolver.class)
public class ServiceResourceResolver
{
    @Reference
    ResourceResolverFactory resourceResolverFactory;

    public ResourceResolver getResourceResolver() {
        ResourceResolver resourceResolver;
        Map<String,Object> props=new HashMap<>();
        props.put((ResourceResolverFactory.SUBSERVICE),"npSubService");
        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(props);
            return resourceResolver;
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }
}
