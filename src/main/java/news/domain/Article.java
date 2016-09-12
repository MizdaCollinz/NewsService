package news.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
public class Article {
	
	public Article(){
		//Default Constructor
	}
	
	@Id
	private int id;
	
	private String title;
	
	@ManyToOne(cascade=CascadeType.PERSIST)
	private Category category;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
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
	public int getId() {
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
