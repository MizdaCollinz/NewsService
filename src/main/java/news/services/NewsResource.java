package news.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
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
			
			try{
				Category category = em.find(Category.class, article.getCategory().getCategoryID());
				if(category == null){
					throw new NoResultException();
				}
				logger.info("Category already existed in the database");
				
			} catch(NoResultException e){
				logger.info("New Category was created");
				em.persist(article.getCategory());
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
			Category cat = (Category) obj;
			
			logger.info("Attempting to persist category object");
			em.persist(cat);
			
			logger.info("Commit category persist");
			em.getTransaction().commit();
			em.close();
			
		}catch(JAXBException e){
			e.printStackTrace();
		}
		
		//RESPONSE TODO
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
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			IOUtils.copy(is, output);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		byte[] bytes = output.toByteArray();
		ByteArrayInputStream is1 = new ByteArrayInputStream(bytes);
		ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);
		
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
			Object obj = unmarshaller1.unmarshal(is1);
			logger.info("Identified as a Reporter object, successfully unmarshalled.");
			Reporter reporter = (Reporter) obj;
			logger.info("Attempting to persist reporter object");
			em.persist(reporter);
		}catch(JAXBException e){
			logger.info("Identified as a Reader object, attempting to unmarshal.");
			Object obj = null;
			try {
				obj = unmarshaller2.unmarshal(is2);
			} catch (JAXBException e1) {
				e1.printStackTrace();
			}
			Reader reader = (Reader) obj;
			logger.info("Attempting to persist reader object");
			em.persist(reader);
		}
			
			logger.info("Commit creation of user");
			em.getTransaction().commit();
			em.close();
			
		//RESPONSE TODO
		return null;
		
	}
	
	
	
	@GET
	@Path("/articles/{articleID}")
	public StreamingOutput retrieveArticle(@PathParam("articleID") int articleID){ //Using Path Params
		logger.info("Retrieve article of ID: "+ articleID);
		
		final String articleXml;
		
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();
		
		logger.info("Attempting to fetch Article");
		//Use of entity graph to eagerly fetch the referenced Reporter object
		EntityGraph<?> graph = em.getEntityGraph("graph.Article.writer");
		Map<String, Object> hints = new HashMap<String, Object>();
		hints.put("javax.persistence.fetchgraph", graph);
		Article article = em.find(Article.class,articleID,hints);
		
		
				
		em.getTransaction().commit();
		
		if (article == null){
			logger.info("No Article found by that id");
			//TODO throw some exception
		} else{
			logger.info("Marshal and return Article object");
			try {
				JAXBContext jaxb = JAXBContext.newInstance(Article.class);
				Marshaller marshaller = jaxb.createMarshaller();
				StringWriter writer = new StringWriter();
				marshaller.marshal(article,writer);
				articleXml = writer.toString();
								
				em.close();

				StreamingOutput streamOutput = new StreamingOutput(){
					public void write(OutputStream outputStream){
						PrintStream writer = new PrintStream(outputStream);
						writer.println(articleXml);
					}
				};
				
				return streamOutput;	
							
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
				
		return null;
		
	}
	
	@GET
	@Path("/articles")
	public StreamingOutput getArticleType(@MatrixParam("category") int categoryID){ //Using Matrix Paramters
		logger.info("Retrieve articles from category of identity: " + categoryID);
		
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();
		
		Category cat = em.find(Category.class,categoryID);
		final List<Article> articles = cat.getArticles();
		articles.size();
		
		StreamingOutput streamOutput = new StreamingOutput(){
			public void write(OutputStream outputStream){
				PrintStream writer = new PrintStream(outputStream);
				try {
					JAXBContext context = JAXBContext.newInstance(Article.class);
					Marshaller marshaller = context.createMarshaller();
					for(Article article : articles){
						StringWriter articleWriter = new StringWriter();
						marshaller.marshal(article, articleWriter);
						writer.println(articleWriter.toString());
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}
				
				
				
			}
		};
		
		em.getTransaction().commit();
		em.close();
		return streamOutput;
		
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
