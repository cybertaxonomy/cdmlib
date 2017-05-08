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

/**
 * @author a.mueller
 * @created 07.08.2008
 * @version 1.0
 */
public interface ITypeDesignation {


	/**
	 * Returns the boolean value indicating whether <i>this</i> type
	 * designation has a "lectotype" status (true) or not (false).<BR>
	 * A lectotype is a type designated as the
	 * nomenclatural type, when no holotype was indicated at the time of
	 * publication of the "type-bringing" {@link TaxonName taxon name}, when the
	 * holotype is found to belong to more than one taxon name,
	 * or as long as it is missing.
	 *
	 * @see  SpecimenTypeDesignationStatus#isLectotype()
	 * @see  SpecimenTypeDesignationStatus#HOLOTYPE()
	 * @see  NameTypeDesignationStatus#isLectotype()
	 */

	public boolean isLectoType();

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
