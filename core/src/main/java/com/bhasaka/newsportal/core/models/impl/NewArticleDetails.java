package com.bhasaka.newsportal.core.models.impl;

import com.adobe.cq.export.json.ExporterConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import lombok.Getter;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.api.resource.Resource;
import javax.annotation.PostConstruct;


@Model(
        adaptables =  Resource.class,
        resourceType = "newsportal/components/articledetails",
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(name = ExporterConstants.SLING_MODEL_EXPORTER_NAME, extensions = ExporterConstants.SLING_MODEL_EXTENSION)
@Getter
public class NewArticleDetails {

    @Self
    private Resource resource;

    @SlingObject
    private PageManager pageManager;

    @ValueMapValue
    private String title;

    @ValueMapValue
    private String articleDesc;

    @ValueMapValue
    private String imagePath;

    @ValueMapValue
    private String imageAltText;

    @PostConstruct
    public void init(){
        Page currentPage = pageManager.getContainingPage(resource);
    }
}
