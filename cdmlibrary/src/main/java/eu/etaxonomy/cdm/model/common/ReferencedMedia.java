/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:48
 */
@Entity
public abstract class ReferencedMedia extends Media implements IReferencedEntity {
	static Logger logger = Logger.getLogger(ReferencedMedia.class);
	private String citationMicroReference;
	private ReferenceBase citation;

	@ManyToOne
	public ReferenceBase getCitation(){
		return this.citation;
	}
	public void setCitation(ReferenceBase citation){
		this.citation = citation;
	}

	public String getCitationMicroReference(){
		return this.citationMicroReference;
	}
	public void setCitationMicroReference(String citationMicroReference){
		this.citationMicroReference = citationMicroReference;
	}

}