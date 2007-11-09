/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.agent.Team;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
 */
@Entity
public class Media extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(Media.class);
	private MultilanguageArray title;
	//creation date of the media (not of the record)
	private Calendar mediaCreated;
	private MultilanguageArray description;
	//A single medium such as a picture can have multiple representations in files. Common are multiple resolutions or file
	//formats for images for example
	private ArrayList instances;
	private ArrayList rights;
	private Team artist;

	public ArrayList getInstances(){
		return this.instances;
	}

	/**
	 * 
	 * @param instances    instances
	 */
	public void setInstances(ArrayList instances){
		this.instances = instances;
	}

	public Team getArtist(){
		return this.artist;
	}

	/**
	 * 
	 * @param artist    artist
	 */
	public void setArtist(Team artist){
		this.artist = artist;
	}

	public ArrayList getRights(){
		return this.rights;
	}

	/**
	 * 
	 * @param rights    rights
	 */
	public void setRights(ArrayList rights){
		this.rights = rights;
	}

	public MultilanguageArray getTitle(){
		return this.title;
	}

	/**
	 * 
	 * @param title    title
	 */
	public void setTitle(MultilanguageArray title){
		this.title = title;
	}

	public Calendar getMediaCreated(){
		return this.mediaCreated;
	}

	/**
	 * 
	 * @param mediaCreated    mediaCreated
	 */
	public void setMediaCreated(Calendar mediaCreated){
		this.mediaCreated = mediaCreated;
	}

	public MultilanguageArray getDescription(){
		return this.description;
	}

	/**
	 * 
	 * @param description    description
	 */
	public void setDescription(MultilanguageArray description){
		this.description = description;
	}

}