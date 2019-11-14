/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.Set;

import javax.persistence.Transient;

/**
 * Common interface for all type designation subclasses.
 *
 * @author a.mueller
 * @since 07.08.2008
 */
public interface ITypeDesignation {


    /**
     * Shortcut to retrieve the information if the status of
     * <i>this</i> type designation is considered to be a "lectotype" status
     * in the sense that it usually should have a designation reference.
     * For details see Type {@link TypeDesignationStatusBase#hasDesignationSource()}
     *
     * @see  TypeDesignationStatusBase#hasDesignationSource()
     */
    @Transient
    public boolean hasDesignationSource();

//	/**
//	 * Returns the {@link HomotypicalGroup homotypical group} that is typified
//	 * in <i>this</i> type designation.
//	 *
//	 * @see   #getTypeSpecimen()
//   * @deprecated homotypical group can not be set and always seems to be <code>null</code>.
//   * Probably it is a relict of an old version.
//   * See also http://dev.e-taxonomy.eu/trac/ticket/2173
//	 */
//	public HomotypicalGroup getHomotypicalGroup();

	/**
	 * Returns the set of {@link TaxonName taxon names} included in the
	 * {@link HomotypicalGroup homotypical group} typified in <i>this</i> type designation.
	 */
	public Set<TaxonName> getTypifiedNames();

}
