package testPOST;
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

public class PostArticleTest {
	
	private static String WEB_SERVICE_URI = "http://localhost:1357/services/news/articles";
	private static Logger logger = LoggerFactory.getLogger(PostArticleTest.class);
	
	/*@Test
	public void testArticle() {
		//Start Server
		
		//Create Article
		//Create Identity as Poster
		
		//Check Status Code of response
		
		//Close Server
		
		fail("Not yet implemented");
	}*/
	
	@Test
	public void testArticleMarshalling(){
		
			logger.info("Starting article marshalling and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			Reporter writer = new Reporter();
			writer.setUserName("Steven101");
			writer.setFirstName("Steven");
			writer.setLastName("Steel");
			writer.setCreationYear(2016);
			
			Category category = new Category();
			category.setCategoryID(10);
			category.setCategoryName("General Content");
			
			Article article = new Article();
			article.setId(1);
			article.setTitle("The Most General of all the General Content");
			article.setWriter(writer);
			article.setCategory(category);
			
			
			StringWriter stringW = new StringWriter();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.marshal(article,System.out);
			marshaller.marshal(article, stringW);
			String input = stringW.toString();
			
			
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post ARTICLE entity to the client");
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
	}

}
