package org.iproduct.polling.filters;


import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
//@PreMatching
@Priority(1000)
@Logged
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Logger.getLogger("Logging Resource Request >>>>> ")
            .log(Level.INFO, "Method: " + requestContext.getMethod()
                + ", URI: " + requestContext.getUriInfo().getAbsolutePath()
                + ", Headers: " + requestContext.getHeaders()
                );
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Logger.getLogger("Logging Service Response >>>>> ")
            .log(Level.INFO, "Method: " + requestContext.getMethod()
                + ", URI: " + requestContext.getUriInfo().getAbsolutePath()
                + ", Status Code: " + responseContext.getStatus()
                + ", MIME Type: " + responseContext.getHeaders()
                + ", Entity Body: " + responseContext.getEntity()
                );
    }

}
