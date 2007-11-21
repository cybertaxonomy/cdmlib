package org.bgbm.model;

import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
//@Inheritance(strategy=InheritanceType.JOINED)
public abstract class MetaBase {
	private int id;
	private String uuid;
	private Calendar created;

	public MetaBase() {
		this.uuid = UUID.randomUUID().toString();
		this.created = Calendar.getInstance();
	}

	@Id
	@GeneratedValue(generator = "system-increment")
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}


	public String getUuid(){
		return this.uuid;
	}
	private void setUuid(String uuid){
		this.uuid = uuid;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getCreated() {
		return created;
	}

	public void setCreated(Calendar created) {
		this.created = created;
	}

}
