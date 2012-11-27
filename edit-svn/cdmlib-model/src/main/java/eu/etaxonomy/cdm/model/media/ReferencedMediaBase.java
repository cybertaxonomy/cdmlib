/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:48
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferencedMedia", propOrder = {
    "citationMicroReference",
    "citation"
})
@XmlRootElement(name = "ReferencedMedia")
@Entity
@Audited
public abstract class ReferencedMediaBase extends Media implements IReferencedEntity {
	private static final long serialVersionUID = 4118655992311004088L;
	static Logger logger = Logger.getLogger(ReferencedMediaBase.class);
	
	@XmlElement(name = "CitationMicroReference")
	private String citationMicroReference;
	
	@XmlElement(name = "Citation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Reference<?> citation;

	public Reference<?> getCitation(){
		return this.citation;
	}
	
	public void setCitation(Reference<?> citation){
		this.citation = citation;
	}

	public String getCitationMicroReference(){
		return this.citationMicroReference;
	}
	
	public void setCitationMicroReference(String citationMicroReference){
		this.citationMicroReference = citationMicroReference;
	}

}