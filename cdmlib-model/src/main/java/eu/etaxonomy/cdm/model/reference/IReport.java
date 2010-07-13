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
 * This interface represents reports. A report is a document characterized by 
 * information reflective of inquiry or investigation. Reports often address
 * questions posed by individuals in government or science and are generally
 * elaborated within an {@link eu.etaxonomy.cdm.model.agent.Institution institution}.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * term (from PublicationTypeTerm): "Report".
 */
public interface IReport extends IPublicationBase{
	
	/**
	 * Returns the institution that published this report
	 */
	public Institution getInstitution();
	
	/**
	 * Sets the institution that published this report
	 */
	public void setInstitution(Institution institution);

}
