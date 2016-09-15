package news.services;

import java.io.InputStream;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;
import news.domain.Reporter;

@Path("/news")
public class NewsResource {
	//Logger
	private static Logger logger = LoggerFactory.getLogger(NewsResource.class);
	
	@POST
	@Path("/articles")
	@Consumes("application/xml")
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
			
			logger.info("Checking if Reporter object saved yet");
			//Save the reporter to the database(Persist) before saving article
			//Query for Writer
			try{
			Reporter reporter = em.find(Reporter.class,article.getWriter().getUsername());
			if(reporter == null){
				throw new NoResultException();
			}
			
			logger.info("Reporter already existed in the database");
			} catch(NoResultException e){// writer doesn't exist
				logger.info("New Reporter was created");
				em.persist(article.getWriter());
			}
			
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
		
		//Create reponse message
		//TODO
		return null;
	}
	
	@POST
	@Path("/categories") // Initialise a category
	public Response addCategory(InputStream is){
		return null;
		
	}
	
	@POST 
	@Path("/users") //separate Reporters from Readers
	public Response signupUser(InputStream is){
		return null;
		
	}
	
	
	
	@GET
	@Path("/articles/{articleID}")
	public Response retrieveArticle(@PathParam("articleID") int articleID){ //Using Path Params
		return null;
		
	}
	
	@GET
	@Path("/articles")
	public Response getArticleType(@Context UriInfo info){ //Using Query Params - CHANGE TO MATRIX?
		return null;
		
	}
	
	@GET
	@Path("/articles")
	public Response getSubscribedArticles(@CookieParam("username") String username){ //Using Cookies
		return null;
	}
	
	@GET
	@Path("/categories")
	public Response getCategories(){
		return null;
	}
	
	@DELETE
	@Path("/articles/{articleID}")
	public void deleteArticle(@PathParam("articleID") int articleID){ //Delete Using Path Params
		//TODO
	}
	

	

	
	
	
	
}
