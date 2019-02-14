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
 * This interface represents books. A book is a  {@link IPrintedUnitBase printed unit}
 * usually published by a publishing company.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "Book"
 * <li> "EditedBook"
 * </ul>
 */
public interface IBook extends IPrintedUnitBase, IWithIsbn, INomenclaturalReference{

	/**
	 * Returns this books edition
	 */
	public String getEdition();

	/**
	 * Sets this books edition
	 * @param edition
	 */
	public void setEdition(String edition);


}
