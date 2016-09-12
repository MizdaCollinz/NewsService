package news.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Category {
	
	public Category(){
		//Default Constructor
	}
	
	String categoryName;
	@Id
	int categoryID;
	
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public int getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}

	
}
