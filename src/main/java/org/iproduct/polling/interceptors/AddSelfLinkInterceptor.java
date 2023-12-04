package org.iproduct.polling.interceptors;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Provider
@SelfLinked
public class AddSelfLinkInterceptor implements WriterInterceptor {
    @Context
    UriInfo uriInfo;

    @Override
    public void aroundWriteTo(WriterInterceptorContext responseContext)
            throws WebApplicationException, IOException {
        List<Object> links = responseContext.getHeaders().get("Link");
        if(links == null)
            links = new ArrayList<>();
        links.add("<" + uriInfo.getAbsolutePath() + ">; rel=self");
        responseContext.getHeaders().put("Link", links);
        responseContext.proceed();
    }

}
