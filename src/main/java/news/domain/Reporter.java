package news.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class Reporter extends User {
	
	@XmlTransient
	@OneToMany(mappedBy = "writer",fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	List<Article> writtenArticles;
	
	public Reporter(){
		super();
		this.writtenArticles = new ArrayList<Article>();
	}
	
	public Reporter(String username, String firstName, String lastName, int creationYear){
		this.writtenArticles = new ArrayList<Article>();
		setUserName(username);
		setFirstName(firstName);
		setLastName(lastName);
		setCreationYear(creationYear);
	}

	public List<Article> getWrittenArticles() {
		return writtenArticles;
	}

	public void setWrittenArticles(List<Article> writtenArticles) {
		this.writtenArticles = writtenArticles;
	}
	
	public void setWrittenArticle(Article writtenArticle){
		this.writtenArticles.add(writtenArticle);
	}

	public String getUsername() {
		return super.getUserName();
	}
}
