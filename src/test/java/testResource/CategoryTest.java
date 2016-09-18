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
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.marshal(cat,System.out);
			marshaller.marshal(cat, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post READER entity to the client");
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			
			
			
			//Create several additional categories
			logger.info("CATEGORYTEST - PART TWO");
			logger.info("Posting three new categories to the database");
			Category cat1 = new Category("Technology",4);
			Category cat2 = new Category("World News",5);
			Category cat3 = new Category("Sports",6);
			
			stringW.getBuffer().setLength(0);
			marshaller.marshal(cat1, stringW);
			input = stringW.toString();
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			stringW.getBuffer().setLength(0);
			marshaller.marshal(cat2, stringW);
			input = stringW.toString();
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			stringW.getBuffer().setLength(0);
			marshaller.marshal(cat3, stringW);
			input = stringW.toString();
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			String categoriesAsXML = client.target(WEB_SERVICE_URI).request().get(String.class);
			logger.info("Retrieved the categories: " + categoriesAsXML);
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
	}	

}
