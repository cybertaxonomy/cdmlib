/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.common;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * An original source can be used in different ways.<BR>
 * 1.) As a referencing system. The original source holds all information about the reference,
 * the microReference (page, figure, ...), the identifier used in the source for the referenced object in the
 * reference, a namespace that makes this identifier unique, the original name string that in
 * general stores the representation of the referenced object within the source (if the string representation
 * in the source differs from that one unsed in the CDM object)
 *
 * 2.) Dataprovenance: When importing data from another datasource important information like the identifier
 * and it's namespace (e.g. tablename) as well as the datasource itself maybe stored in an original source.
 * E.g. when importing SDD data here you may store the filename and the id used in the SDD file here.
 *
 * Starting with v3.3 we started to let this class be influenced by the PROV Ontology
 * (http://www.w3.org/TR/2013/NOTE-prov-overview-20130430/). It may move further in this direction in
 * future.
 * The above mentioned ways to use the Original Source are now related to different activity types
 * (http://www.w3.org/TR/2013/REC-prov-dm-20130430/#term-Activity) in PROV.
 *
 *
 * @author a.mueller
 * @created 18.09.2009
 */
public interface IOriginalSource<T extends ISourceable> {

	/*************** GETTER /SETTER ************************************/


	/**
	 * Returns the {@link OriginalSourceType type} of the source. The type defines the relationship
	 * between the current data and the original data.
	 * @see OriginalSourceType
	 * @return the type of the source
	 */
	public OriginalSourceType getType();

	/**
	 * @see #getType()
	 * @param type the type to be set
	 */
	public void setType(OriginalSourceType type);

	/**
	 * Returns the (unique) identifier used in the source.
	 * If the identifier is not unique, {@link #getIdNamespace() namespace} should be defined.
	 * The namespace together with the identifier should be unique.
	 * @return  the identifier used in the source.
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


	/**
	 * Returns the citation.
	 * @return
	 */
	public Reference getCitation();

	/**
	 * @see #getCitation()
	 * @param citation
	 */
	public void setCitation(Reference citation);

}
