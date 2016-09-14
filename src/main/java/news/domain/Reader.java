package news.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
@Entity
@XmlAccessorType(XmlAccessType.FIELD)
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public class Reader extends User {
	
	@ManyToMany
	List<Category> favouriteCategories;
	
	public Reader(){
		super();
		favouriteCategories = new ArrayList<Category>();
	}

	public List<Category> getFavouriteCategories() {
		return favouriteCategories;
	}

	public void setFavouriteCategories(List<Category> favouriteCategories) {
		this.favouriteCategories = favouriteCategories;
	}
	
	public void setFavouriteCategory(Category category){
		this.favouriteCategories.add(category);
	}
}
