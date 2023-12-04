package org.iproduct.polling;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
  @Override
  public Response toResponse(WebApplicationException ex) {
    return Response.status(ex.getResponse().getStatus()).
      entity(ex.getMessage()).
      type("text/plain").
      build();
  }
}
