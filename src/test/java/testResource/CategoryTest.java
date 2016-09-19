package testResource;

import static org.junit.Assert.*;

import java.io.StringWriter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import news.domain.Category;
import news.domain.Reader;



public class CategoryTest {

	private static String WEB_SERVICE_URI = "http://localhost:1357/services/news/categories";
	private static Logger logger = LoggerFactory.getLogger(CategoryTest.class);	
	
	@Test
	public void testCategoryMethods(){
			logger.info("CATEGORYTEST");
			logger.info("Starting Category marshalling and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Category.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			//Create Category
			Category cat = new Category();
			cat.setCategoryName("Entertainment");
			cat.setCategoryID(3);
			
			//Convert object to XML string
			StringWriter stringW = new StringWriter();
			marshaller.marshal(cat, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post CATEGORY entity to the client");
			Response response = client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			assertEquals(response.getStatus(),201);
			
			String location = response.getLocation().toString();
			response.close();
			
			logger.info("Retrieving newly created Category from returned URI location");
			String category = client.target(location).request().get(String.class);
			assertTrue(category.contains("<categoryID>3</categoryID>"));
			assertTrue(category.contains("<categoryName>Entertainment</categoryName>"));
			
			
			//Create several additional categories
			logger.info("CATEGORYTEST - PART TWO");
			logger.info("Posting three new categories to the database");
			Category cat1 = new Category("Technology",4);
			Category cat2 = new Category("World News",5);
			Category cat3 = new Category("Sports",6);
			
			stringW.getBuffer().setLength(0);
			marshaller.marshal(cat1, stringW);
			input = stringW.toString();
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input)).close();;
			
			stringW.getBuffer().setLength(0);
			marshaller.marshal(cat2, stringW);
			input = stringW.toString();
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input)).close();;
			
			stringW.getBuffer().setLength(0);
			marshaller.marshal(cat3, stringW);
			input = stringW.toString();
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input)).close();;
			
			
			logger.info("Attempting to retrieve the full list of categories from the server");
			String categoriesAsXML = client.target(WEB_SERVICE_URI).request().get(String.class);
			logger.info("Retrieved the categories: " + categoriesAsXML);
			logger.info("Expect a minimum of 4 <Category> entities with the IDs <categoryID> of 3,4,5 and 6");
			
			assertTrue(categoriesAsXML.contains("<category><categoryName>Entertainment</categoryName><categoryID>3</categoryID></category>"));
			assertTrue(categoriesAsXML.contains("<category><categoryName>Technology</categoryName><categoryID>4</categoryID></category>"));
			assertTrue(categoriesAsXML.contains("<category><categoryName>World News</categoryName><categoryID>5</categoryID></category>"));
			assertTrue(categoriesAsXML.contains("<category><categoryName>Sports</categoryName><categoryID>6</categoryID></category>"));
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
	}	

}
