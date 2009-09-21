// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.common;

/**
 * Other names/labels/titles (abreviated or not) for the same object (person,
 * reference, source, etc.).
 * It can also be used to store the id, namespace/tablename from an import source into CDM.
 * E.g. when importing SDD data here you may store the filename and the id used in the SDD file here.
 * 
 * @author a.mueller
 * @created 18.09.2009
 * @version 1.0
 */
public interface IOriginalSource<T extends ISourceable> {

	/*************** GETTER /SETTER ************************************/

	/**
	 * Returns the (unique) identifier used in the source.
	 * If the identifier is not unique, {@link #getIdNamespace() namespace} should be defined.
	 * The namespace together with the identifier should be unique.
	 */
	public String getIdInSource();

	/**
	 * @see #getIdInSource()
	 * @param idInSource
	 */
	public void setIdInSource(String idInSource);

	/**
	 * Returns the id namespace. The id namespace is a String that further defines the origin of
	 * the original record. In the combination with the id it should be unique within one a source. 
	 * E.g. if a record comes from table ABC and has the id 345, 'ABC' is a suitable namespace and the 
	 * combination of 'ABC' and 345 is a unique id for this source. 
	 * The namespace is meant to distinguish import records that come from two different tables, elements, objects, ... 
	 * and end up in the same CDM class. In this case the id may not be enough to identify the original record. 
	 * @return the idNamespace
	 */
	public String getIdNamespace();

	/**
	 * @see #getIdNamespace()
	 * @param idNamespace the idNamespace to set
	 */
	public void setIdNamespace(String idNamespace);

	/**
	 * The object this original source is the source for
	 * @return
	 */
	public T getSourcedObj();

	/**
	 * @see #getSourcedObj()
	 * @param sourcedObj
	 */
	public void setSourcedObj(T sourcedObj);

	/**
	 * Returns the micro citation of the according citation. This may be a String
	 * defining a page or a page range, a figure in a citation, etc.
	 * Examples: 'p.345', 'pp.345-367', 'fig. 3a', ...
	 * @return
	 */
	public String getCitationMicroReference();
	
	/**
	 * @see #getCitationMicroReference()
	 * @param microCitation
	 */
	public void setCitationMicroReference(String microCitation);
	
	
}