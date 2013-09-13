/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.agent.Institution;

/**
 * This interface represents a thesis. A thesis is a document that presents the
 * author's research and findings and is submitted at a
 * {@link eu.etaxonomy.cdm.model.agent.Institution high school institution} in support of candidature for
 * a degree or professional qualification.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Thesis".
 */
public interface IThesis extends IPublicationBase{
	
	/**
	 * Returns the school which published this thesis
	 */
	public Institution getSchool();
	
	/**
	 * Sets the school which published this thesis
	 * @param school
	 */
	public void setSchool(Institution school);
}
