package news.domain;

import java.util.List;

import javax.persistence.Entity;

@Entity
public class Reporter extends User {
	List<Article> writtenArticles;
}
