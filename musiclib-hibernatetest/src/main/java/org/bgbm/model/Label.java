package org.bgbm.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

@Entity
public class Label extends MetaBase {
	static Logger logger = Logger.getLogger(Label.class);

	private String name;
	private Set<Record> records = new HashSet();

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
	
}
