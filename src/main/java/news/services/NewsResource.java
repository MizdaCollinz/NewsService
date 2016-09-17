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
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;
import news.domain.Category;
import news.domain.Reader;
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
	@Consumes("application/xml")
	public Response addCategory(InputStream is){
		
		logger.info("Calling POST method to create a new category");
		
		try{
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();
			
			//Produce unmarshaller using JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(Category.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//Retrieve article from input stream
			Object obj = unmarshaller.unmarshal(is);
			
			
			em.getTransaction().commit();
			em.close();
			
		}catch(JAXBException e){
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	@POST 
	@Path("/users") //separate Reporters from Readers
	@Consumes("application/xml")
	public Response signupUser(InputStream is){
		
		logger.info("Calling POST method to register a new user");
		JAXBContext jaxbContext1;
		JAXBContext jaxbContext2;
		Unmarshaller unmarshaller1 = null;
		Unmarshaller unmarshaller2 = null;
		EntityManager em = PersistenceManager.instance().createEntityManager();
		try{
			em.getTransaction().begin();
			
			//Produce unmarshaller using JAXB
			jaxbContext1 = JAXBContext.newInstance(Reporter.class);
			jaxbContext2 = JAXBContext.newInstance(Reader.class);
			
			unmarshaller1 = jaxbContext1.createUnmarshaller();
			unmarshaller2 = jaxbContext2.createUnmarshaller();
		}catch(JAXBException e){
			e.printStackTrace();
		}
			//Retrieve article from input stream
		try{
			Object obj = unmarshaller1.unmarshal(is);
			logger.info("Identified as a Reporter object, successfully unmarshalled.");
			Reporter reporter = (Reporter) obj;
			em.persist(reporter);
		}catch(JAXBException e){
			logger.info("Identified as a Reader object, attempting to unmarshal.");
			Object obj = null;
			try {
				obj = unmarshaller2.unmarshal(is);
			} catch (JAXBException e1) {
				e1.printStackTrace();
			}
			Reader reader = (Reader) obj;
			em.persist(reader);
		}
			
			em.getTransaction().commit();
			em.close();
			
		
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
	@Path("/articles/subscribed")
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
