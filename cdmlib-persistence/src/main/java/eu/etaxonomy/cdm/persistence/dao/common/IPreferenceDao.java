/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.metadata.CdmPreference;
import eu.etaxonomy.cdm.model.metadata.CdmPreference.PrefKey;
import eu.etaxonomy.cdm.model.metadata.IPreferencePredicate;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 2013-09-09
 */
public interface IPreferenceDao {

	/**
	 * Retrieve the value for the given preference key.
	 * @param key
	 * @return
	 */
	public CdmPreference get(CdmPreference.PrefKey key);

	/**
	 * Write the value for the preference's key
	 */
	public void set(CdmPreference preference);

	/**
	 * Return the number of all existing cdm preferences
	 */
	public long count();

    public List<CdmPreference> list();

    public List<CdmPreference> list(IPreferencePredicate<?> predicate);

    /**
     * Retrieves the best matching CdmPreference for the given
     * taxon node and the given predicate.
     *
     * @param taxonNode
     * @param predicate
     * @return the CdmPreference
     */
    public CdmPreference find(TaxonNode taxonNode, String predicate);

    public void remove(PrefKey key);

}
