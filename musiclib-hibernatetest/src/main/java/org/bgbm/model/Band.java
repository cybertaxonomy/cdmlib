package org.bgbm.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;

@Entity
public class Band extends Annotatable {
	public Band() {
		super();
		// TODO Auto-generated constructor stub
	}
	static Logger logger = Logger.getLogger(Band.class);

	private String name;
	//private Set<Record> records = new HashSet();

	
	public Band(String title) {
		name=title;
	}

	public String getName() {
		if (name!=null){			
			return name;
		}else{
			return "";
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString(){
		return getName();
	}
}
