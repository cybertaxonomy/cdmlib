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

@MappedSuperclass
public abstract class Annotatable extends EntityBase{
	static Logger logger = Logger.getLogger(Annotatable.class);

	private String uuid;
	private Set<Annotation> annotations = new HashSet();
	
	public Annotatable() {
		this.uuid = UUID.randomUUID().toString();
	}

	@OneToMany
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

	public String getUuid(){
		return this.uuid;
	}
	private void setUuid(String uuid){
		this.uuid = uuid;
	}

}
