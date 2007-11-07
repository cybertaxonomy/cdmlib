package eu.etaxonomy.cdm.model.common;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;

public abstract class CdmBase {

	private PropertyChangeSupport support = new PropertyChangeSupport(this);

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

}
