package eu.etaxonomy.cdm.model.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;


@Entity
@Table(name="CDM_OBJECT")
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class CdmBase implements Serializable{
	
	public CdmBase() {
		// TODO Auto-generated constructor stub
	}
	private PropertyChangeSupport support = new PropertyChangeSupport(this);
	private int id;

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		support.removePropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void hasListeners(String propertyName) {
		support.hasListeners(propertyName);
	}

	public void firePropertyChange(String property, String oldval, String newval) {
		support.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, int oldval, int newval) {
		support.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, float oldval, float newval) {
		support.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, boolean oldval, boolean newval) {
		support.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(String property, Object oldval, Object newval) {
		support.firePropertyChange(property, oldval, newval);
	}
	public void firePropertyChange(PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	@Id
	@GeneratedValue(generator = "system-increment")
	public int getId() {
		return this.id;
	}

	/**
	 * 
	 * @param id    id
	 */
	public void setId(int id) {
		this.id = id;
	}

}
