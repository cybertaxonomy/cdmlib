package eu.etaxonomy.cdm.model.common;

import java.util.UUID;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.Person;

public interface ICdmBase {

	/**
	 * Returns local unique identifier for the concrete subclass
	 * @return
	 */
	public abstract int getId();

	/**
	 * Assigns a unique local ID to this object. 
	 * Because of the EJB3 @Id and @GeneratedValue annotation this id will be
	 * set automatically by the persistence framework when object is saved.
	 * @param id
	 */
	public abstract void setId(int id);

	public abstract UUID getUuid();

	public abstract void setUuid(UUID uuid);

	public abstract DateTime getCreated();

	/**
	 * Sets the timestamp this object was created. 
	 * Most databases cannot store milliseconds, so they are removed by this method.
	 * Caution: We are planning to replace the Calendar class with a different datetime representation which is more suitable for hibernate
	 * see {@link http://dev.e-taxonomy.eu/trac/ticket/247 TRAC ticket} 
	 * 
	 * @param created
	 */
	public abstract void setCreated(DateTime created);

	public abstract Person getCreatedBy();

	public abstract void setCreatedBy(Person createdBy);

}