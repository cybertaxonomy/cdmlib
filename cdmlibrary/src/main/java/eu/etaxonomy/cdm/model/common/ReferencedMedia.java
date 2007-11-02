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
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:28
 */
@Entity
public abstract class ReferencedMedia extends Media implements IReferencedEntity {
	static Logger logger = Logger.getLogger(ReferencedMedia.class);

	@Description("")
	private String citationMicroReference;
	private ReferenceBase citation;

	public ReferenceBase getCitation(){
		return citation;
	}

	/**
	 * 
	 * @param citation
	 */
	public void setCitation(ReferenceBase citation){
		;
	}

	public String getCitationMicroReference(){
		return citationMicroReference;
	}

	/**
	 * 
	 * @param citationMicroReference
	 */
	public void setCitationMicroReference(String citationMicroReference){
		;
	}

	@Transient
	public String getCitation(){
		return "";
	}

	@Transient
	public StrictReferenceBase getCitation(){
		return null;
	}

}