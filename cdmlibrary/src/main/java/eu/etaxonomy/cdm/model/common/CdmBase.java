package eu.etaxonomy.cdm.model.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Person;


@MappedSuperclass
public abstract class CdmBase implements Serializable{
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private int id;
	private String uuid;
	private Calendar created;
	private Person createdBy;

	public CdmBase() {
		this.uuid = UUID.randomUUID().toString();
		this.created = Calendar.getInstance();
	}

	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	@Transient
	public void hasListeners(String propertyName) {
		propertyChangeSupport.hasListeners(propertyName);
	}

	public void firePropertyChange(String property, String oldval, String newval) {
		propertyChangeSupport.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, int oldval, int newval) {
		propertyChangeSupport.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, float oldval, float newval) {
		propertyChangeSupport.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, boolean oldval, boolean newval) {
		propertyChangeSupport.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, Object oldval, Object newval) {
		propertyChangeSupport.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(PropertyChangeEvent evt) {
		propertyChangeSupport.firePropertyChange(evt);
	}

	@Id
	@GeneratedValue(generator = "system-increment")
	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}

	
	public String getUuid() {
		return this.uuid;
	}
	protected void setUuid(String uuid) {
		this.uuid = uuid;
	}

	
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getCreated() {
		return this.created;
	}
	public void setCreated(Calendar created) {
		this.created = created;
	}


	@ManyToOne
	@Cascade( { CascadeType.SAVE_UPDATE })
	public Person getCreatedBy() {
		return this.createdBy;
	}
	public void setCreatedBy(Person createdBy) {
		this.createdBy = createdBy;
	}

	
	@Override
	// equals if UUID and created timestamp are the same!
	public boolean equals(Object obj) {
		if (CdmBase.class.isAssignableFrom(obj.getClass())){
			CdmBase cdmObj = (CdmBase)obj;
			if (cdmObj.getUuid().equals(this.getUuid()) && cdmObj.getCreated().equals(this.getCreated())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"<"+this.getUuid()+">";
	}
	
}
