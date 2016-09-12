package news.services;

import java.io.InputStream;

import javax.persistence.EntityManager;
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

@Path("/news")
public class NewsResource {
	//Logger
	private static Logger logger = LoggerFactory.getLogger(NewsResource.class);
	
	@POST
	@Path("/articles")
	@Produces("application/xml")
	public Response postArticle(InputStream is) {
			logger.info("Calling POST method to create an article");
		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			//Produce unmarshaller using JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//Retrieve article from input stream
			Object obj = unmarshaller.unmarshal(is);
			Article article = (Article) obj;
			
			logger.info("Attempting to persist reporter object");
			//Save the reporter to the database(Persist) before saving article
			em.persist(article.getWriter());
			
			//Commit the transaction and close the entity manager
			em.getTransaction().commit();
			logger.info("Commit reporter creation transaction");
			
			//Begin second transaction to create article
			em.getTransaction().begin();
			logger.info("Attempting to persist article object");
			em.persist(article);
			
			em.getTransaction().commit();
			logger.info("Commit article creation transaction");
			
			em.close();
			
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
