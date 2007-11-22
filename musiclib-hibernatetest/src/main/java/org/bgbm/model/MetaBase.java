package org.bgbm.model;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;

//@MappedSuperclass
@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class MetaBase {
	static Logger logger = Logger.getLogger(MetaBase.class);

	private int id;
	private String uuid;
	private Calendar created;
	private Set<Annotation> annotations = new HashSet();
	
	@OneToMany(mappedBy="parent")
	@Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE,
        org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    public Set<Annotation> getAnnotations() {
		return annotations;
	}
	private void setAnnotations(Set<Annotation> annotations) {
		this.annotations = annotations;
	}
	public void addAnnotation(Annotation annotation) {
		this.annotations.add(annotation);
	}
	public void addAnnotation(String text) {
		this.annotations.add(new Annotation(text));
	}
	public void removeAnnotation(Annotation annotation) {
		this.annotations.remove(annotation);
	}

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
