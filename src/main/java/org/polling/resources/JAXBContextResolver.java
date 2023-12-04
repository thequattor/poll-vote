package org.polling.resources;


import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBContext;

@Provider
public class JAXBContextResolver implements ContextResolver<JAXBContext> {

	private JAXBContext context;
	private Class<?>[] types = 
            { 
//                Poll.class, PollRepresentationCustomLinks.class, 
//                PollRepresentationDocumentLinks.class,
//                PollRepresentationHeaderLinks.class,
//                Alternative.class, Vote.class, 
                };

	public JAXBContextResolver() throws Exception {
//          this.context = 
//   	  new JSONJAXBContext(JSONConfiguration.natural().rootUnwrapping(true).build(), types); 
     }

        @Override
	public JAXBContext getContext(Class<?> objectType) {
		for (Class<?> type : types) {
			if(type.equals(objectType)) {
				return context;
			}
		}
		return null;
	}
}