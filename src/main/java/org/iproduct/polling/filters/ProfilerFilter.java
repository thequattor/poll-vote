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
@Priority(500)
@Profiled
public class ProfilerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Logger.getLogger("Profiling Resource Request >>>>> ")
            .log(Level.INFO, "Profiling Request >>>>>>> Method: " + requestContext.getMethod()
                + ", URI: " + requestContext.getUriInfo().getAbsolutePath()
            );
            requestContext.setProperty("startTime", (Long)System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Logger.getLogger("Profiling Resource Request >>>>> ")
            .log(Level.INFO, "Profiling Request >>>>>>> Method: " + requestContext.getMethod()
                + ", URI: " + requestContext.getUriInfo().getAbsolutePath()
                + ", Execution Time: " 
                + (System.currentTimeMillis() - ((Long)requestContext.getProperty("startTime"))) 
                + " ms" 
                );
    }

}
