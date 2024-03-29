package news.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.soap.AddressingFeature.Responses;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;
import news.domain.Category;
import news.domain.Reader;
import news.domain.Reporter;
import news.domain.User;

@Path("/news")
public class NewsResource {
	// Logger
	private static Logger logger = LoggerFactory.getLogger(NewsResource.class);
	
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

			em.close();
			return Response.created(URI.create("/news/articles/" + article.getId())).build();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		// Create reponse message

		return null;
	}

	@POST
	@Path("/categories") // Initialise a category
	@Consumes("application/xml")
	public Response addCategory(InputStream is) {

		logger.info("Calling POST method to create a new category");

		try {
			EntityManager em = PersistenceManager.instance().createEntityManager();
			em.getTransaction().begin();

			// Produce unmarshaller using JAXB
			JAXBContext jaxbContext = JAXBContext.newInstance(Category.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			// Retrieve article from input stream
			Object obj = unmarshaller.unmarshal(is);
			Category cat = (Category) obj;

			logger.info("Attempting to persist category object");
			em.persist(cat);

			logger.info("Commit category persist");
			em.getTransaction().commit();
			em.close();

			return Response.created(URI.create("/news/categories/" + cat.getCategoryID())).build();

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		// RESPONSE TODO
		return null;

	}

	@POST
	@Path("/users") // separate Reporters from Readers
	@Consumes("application/xml")
	public Response signupUser(InputStream is) {
		String username;
		logger.info("Calling POST method to register a new user");
		JAXBContext jaxbContext1;
		JAXBContext jaxbContext2;
		Unmarshaller unmarshaller1 = null;
		Unmarshaller unmarshaller2 = null;
		EntityManager em = PersistenceManager.instance().createEntityManager();

		// Store input stream to Byte array in-case first attempt to unmarshal
		// fails
		// due to it being a Reader object
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			IOUtils.copy(is, output);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		byte[] bytes = output.toByteArray();
		ByteArrayInputStream is1 = new ByteArrayInputStream(bytes);
		ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);

		try {
			em.getTransaction().begin();

			// Produce unmarshaller using JAXB
			jaxbContext1 = JAXBContext.newInstance(Reporter.class);
			jaxbContext2 = JAXBContext.newInstance(Reader.class);

			unmarshaller1 = jaxbContext1.createUnmarshaller();
			unmarshaller2 = jaxbContext2.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		// Retrieve article from input stream
		try {
			Object obj = unmarshaller1.unmarshal(is1);
			logger.info("Identified as a Reporter object, successfully unmarshalled.");
			Reporter reporter = (Reporter) obj;
			logger.info("Attempting to persist reporter object");
			em.persist(reporter);
			username = reporter.getUsername();
		} catch (JAXBException e) {
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
			username = reader.getUserName();
		}

		logger.info("Commit creation of user");
		em.getTransaction().commit();
		em.close();
		return Response.created(URI.create("/news/users/" + username)).build();

	}

	@GET
	@Path("/articles/{articleID}")
	@Produces("application/xml")
	public StreamingOutput retrieveArticle(@PathParam("articleID") int articleID) { // Using
																					// Path
																					// Params
		logger.info("Calling GET method to Retrieve article of ID: " + articleID);

		final String articleXml;

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		logger.info("Attempting to fetch Article");
		// Use of entity graph to eagerly fetch the referenced Reporter object
		EntityGraph<?> graph = em.getEntityGraph("graph.Article.writer");
		Map<String, Object> hints = new HashMap<String, Object>();
		hints.put("javax.persistence.fetchgraph", graph);
		Article article = em.find(Article.class, articleID, hints);

		em.getTransaction().commit();

		if (article == null) {
			logger.info("No Article found by that id");
			
		} else {
			logger.info("Marshal and return Article object");
			try {
				JAXBContext jaxb = JAXBContext.newInstance(Article.class);
				Marshaller marshaller = jaxb.createMarshaller();
				StringWriter writer = new StringWriter();
				marshaller.marshal(article, writer);
				articleXml = writer.toString();

				em.close();

				StreamingOutput streamOutput = new StreamingOutput() {
					public void write(OutputStream outputStream) {
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
	@Produces("application/xml")
	public StreamingOutput getArticleType(@MatrixParam("category") int categoryID) { // Using
																						// Matrix
																						// Paramters
		logger.info("Calling GET method to retrieve articles from category of identity: " + categoryID);

		final List<Article> articles = fetchCategoryArticles(categoryID);

		StreamingOutput streamOutput = new StreamingOutput() {
			public void write(OutputStream outputStream) {
				PrintStream writer = new PrintStream(outputStream);
				try {
					JAXBContext context = JAXBContext.newInstance(Article.class);
					Marshaller marshaller = context.createMarshaller();
					logger.info("Attempting to marshal retrieved article objects");
					for (Article article : articles) {
						StringWriter articleWriter = new StringWriter();
						marshaller.marshal(article, articleWriter);
						writer.println(articleWriter.toString());
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}

			}
		};

		return streamOutput;

	}

	//Retrieves all articles listed under a certain category
	public List<Article> fetchCategoryArticles(int categoryID) {
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		Category cat = em.find(Category.class, categoryID);
		logger.info("Fetching articles from category: " + cat.getCategoryName());
		final List<Article> articles = cat.getArticles();
		articles.size();

		em.getTransaction().commit();
		em.close();
		return articles;
	}

	@GET
	@Path("/articles/subscribed")
	@Produces("application/xml")
	public StreamingOutput getSubscribedArticles(@CookieParam("username") String username) { // Using
																								// Cookies

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		logger.info("Calling GET method to retrieve articles from categories which " + username + " subscribes to");
		Reader reader = em.find(Reader.class, username);
		final List<Category> categories = reader.getFavouriteCategories();
		final List<Article> allArticles = new ArrayList<Article>();
		categories.size();
		for (Category cat : categories) {
			allArticles.addAll(fetchCategoryArticles(cat.getCategoryID()));
		}

		em.getTransaction().commit();
		em.close();

		//Setup streaming output to return XML strings of article objects
		StreamingOutput streamOutput = new StreamingOutput() {
			public void write(OutputStream outputStream) {
				PrintStream writer = new PrintStream(outputStream);
				try {
					JAXBContext context = JAXBContext.newInstance(Article.class);
					Marshaller marshaller = context.createMarshaller();
					logger.info("Attempting to marshal retrieved article objects");
					for (Article article : allArticles) {
						StringWriter articleWriter = new StringWriter();
						marshaller.marshal(article, articleWriter);
						writer.println(articleWriter.toString());
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}

			}
		};

		return streamOutput;
	}

	@GET
	@Path("/categories")
	@Produces("application/xml")
	public StreamingOutput getCategories() {
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		logger.info("Calling GET method to retrieve a list of all Categories");
		TypedQuery<Category> query = em.createQuery("Select c From Category c", Category.class);
		final List<Category> categories = query.getResultList();

		em.getTransaction().commit();
		em.close();

		//Setup streaming output to return XML strings of category objects
		StreamingOutput streamOutput = new StreamingOutput() {
			public void write(OutputStream outputStream) {
				PrintStream writer = new PrintStream(outputStream);
				try {
					JAXBContext context = JAXBContext.newInstance(Article.class);
					Marshaller marshaller = context.createMarshaller();
					logger.info("Attempting to marshal retrieved Category objects");
					for (Category category : categories) {
						StringWriter articleWriter = new StringWriter();
						marshaller.marshal(category, articleWriter);
						writer.println(articleWriter.toString());
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
		};

		return streamOutput;
	}

	@GET
	@Path("/categories/{categoryID}")
	@Produces("application/xml")
	public StreamingOutput retrieveCategory(@PathParam("categoryID") int categoryID) { // Using
																						// Path
																						// Params
		logger.info("Calling GET method to Retrieve category of ID: " + categoryID);

		final String categoryXml;

		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		logger.info("Attempting to fetch Category");
		Category category = em.find(Category.class, categoryID);

		em.getTransaction().commit();

		if (category == null) {
			logger.info("No Category found by that id");
		} else {
			logger.info("Marshal and return Category object");
			try {
				JAXBContext jaxb = JAXBContext.newInstance(Category.class);
				Marshaller marshaller = jaxb.createMarshaller();
				StringWriter writer = new StringWriter();
				marshaller.marshal(category, writer);
				categoryXml = writer.toString();

				em.close();

				StreamingOutput streamOutput = new StreamingOutput() {
					public void write(OutputStream outputStream) {
						PrintStream writer = new PrintStream(outputStream);
						writer.println(categoryXml);
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
	@Path("/users/{username}")
	@Produces("application/xml")
	public StreamingOutput getUser(@PathParam("username") String username) {
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		logger.info("Calling GET method to retrieve the User: " + username);

		Reporter reporter = em.find(Reporter.class, username);
		Reader reader = em.find(Reader.class, username);
		User output;
		try {
			Marshaller marshaller;

			if (reader != null) {
				output = reader;
				JAXBContext jaxb = JAXBContext.newInstance(Reader.class);
				marshaller = jaxb.createMarshaller();
			} else if (reporter != null) {
				output = reporter;
				JAXBContext jaxb = JAXBContext.newInstance(Reporter.class);
				marshaller = jaxb.createMarshaller();
			} else {
				logger.info("No User found by that username");
				marshaller = null;
				output = null;
			}

			//Write the user object to a string
			StringWriter writer = new StringWriter();
			marshaller.marshal(output, writer);
			final String userXML = writer.toString();

			em.close();

			//Setup output stream, write the xml of the user to it
			StreamingOutput streamOutput = new StreamingOutput() {
				public void write(OutputStream outputStream) {
					PrintStream writer = new PrintStream(outputStream);
					writer.println(userXML);
				}
			};

			return streamOutput;

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		em.getTransaction().commit();
		em.close();
		return null;
	}

	@PUT
	@Path("/users/{username}")
	@Consumes("application/xml")
	public void updateUser(InputStream is, @PathParam("username") String username) {
		logger.info("Calling Update method to update the User: " + username);
		JAXBContext jaxbContext1;
		JAXBContext jaxbContext2;
		Unmarshaller unmarshaller1 = null;
		Unmarshaller unmarshaller2 = null;
		EntityManager em = PersistenceManager.instance().createEntityManager();

		// Store input stream to Byte array in-case first attempt to unmarshal
		// fails
		// due to it being a Reader object
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			IOUtils.copy(is, output);
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		byte[] bytes = output.toByteArray();
		ByteArrayInputStream is1 = new ByteArrayInputStream(bytes);
		ByteArrayInputStream is2 = new ByteArrayInputStream(bytes);

		try {
			em.getTransaction().begin();

			// Produce unmarshaller using JAXB
			jaxbContext1 = JAXBContext.newInstance(Reporter.class);
			jaxbContext2 = JAXBContext.newInstance(Reader.class);

			unmarshaller1 = jaxbContext1.createUnmarshaller();
			unmarshaller2 = jaxbContext2.createUnmarshaller();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		// Retrieve the User object from the input stream and determine which
		// type it is
		// Proceed to update the object with new fields
		try {
			Object obj = unmarshaller1.unmarshal(is1);
			logger.info("Identified as a Reporter object, successfully unmarshalled.");
			Reporter reporter = (Reporter) obj;

			logger.info("Update the Reporter object in the database");
			Reporter oldReporter = em.find(Reporter.class, username);
			oldReporter.setFirstName(reporter.getFirstName());
			oldReporter.setLastName(reporter.getLastName());
			oldReporter.setCreationYear(reporter.getCreationYear());

		} catch (JAXBException e) {
			try {
				logger.info("Identified as a Reader object, attempting to unmarshal.");
				Object obj = unmarshaller2.unmarshal(is2);
				Reader reader = (Reader) obj;
				logger.info("Update the Reader object in the database");
				Reader oldReader = em.find(Reader.class, username);
				oldReader.setFirstName(reader.getFirstName());
				oldReader.setLastName(reader.getLastName());
				oldReader.setCreationYear(reader.getCreationYear());
				oldReader.setFavouriteCategories(reader.getFavouriteCategories());

			} catch (JAXBException e1) {
				e1.printStackTrace();
			}

		}

		logger.info("Commit update of user");
		em.getTransaction().commit();
		em.close();
	}

	@DELETE
	@Path("/articles/{articleID}")
	public void deleteArticle(@PathParam("articleID") int articleID) { // Delete
																		// Using
																		// Path
																		// Params
		EntityManager em = PersistenceManager.instance().createEntityManager();
		em.getTransaction().begin();

		//Retrieve article object and delete it from the database
		Article article = em.find(Article.class, articleID);
		em.remove(article);

		em.getTransaction().commit();
		em.close();
	}


	

}
