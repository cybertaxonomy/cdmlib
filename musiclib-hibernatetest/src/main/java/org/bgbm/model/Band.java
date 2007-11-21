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
public class Band extends MetaBase {
	static Logger logger = Logger.getLogger(Band.class);

	private String name;
	private Set<Person> musicians;
	//private Set<Record> records = new HashSet();

	
	public Band(String title) {
		name=title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, mappedBy="bands")
	public Set<Person> getMusicians() {
		return musicians;
	}

	public void setMusicians(Set<Person> musicians) {
		this.musicians = musicians;
	}

}
