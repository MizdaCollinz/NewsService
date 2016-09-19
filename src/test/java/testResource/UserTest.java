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

import news.domain.Reader;
import news.domain.Reporter;

public class UserTest {
		
	private static String WEB_SERVICE_URI = "http://localhost:1357/services/news/users";
	private static Logger logger = LoggerFactory.getLogger(UserTest.class);
	
	@Test
	public void testReporterPosting(){
			logger.info("USERTEST");
			logger.info("Starting Reporter marshalling and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Reporter.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			//Create Reporter
			Reporter reporter = new Reporter();
			reporter.setUserName("Steven101");
			reporter.setFirstName("Steven");
			reporter.setLastName("Steel");
			reporter.setCreationYear(2016);
			
			//Convert object to XML string
			StringWriter stringW = new StringWriter();
			marshaller.marshal(reporter, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post REPORTER entity to the server");
			Response response = client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			assertEquals(response.getStatus(),201);
			String location = response.getLocation().toString();
			response.close();
			
			String user = client.target(location).request().get(String.class);
			assertTrue(user.contains("<userName>Steven101</userName>"));
			assertTrue(user.contains("<creationYear>2016</creationYear>"));
			assertTrue(user.contains("<firstName>Steven</firstName>"));
			assertTrue(user.contains("<lastName>Steel</lastName>"));
			
			logger.info("USERTEST - PART TWO");
			//Attempt to update the Username of Reporter object
			reporter.setFirstName("NotARealName");
			stringW.getBuffer().setLength(0);
			marshaller.marshal(reporter, stringW);
			input = stringW.toString();
			
			logger.info("Attempting to update REPORTER entity in the server");
			response = client.target(WEB_SERVICE_URI + "/"+reporter.getUsername()).request().put(Entity.xml(input));
			assertEquals(response.getStatus(),204); //Default Response code should be provided 
		
			
			logger.info("USERTEST - PART THREE");
			logger.info("Attempting to get created and updated REPORTER entity from the server");
			String userXML = client.target(WEB_SERVICE_URI + "/" + reporter.getUsername()).request().get(String.class);
			logger.info("Updated user printed as" + userXML);
			
			assertTrue(userXML.contains("<firstName>NotARealName</firstName>"));
			
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
	}	
	
	@Test
	public void testReaderPosting(){
			logger.info("READERTEST");
			logger.info("Starting Reader marshalling and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Reader.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			//Create Reporter
			Reader reader = new Reader();
			reader.setUserName("Charles101");
			reader.setFirstName("Charles");
			reader.setLastName("Steel");
			reader.setCreationYear(2016);
			
			//Convert object to XML string
			StringWriter stringW = new StringWriter();
			marshaller.marshal(reader, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post READER entity to the server");
			Response response = client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			assertEquals(response.getStatus(),201);
			String location = response.getLocation().toString();
			response.close();
			
			String user = client.target(location).request().get(String.class);
			assertTrue(user.contains("<userName>Charles101</userName>"));
			assertTrue(user.contains("<creationYear>2016</creationYear>"));
			assertTrue(user.contains("<firstName>Charles</firstName>"));
			assertTrue(user.contains("<lastName>Steel</lastName>"));
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
	}	

}
