package news.services;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;
import news.domain.Category;
import news.domain.Reporter;

@Path("/newsA")

public class NewsAsyncResource {
	protected HashMap<Integer, List<AsyncResponse>> responseMap = new HashMap<Integer, List<AsyncResponse>>();
	private static Logger logger = LoggerFactory.getLogger(NewsAsyncResource.class);
	
	@POST
	@Path("/articles")
	@Consumes("application/xml")
	public Response postArticle(InputStream is) {
		logger.info("Calling POST method to create an article");

		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();

			// Produce unmarshaller using JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			final Marshaller marshaller = jaxbContext.createMarshaller();
			// Retrieve article from input stream
			Object obj = unmarshaller.unmarshal(is);
			final Article article = (Article) obj;

			logger.info("Checking if Reporter object saved yet");
			// Save the reporter to the database(Persist) before saving article
			// Query for Writer
			try {
				Reporter reporter = em.find(Reporter.class, article.getWriter().getUsername());
				if (reporter == null) {
					throw new NoResultException();
				}

				logger.info("Reporter already existed in the database");
			} catch (NoResultException e) {// writer doesn't exist
				logger.info("New Reporter was created");
				em.persist(article.getWriter());
			}

			try {
				Category category = em.find(Category.class, article.getCategory().getCategoryID());
				if (category == null) {
					throw new NoResultException();
				}
				logger.info("Category already existed in the database");

			} catch (NoResultException e) {
				logger.info("New Category was created");
				em.persist(article.getCategory());
			}

			// Commit the transaction and close the entity manager
			em.getTransaction().commit();
			logger.info("Commit reporter creation transaction");

			// Begin second transaction to create article
			em.getTransaction().begin();
			logger.info("Attempting to persist article object");
			em.persist(article);

			em.getTransaction().commit();
			logger.info("Commit article creation transaction");

			logger.info("Notify waiting subscribers that an article has been submitted");

			new Thread() {
				public void run() {
					try {
						int catID = article.getCategory().getCategoryID();
												
						List<AsyncResponse> responseList = responseMap.get(catID);
						if (responseList != null) {
							for (AsyncResponse resp : responseList) {
								StringWriter sWriter = new StringWriter();
								marshaller.marshal(article, sWriter);
								resp.resume(sWriter.toString());
							}
							responseList.clear();
						}
					} catch (JAXBException e) {
						e.printStackTrace();
					}
				}
			}.start();

			em.close();
			return Response.created(URI.create("/news/articles/" + article.getId())).build();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		// Create reponse message

		return null;
	}
	
	@GET
	@Path("/subscribe/{categoryid}")
	public void subscribe(@Suspended AsyncResponse response, @PathParam("categoryid") int categoryId) {
		logger.info("Adding client to wait list for a particular category: " + categoryId);
		// Retrieve response list for specified category
		List<AsyncResponse> responses = responseMap.get(categoryId);
		// Create list if it doesnt exist
		while (responses == null) {
			responseMap.put((Integer)categoryId,new ArrayList<AsyncResponse>());
			responses = responseMap.get(categoryId);
		}
		// Add response to subscription list
		responses.add(response);		
	}
	
}
