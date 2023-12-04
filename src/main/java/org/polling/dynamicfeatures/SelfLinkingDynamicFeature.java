package org.polling.dynamicfeatures;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

import org.polling.interceptors.AddSelfLinkInterceptor;

@Provider
public class SelfLinkingDynamicFeature implements DynamicFeature {
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        if (resourceInfo.getResourceMethod().isAnnotationPresent(GET.class) || resourceInfo.getResourceMethod().isAnnotationPresent(POST.class)) {
            context.register(AddSelfLinkInterceptor.class);
        }
    }
}
