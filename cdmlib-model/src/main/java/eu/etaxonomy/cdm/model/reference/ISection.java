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
 * This  interface represents isolated sections (parts, chapters or
 * papers) within a {@link IPrintedUnitBase printed unit}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "SubReference".
 */
public interface ISection extends IReference {
	
	/**
	 * Returns the pages this reference covers in its in-reference.
	 * E.g. if this reference is an article it may cover the pages
	 * 34-45 in the according journal.
	 */
	public String getPages();
	
	/**
	 * Sets the pages that this reference covers in its in-reference
	 * @see #getPages()
	 * @param pages
	 */
	public void setPages(String pages);
	
	/**
	 * Returns the in-reference of this reference.
	 * E.g. if this reference is a book section the according book is returned
	 */
	public Reference getInReference();
	
	/**
	 * Sets the in-reference of this reference.
	 * E.g. if this reference is a book section the according book is set via this
	 * method.
	 */
	public void setInReference(Reference reference);
}
