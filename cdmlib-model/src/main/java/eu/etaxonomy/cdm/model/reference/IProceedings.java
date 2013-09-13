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
* This interface represents conference proceedings. Proceedings are a
* collection of academic papers that are published in the context of an
* academic conference. Each paper typically is quite isolated from the other
* papers in the proceedings. Proceedings are published in-house, by the
* organizing institution of the conference, or via an academic publisher. 
* <P>
* This class corresponds, according to the TDWG ontology, to the publication type
* term (from PublicationTypeTerm): "ConferenceProceedings".
*/
public interface IProceedings extends IPrintedUnitBase{

	/**
	 * Returns the organization which published this reference
	 */
	public String getOrganization();
	
	/**
	 * Sets the organization which published this reference
	 */
	public void setOrganization(String organization);
}
