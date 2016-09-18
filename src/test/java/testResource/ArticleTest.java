package testResource;
import static org.junit.Assert.*;

import java.io.StringWriter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Article;
import news.domain.Category;
import news.domain.Reporter;
import news.services.NewsResource;

public class ArticleTest {
	
	private static String WEB_SERVICE_URI = "http://localhost:1357/services/news/articles";
	private static Logger logger = LoggerFactory.getLogger(ArticleTest.class);
	
	@Test
	public void testArticle(){
		Marshaller marshaller = null;
		
		// TEST Article POST
		logger.info("Starting article marshalling and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			marshaller = jaxbContext.createMarshaller();
			
			//Create Reporter [alternatively provide cookie of existing reporter]
			Reporter writer = new Reporter();
			writer.setUserName("Steven101");
			writer.setFirstName("Steven");
			writer.setLastName("Steel");
			writer.setCreationYear(2016);
			
			//Create Category
			Category category = new Category();
			category.setCategoryName("General Content");
			category.setCategoryID(1);
			
			//Create Article
			Article article = new Article();
			article.setTitle("The Most General of all the General Content");
			article.setWriter(writer);
			article.setCategory(category);
			
			//Convert object to XML string
			StringWriter stringW = new StringWriter();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.marshal(article,System.out);
			marshaller.marshal(article, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post ARTICLE entity to the client");
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
		// TEST Article GET - Path Parameters
		
		logger.info("Starting article retrieval, GET test");
		
		String WEB_GET_ARTICLE = WEB_SERVICE_URI + "/1";
		
		Client client = ClientBuilder.newClient();
		
		logger.info("Attempting to retrieve ARTICLE entity from server");
		String articleXML = client.target(WEB_GET_ARTICLE).request().get(String.class);
		
		logger.info(articleXML);
		
		
		//TEST Article Category GET - Matrix Parameters
		
		//Posting articles and categories in preparation
		try{
		Reporter testReporter = new Reporter("Bobby", "Bob", "Smith", 2016);
		Category testCat = new Category("Science",2);
		Article testArticle = new Article(testReporter,testCat,"Test Article 1");
		Article testArticle2 = new Article(testReporter,testCat,"Test Article 2");
		StringWriter stringW = new StringWriter();
		StringWriter stringW2 = new StringWriter();
		marshaller.marshal(testArticle, stringW);
		marshaller.marshal(testArticle2, stringW2);
		String input = stringW.toString();
		String input2 = stringW2.toString();
		
		logger.info("Attempting to post 2 test articles");
		client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
		client.target(WEB_SERVICE_URI).request().post(Entity.xml(input2));
		
		} catch(JAXBException e){
			e.printStackTrace();
		}
	
		logger.info("Starting article retrieval from specified category, GET test");
		
		String WEB_GET_ARTICLE_CATEGORY = WEB_SERVICE_URI + ";category=2";
		
		String articlesXML = client.target(WEB_GET_ARTICLE_CATEGORY).request().get(String.class);
		
		logger.info(articlesXML);
		
	}
	
}
