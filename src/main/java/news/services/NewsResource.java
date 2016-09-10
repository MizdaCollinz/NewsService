package news.services;

import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;

public class NewsResource {
	//Logger
	private static Logger logger = LoggerFactory.getLogger(NewsResource.class);
	
	@POST
	@Produces("application/xml")
	public Response postArticle(InputStream is) {
		try {
			//Produce unmarshaller using JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//Retrieve article from input stream
			Object obj = unmarshaller.unmarshal(is);
			Article article = (Article) obj;
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@DELETE
	@Path("{username}")
	public void deleteReporter(@PathParam("username") String username){
		//Todo
	}
	
	
	
	
}
