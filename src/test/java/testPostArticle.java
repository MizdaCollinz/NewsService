import static org.junit.Assert.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;

import news.domain.Article;
import news.domain.Reporter;

public class testPostArticle {

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
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Article.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			
			Reporter writer = new Reporter();
			writer.setUserName("steven101");
			writer.setCreationYear(2016);
			Article article = new Article();
			article.setId(1);
			article.setTitle("Steve");
			article.setWriter(writer);
			
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
			marshaller.marshal(article,System.out);
			
		} catch (JAXBException e) {
			
			e.printStackTrace();
		}
	}

}
