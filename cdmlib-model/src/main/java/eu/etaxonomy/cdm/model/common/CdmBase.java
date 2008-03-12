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



/**
 * The base class for all CDM domain classes implementing UUIDs and bean property change event firing.
 * It provides a globally unique UUID and keeps track of creation date and person.
 * The UUID is the same for different versions (see {@link VersionableEntity}) of a CDM object, so a locally unique id exists in addition 
 * that allows to safely access and store several objects (=version) with the same UUID.
 * 
 * This class together with the {@link eu.etaxonomy.cdm.aspectj.PropertyChangeAspect} 
 * will fire bean change events to all registered listeners. Listener registration and event firing
 * is done with the help of the {@link PropertyChangeSupport} class.
 * 
 * @author m.doering
 *
 */
@MappedSuperclass
public abstract class CdmBase implements Serializable{
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	private int id;
	private UUID uuid;
	private Calendar created;
	private Person createdBy;

	/**
	 * Class constructor assigning a unique UUID and creation date.
	 * UUID can be changed later via setUuid method.
	 */
	public CdmBase() {
		this.uuid = UUID.randomUUID();
		this.setCreated(Calendar.getInstance());
	}

	
	/**
	 * see {@link PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)}
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * see {@link PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * see {@link PropertyChangeSupport#addPropertyChangeListener(PropertyChangeListener)}
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
	
	/**
	 * @see PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)
	 */
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

	/**
	 * Returns local unique identifier for the concrete subclass
	 * @return
	 */
	@Id
	@GeneratedValue(generator = "system-increment")
	public int getId() {
		return this.id;
	}
	/**
	 * Assigns a unique local ID to this object. 
	 * Because of the EJB3 @Id and @GeneratedValue annotation this id will be
	 * set automatically by the persistence framework when object is saved.
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	
	/**
	 * Method for hibernate only to read the UUID value as a simple string from the object and persist it (e.g. in a database).
	 * For reading the UUID please use getUuid method
	 * @return String representation of the UUID
	 */
	private String getStrUuid() {
		return this.uuid.toString();
	}
	/**
	 * Method for hibernate only to set the UUID value as a simple string as it was stored in the persistence layer (e.g. a database).
	 * For setting the UUID please use setUuid method
	 */
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
	/**
	 * Sets the timestamp this object was created. 
	 * Most databases cannot store milliseconds, so they are removed by this method.
	 * Caution: We are planning to replace the Calendar class with a different datetime representation which is more suitable for hibernate
	 * see {@link http://dev.e-taxonomy.eu/trac/ticket/247 TRAC ticket} 
	 * 
	 * @param created
	 */
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

	
	/**
	 * Is true if UUID and created timestamp are the same for the passed Object and this one.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
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
	
	/**
	 * Returns the class, id and uuid as a string for any CDM object. 
	 * For example: Taxon#13<b5938a98-c1de-4dda-b040-d5cc5bfb3bc0>
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+"#"+this.getId()+"<"+this.getUuid()+">";
	}
	
}
