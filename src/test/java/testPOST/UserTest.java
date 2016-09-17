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

import news.domain.Reader;
import news.domain.Reporter;

public class UserTest {
		
	private static String WEB_SERVICE_URI = "http://localhost:1357/services/news/users";
	private static Logger logger = LoggerFactory.getLogger(UserTest.class);
	
	@Test
	public void testReporterPosting(){
		
			logger.info("Starting User marshalling and POST test");
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
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.marshal(reporter,System.out);
			marshaller.marshal(reporter, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post REPORTER entity to the client");
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
	}	
	
	@Test
	public void testReaderPosting(){
		
			logger.info("Starting User marshalling and POST test");
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Reader.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			//Create Reporter
			Reader reader = new Reader();
			reader.setUserName("Steven111");
			reader.setFirstName("Steven");
			reader.setLastName("Steel");
			reader.setCreationYear(2016);
			
			//Convert object to XML string
			StringWriter stringW = new StringWriter();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.marshal(reader,System.out);
			marshaller.marshal(reader, stringW);
			String input = stringW.toString();
			
			//Start Server and call post method
			Client client = ClientBuilder.newClient();
			
			logger.info("Attempting to post READER entity to the client");
			client.target(WEB_SERVICE_URI).request().post(Entity.xml(input));
			
			// Check status code of reponse and print the URI of the newly created Article
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
		
	}	

}
