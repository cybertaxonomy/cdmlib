package org.bgbm.model;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class MetaUltra {
	private Calendar created_ultra;
	private int id;

	public MetaUltra() {
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getCreatedUltra() {
		return created_ultra;
	}

	public void setCreatedUltra(Calendar created) {
		this.created_ultra = created;
	}
	@Id
	@GeneratedValue(generator = "system-increment")
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
