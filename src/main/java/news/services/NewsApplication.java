package news.services;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/services")
public class NewsApplication extends Application {
	
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> classes = new HashSet<Class<?>>();
	
	public NewsApplication(){
		singletons.add(new NewsResource());
		singletons.add(PersistenceManager.instance());
		
	}
	
	@Override
	   public Set<Object> getSingletons() {
	      return singletons;
	   }
	   
	   @Override
	   public Set<Class<?>> getClasses() {
		   return classes;
	   }
}
