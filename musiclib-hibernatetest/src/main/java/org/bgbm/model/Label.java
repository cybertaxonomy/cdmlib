package org.bgbm.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;

@Entity
public class Label extends MetaBase {
	static Logger logger = Logger.getLogger(Label.class);

	private String name;
	private Set<Record> records = new HashSet();

	public Label(String string) {
		this.name=string;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(mappedBy="label", cascade={CascadeType.PERSIST, CascadeType.MERGE})
	public Set<Record> getRecords() {
		return records;
	}

	public void setRecords(Set<Record> records) {
		this.records = records;
	}
	
}
