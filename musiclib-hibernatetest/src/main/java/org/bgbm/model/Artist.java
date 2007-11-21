package org.bgbm.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

@Entity
public class Artist extends MetaBase {
	static Logger logger = Logger.getLogger(Artist.class);

	private String name;
	private Set<Record> records = new HashSet();
	private Set<Person> musicians;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Record> getRecords() {
		return records;
	}

	public void setRecords(Set<Record> records) {
		this.records = records;
	}

	public Set<Person> getMusicians() {
		return musicians;
	}

	public void setMusicians(Set<Person> musicians) {
		this.musicians = musicians;
	}

}
