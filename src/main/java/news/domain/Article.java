package news.domain;

import javax.persistence.Entity;

@Entity
public class Article {
	String category;
	Reporter writer;
}
