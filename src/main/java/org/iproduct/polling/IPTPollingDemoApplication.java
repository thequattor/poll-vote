package org.iproduct.polling;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class IPTPollingDemoApplication extends Application {

}

//public class IPTPollingDemoApplication extends Application {
//	@Override
//	public Set<Class<?>> getClasses() {
//	    final Set<Class<?>> classes = new HashSet<>();
//	    // register root resources
//	    classes.add(PollsResource.class);
//	    classes.add(AlternativesResource.class);
//	    classes.add(VotesResource.class);
//	    classes.add(CustomBinder.class);
//	    return classes;
//	}     
//}
