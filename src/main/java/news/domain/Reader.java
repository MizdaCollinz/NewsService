package news.domain;

import java.util.List;

import javax.persistence.Entity;

@Entity
public class Reader extends User {
	List<String> favouriteCategories;
}
