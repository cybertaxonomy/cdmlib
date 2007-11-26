/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Media extends AnnotatableEntity {
	public Media() {
		super();
		// TODO Auto-generated constructor stub
	}

	static Logger logger = Logger.getLogger(Media.class);
	private MultilanguageSet title = new MultilanguageSet();
	//creation date of the media (not of the record)
	private Calendar mediaCreated;
	private MultilanguageSet description = new MultilanguageSet();
	//A single medium such as a picture can have multiple representations in files. Common are multiple resolutions or file
	//formats for images for example
	private Set<MediaInstance> instances = new HashSet();
	private Set<Rights> rights = new HashSet();
	private Agent artist;

	@OneToMany
	public Set<MediaInstance> getInstances(){
		return this.instances;
	}
	protected void setInstances(Set<MediaInstance> instances){
		this.instances = instances;
	}
	public void addInstance(MediaInstance instance){
		this.instances.add(instance);
	}
	public void removeInstance(MediaInstance instance){
		this.instances.remove(instance);
	}

	
	@ManyToOne
	public Agent getArtist(){
		return this.artist;
	}
	public void setArtist(Agent artist){
		this.artist = artist;
	}


	@ManyToMany
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
	public void addDescription(String text, Language lang){
		this.description.add(text, lang);
	}
	public void removeDescription(Language lang){
		this.description.remove(lang);
	}

}