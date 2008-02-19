package eu.etaxonomy.cdm.model.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
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
	private UUID uuid;
	private Calendar created;
	private Person createdBy;

	public CdmBase() {
		this.uuid = UUID.randomUUID();
		this.setCreated(Calendar.getInstance());
	}

	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
	}
	
	@Transient
	public boolean hasListeners(String propertyName) {
		return propertyChangeSupport.hasListeners(propertyName);
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

	
	private String getStrUuid() {
		return this.uuid.toString();
	}
	private void setStrUuid(String uuid) {
		this.uuid = UUID.fromString(uuid);
	}
	
	
	@Transient
	public UUID getUuid() {
		return this.uuid;
	}
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	
	@Temporal(TemporalType.TIMESTAMP)
	@Basic(fetch = FetchType.LAZY)
	public Calendar getCreated() {
		return created;
	}
	public void setCreated(Calendar created) {
		if (created != null){
			created.set(Calendar.MILLISECOND, 0);
		}
		this.created = created;
	}


	@ManyToOne(fetch=FetchType.LAZY)
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
		if (obj == null){
			return false;
		}else if (CdmBase.class.isAssignableFrom(obj.getClass())){
			CdmBase cdmObj = (CdmBase)obj;
			boolean uuidEqual = cdmObj.getUuid().equals(this.getUuid());
			boolean createdEqual = cdmObj.getCreated().equals(this.getCreated());
			if (uuidEqual && createdEqual){
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
