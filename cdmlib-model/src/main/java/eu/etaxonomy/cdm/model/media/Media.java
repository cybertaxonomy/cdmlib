/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IMultiLanguageText;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import java.util.*;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A {@link Media media} is any kind of media that represents a media object. 
 * This media object can have multiple {@link MediaRepresentation media representations} that differ in MIME-type 
 * and/or quality. 
 * E.g. 
 * (1) an image can have a tiff and a jpg media representation. 
 * (2) an formatted text can have a text/html or an application/pdf representation. 
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
@Indexed
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Media extends AnnotatableEntity {
	private static final long serialVersionUID = -1927421567263473658L;
	private static final Logger logger = Logger.getLogger(Media.class);

    // TODO once hibernate annotations support custom collection type
	// private MultilanguageText title = new MultilanguageText();
	@XmlElement(name = "MediaTitle")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @IndexedEmbedded
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.DELETE})
	private Map<Language,LanguageString> title = new HashMap<Language,LanguageString>();
	
	//creation date of the media (not of the record) 
	// FIXME Surely this should be a DateTime, not a Calender
	@XmlElement(name = "MediaCreated")
	@Temporal(TemporalType.DATE)
	private Calendar mediaCreated;
	
	 // TODO once hibernate annotations support custom collection type
	// private MultilanguageText description = new MultilanguageText();
	@XmlElement(name = "MediaDescription")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @IndexedEmbedded
    @JoinTable(name = "Media_Description")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.DELETE})
	private Map<Language,LanguageString> description = new HashMap<Language,LanguageString>();
	
	//A single medium such as a picture can have multiple representations in files. 
	//Common are multiple resolutions or file formats for images for example
	@XmlElementWrapper(name = "MediaRepresentations")
	@XmlElement(name = "MediaRepresentation")
	@OneToMany(mappedBy="media",fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	private Set<MediaRepresentation> representations = new HashSet<MediaRepresentation>();
	
	// FIXME should be OneToMany?
	@XmlElementWrapper(name = "Rights")
	@XmlElement(name = "Right")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Set<Rights> rights = new HashSet<Rights>();
	
	@XmlElement(name = "Artist")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	private AgentBase artist;

	/**
	 * Factory method
	 * @return
	 */
	public static Media NewInstance(){
		logger.debug("NewInstance");
		return new Media();
	}
	
	/**
	 * Constructor
	 */
	protected Media() {
		super();
	}

	public Set<MediaRepresentation> getRepresentations(){
		return this.representations;
	}

	@SuppressWarnings("deprecation")
	public void addRepresentation(MediaRepresentation representation){
		if (representation != null){
			this.getRepresentations().add(representation);
			representation.setMedia(this);
		}
	}
	
	@SuppressWarnings("deprecation")
	public void removeRepresentation(MediaRepresentation representation){
		this.getRepresentations().remove(representation);
		if (representation != null){
			representation.setMedia(null);
		}

	}

	public AgentBase getArtist(){
		return this.artist;
	}
	
	public void setArtist(AgentBase artist){
		this.artist = artist;
	}

	public Set<Rights> getRights(){
		return this.rights;
	}
	
	public void addRights(Rights rights){
		this.rights.add(rights);
	}
	
	public void removeRights(Rights rights){
		this.rights.remove(rights);
	}

	public Map<Language,LanguageString> getTitle(){
		return this.title;
	}
	
	public void addTitle(LanguageString title){
		this.title.put(title.getLanguage(), title);
	}
	
	public void removeTitle(Language language){
		this.title.remove(language);
	}

	public Calendar getMediaCreated(){
		return this.mediaCreated;
	}
	
	public void setMediaCreated(Calendar mediaCreated){
		this.mediaCreated = mediaCreated;
	}

	public Map<Language,LanguageString> getDescription(){
		return this.description;
	}
	
	public void addDescription(LanguageString description){
		this.description.put(description.getLanguage(),description);
	}
	
	public void addDescription(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	
	public void removeDescription(Language language){
		this.description.remove(language);
	}
}