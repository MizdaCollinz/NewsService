package news.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedAttributeNode;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity

@NamedEntityGraph(name = "graph.Article.writer",attributeNodes={@NamedAttributeNode("writer"),@NamedAttributeNode("category")})
public class Article {
	
	public Article(){
		//Default Constructor
	}
	
	public Article(Reporter writer, Category category, String title){
		this.writer = writer;
		this.category = category;
		this.title = title;
	}
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int id;
	
	private String title;
	
	@ManyToOne(fetch=FetchType.LAZY)
	private Category category;
	
	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	private Reporter writer;
	
	
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	public Reporter getWriter() {
		return writer;
	}
	public void setWriter(Reporter writer) {
		this.writer = writer;
	}
	public long getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
