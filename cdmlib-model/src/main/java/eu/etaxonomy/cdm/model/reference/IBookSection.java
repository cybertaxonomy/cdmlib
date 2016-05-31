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
 * This interface represents isolated sections (parts or chapters) within a
 * {@link IBook book} or {@link IProceedings proceedings}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "BookSection".
 */
public interface IBookSection extends ISection, INomenclaturalReference{

	/**
	 * Returns this book sections book
	 * @return
	 */
	public IBook getInBook();

	/**
	 * Sets this book sections book.
	 * @param book
	 */
	public void setInBook (IBook book);

}
