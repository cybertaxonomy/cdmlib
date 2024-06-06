/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.UUID;

import javax.persistence.Transient;
import javax.validation.GroupSequence;
import javax.validation.groups.Default;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;

@GroupSequence({Default.class, Level2.class, Level3.class})
public interface ICdmBase {

	/**
	 * Returns local unique identifier for the concrete subclass
	 * @return
	 */
	public int getId();

	/**
	 * Assigns a unique local ID to this object.
	 * Because of the EJB3 @Id and @GeneratedValue annotation this id will be
	 * set automatically by the persistence framework when object is saved.
	 * @param id
	 */
	public void setId(int id);

	public UUID getUuid();

	public void setUuid(UUID uuid);

	public DateTime getCreated();

	/**
	 * Sets the timestamp this object was created.
	 * Most databases cannot store milliseconds, so they are removed by this method.
	 * Caution: We are planning to replace the Calendar class with a different datetime representation which is more suitable for hibernate
	 * see {@link https://dev.e-taxonomy.eu/redmine/issues/247 TRAC ticket}
	 *
	 * @param created
	 */
	public void setCreated(DateTime created);

	/**
	 * @return The {@link User} who was authenticated when the entity was created.
	 * Can be <code>null</code> if the entity has been created without any user authentication present.
	 */
	public User getCreatedBy();

	public void setCreatedBy(User createdBy);

	/**
     * Tests if the object is assignable from the type
     * defined as parameter <code>clazz</code>, regardless
     * whether it is an actual entity or hibernate proxy.
     */
    public boolean isInstanceOf(Class<? extends CdmBase> clazz);

    /**
     * Reports the persistend state of the cdm entity. If this method returns <code>true</code> it does not
     * necessarily mean that there is a database record already but
     * maybe the object was only saved to a persistence session, which still needs to be flushed to the storage engine.
     *
     * @return if and only if the instance has been saved to the persistent storage.
     * For new and un-persisted entities this method returns false.
     */
    @Transient
    public boolean isPersisted();
}