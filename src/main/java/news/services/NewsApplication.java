package news.services;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;


@ApplicationPath("/services")
public class NewsApplication extends Application {
	
	
	
	
	public NewsApplication(){
	}
	
	@Override
	   public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<Object>();  
		
		singletons.add(new NewsAsyncResource());
		singletons.add(PersistenceManager.instance());
		return singletons;
	   }
	   
	   @Override
	   public Set<Class<?>> getClasses() {
		   Set<Class<?>> classes = new HashSet<Class<?>>();
		   
		   classes.add(NewsResource.class);
		   return classes;
	   }
}
