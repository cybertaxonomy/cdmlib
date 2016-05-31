/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;

/**
 * This interface represents journals. A journal is a periodical
 * {@link IPublicationBase publication} containing several {@link IArticle articles}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Journal".
 */
public interface IJournal extends IPublicationBase{

	/**
	 * Returns the ISSN (International Standard Serial Number)
	 */
	public String getIssn();

	/**
	 * Sets the ISSN (International Standard Serial Number)
	 * @param issn
	 */
	public void setIssn(String issn);

}
