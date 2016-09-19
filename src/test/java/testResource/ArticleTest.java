package testResource;

import static org.junit.Assert.*;

import java.io.OutputStream;
import java.io.StringWriter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;
import news.domain.Category;
import news.domain.Reader;
import news.domain.Reporter;
import news.services.NewsResource;

public class ArticleTest {

	private static String WEB_SERVICE_URI = "http://localhost:1357/services/news/articles";
	private static Logger logger = LoggerFactory.getLogger(ArticleTest.class);

	@Test
	public void testArticle(){
		Marshaller marshaller = null;
		String location = null;
		Category category = null;
		
		// TEST Article POST
		logger.info("ARTICLETEST");
		logger.info("Starting storage/submission of an article and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			marshaller = jaxbContext.createMarshaller();
			
			//Create Reporter [alternatively provide cookie of existing reporter]
			Reporter writer = new Reporter();
			writer.setUserName("Anthony101");
			writer.setFirstName("Anthony");
			writer.setLastName("Steel");
			writer.setCreationYear(2016);
			
			//Create Category
			category = new Category();
			category.setCategoryName("National News");
			category.setCategoryID(1);
			
			//Create Article
			Article article = new Article();
			article.setTitle("The National News");
			article.setWriter(writer);
			article.setCategory(category);
			writer.setWrittenArticle(article);
			
			//Convert object to XML string
			StringWriter stringW = new StringWriter();
			marshaller.marshal(article, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post ARTICLE entity to the client");
			Response response = client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			location = response.getLocation().toString();
			// Check status code of reponse and print the URI of the newly created Article
			assertEquals(response.getStatus(),201);
			response.close();
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
		// TEST Article GET - Path Parameters
		logger.info("ARTICLETEST - PART TWO");
		logger.info("Starting article retrieval of a particular article, GET test");
		
		String WEB_GET_ARTICLE = location;
		
		Client client = ClientBuilder.newClient();
		
		logger.info("Attempting to retrieve ARTICLE entity from server");
		String articleXML = client.target(WEB_GET_ARTICLE).request().get(String.class);
		
		logger.info(articleXML);
		assertTrue(articleXML.contains("<title>The National News</title>"));
		assertTrue(articleXML.contains("<category><categoryName>National News</categoryName><categoryID>1</categoryID></category>"));
		assertTrue(articleXML.contains("<writer><userName>Anthony101</userName><creationYear>2016</creationYear><firstName>Anthony</firstName><lastName>Steel</lastName></writer>"));
		
		//TEST Article Category GET - Matrix Parameters
		logger.info("ARTICLETEST - PART THREE");	
		logger.info("Starting article retrieval of a particular category, GET test");
		//Posting articles and categories in preparation
		Reporter testReporter = new Reporter("Bobby101", "Bob", "Smith", 2016);
		Category testCat = new Category("Business",2);
		Article testArticle = new Article(testReporter,testCat,"Test Article 1");
		Article testArticle2 = new Article(testReporter,testCat,"Test Article 2");
		
		try{
		StringWriter stringW = new StringWriter();
		StringWriter stringW2 = new StringWriter();
		marshaller.marshal(testArticle, stringW);
		marshaller.marshal(testArticle2, stringW2);
		String input = stringW.toString();
		String input2 = stringW2.toString();
		
		logger.info("Attempting to post 2 test articles");
		client.target(WEB_SERVICE_URI).request().post(Entity.xml(input)).close();;
		client.target(WEB_SERVICE_URI).request().post(Entity.xml(input2)).close();;
		
		} catch(JAXBException e){
			e.printStackTrace();
		}
	
		
		logger.info("Starting article retrieval from specified category, GET test");
		String WEB_GET_ARTICLE_CATEGORY = WEB_SERVICE_URI + ";category=2";
		
		String articlesXML = client.target(WEB_GET_ARTICLE_CATEGORY).request().get(String.class);
		//Check that the two articles from the subscribed category Business are returned as expected
		assertTrue(articlesXML.contains("<title>Test Article 1</title>"));
		assertTrue(articlesXML.contains("<title>Test Article 2</title>"));
		
		
		logger.info("ARTICLETEST - PART FOUR");
		logger.info("Post Reader with subscription to Business");
		Reader reader = new Reader("Subscriber101","Addicted","Reader",2016);
		reader.setFavouriteCategory(testCat);
		reader.setFavouriteCategory(category);
		
		try {
			StringWriter writeRead = new StringWriter();
			JAXBContext jaxCon = JAXBContext.newInstance(Reader.class);
			Marshaller mar = jaxCon.createMarshaller();
			mar.marshal(reader,writeRead);
			String post_user = "http://localhost:1357/services/news/users";
			client.target(post_user).request().post(Entity.xml(writeRead.toString())).close();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		final NewCookie cookie = new NewCookie("username","Subscriber101");
		String subArticlesXML = client.target(WEB_SERVICE_URI + "/subscribed").request().cookie(cookie).get(String.class);
		logger.info("Retrieved subscription articles for Subscriber101: " + subArticlesXML);
		
		assertTrue(subArticlesXML.contains("<title>Test Article 1</title>"));
		assertTrue(subArticlesXML.contains("<title>Test Article 2</title>"));
		assertTrue(subArticlesXML.contains("<title>The National News</title>"));
		
	// ASYNCHRONOUS TESTING
		logger.info("ARTICLETEST - PART FIVE");
		logger.info("Asynchronous subscription to articles of Business category");
		String async_service = "http://localhost:1357/services/newsA/subscribe/2";
		final WebTarget target = client.target(async_service);
		target.request().cookie(cookie).async().get(new InvocationCallback<String>(){

			@Override
			public void completed(String arg0) {
				logger.info("ASYNCHRONOUS SUCCESS Received the following article: " + arg0);
				assertTrue(arg0.contains("<article><id>4</id><title>Business Article ASYNC</title><category><categoryName>Business</categoryName><categoryID>2</categoryID></category><writer><userName>Async101</userName><creationYear>2016</creationYear><firstName>Async</firstName><lastName>Response</lastName></writer></article>"));
				target.request().cookie(cookie).async().get(this);
			}

			@Override
			public void failed(Throwable arg0) {
				logger.error("Failed to receive a Business article");
				try {
					throw arg0;
				} catch (Throwable e) {
					logger.error("Asynchronous get cancelled, user has been unsubscribed successfully");
				}
			}
			
		});
		//Post a article relevant to the subscription
		logger.info("Post an article which should notify the waiting subscriber");
		try {
			Client client2 = ClientBuilder.newClient();
			Article art = new Article(new Reporter("Async101","Async","Response",2016),new Category("Business",2), "Business Article ASYNC");
			StringWriter stringW = new StringWriter();
			marshaller.marshal(art, stringW);
			String input = stringW.toString();
			
			client2.target("http://localhost:1357/services/newsA/articles").request().post(Entity.xml(input)).close();
			
			try {
				//Allow article to be posted and received before cancelling subscription
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("Cancel the subscription to Business category");
			client2.target(async_service).request().cookie(cookie).delete();
			
			
			client2.close();
		} catch (JAXBException e){
			e.printStackTrace();
		}
		
		
		
		client.close();
		
	}

}
