package org.bgbm.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;

@Entity
public class Artist extends MetaBase {
	static Logger logger = Logger.getLogger(Artist.class);

	private String name;
	//private Set<Record> records = new HashSet();
	private Set<Person> musicians;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToMany(cascade=CascadeType.PERSIST)
	public Set<Person> getMusicians() {
		return musicians;
	}

	public void setMusicians(Set<Person> musicians) {
		this.musicians = musicians;
	}

}
