/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import eu.etaxonomy.cdm.jaxb.MultilanguageSetAdapter;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageSet;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Media", propOrder = {
    "title",
    "mediaCreated",
    "description",
    "representations",
    "rights",
    "artist"
})
@XmlRootElement(name = "Media")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Media extends AnnotatableEntity {
	
	private static final Logger logger = Logger.getLogger(Media.class);

	@XmlElement(name = "MediaTitle")
    @XmlJavaTypeAdapter(MultilanguageSetAdapter.class)
	private MultilanguageSet title = new MultilanguageSet();
	
	//creation date of the media (not of the record)
	@XmlElement(name = "MediaCreated")
	private Calendar mediaCreated;
	
	@XmlElement(name = "MediaDescription")
    @XmlJavaTypeAdapter(MultilanguageSetAdapter.class)
	private MultilanguageSet description = new MultilanguageSet();
	
	//A single medium such as a picture can have multiple representations in files. 
	//Common are multiple resolutions or file formats for images for example
	@XmlElementWrapper(name = "MediaRepresentations")
	@XmlElement(name = "MediaRepresentation")
	private Set<MediaRepresentation> representations = new HashSet<MediaRepresentation>();
	
	@XmlElementWrapper(name = "Rights")
	@XmlElement(name = "Right")
	private Set<Rights> rights = new HashSet<Rights>();
	
	@XmlElement(name = "Artist")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Agent artist;

	/**
	 * Factory method
	 * @return
	 */
	public static Media NewInstance(){
		return new Media();
	}
	
	
	/**
	 * Constructor
	 */
	protected Media() {
		super();
	}

	@OneToMany(mappedBy="media")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<MediaRepresentation> getRepresentations(){
		return this.representations;
	}
	protected void setRepresentations(Set<MediaRepresentation> representations){
		this.representations = representations;
	}
	public void addRepresentation(MediaRepresentation representation){
		if (representation != null){
			this.getRepresentations().add(representation);
			representation.setMedia(this);
		}
	}
	public void removeRepresentation(MediaRepresentation representation){
		this.getRepresentations().remove(representation);
		if (representation != null){
			representation.setMedia(null);
		}

	}

	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getArtist(){
		return this.artist;
	}
	public void setArtist(Agent artist){
		this.artist = artist;
	}


	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Rights> getRights(){
		return this.rights;
	}
	protected void setRights(Set<Rights> rights){
		this.rights = rights;
	}
	public void addRights(Rights rights){
		this.rights.add(rights);
	}
	public void removeRights(Rights rights){
		this.rights.remove(rights);
	}

	
	public MultilanguageSet getTitle(){
		return this.title;
	}
	public void setTitle(MultilanguageSet title){
		this.title = title;
	}

	@Temporal(TemporalType.DATE)
	public Calendar getMediaCreated(){
		return this.mediaCreated;
	}
	public void setMediaCreated(Calendar mediaCreated){
		this.mediaCreated = mediaCreated;
	}

	
	public MultilanguageSet getDescription(){
		return this.description;
	}
	protected void setDescription(MultilanguageSet description){
		this.description = description;
	}
	public void addDescription(LanguageString description){
		this.description.add(description);
	}
	public void addDescription(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	public void removeDescription(Language language){
		this.description.remove(language);
	}

}