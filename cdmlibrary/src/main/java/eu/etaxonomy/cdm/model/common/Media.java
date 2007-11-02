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
 * @created 02-Nov-2007 19:36:16
 */
@Entity
public class Media extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(Media.class);

	@Description("")
	private MultilanguageString title;
	//creation date of the media (not of the record)
	@Description("creation date of the media (not of the record)")
	private Calendar mediaCreated;
	@Description("")
	private MultilanguageString description;
	/**
	 * A single medium such as a picture can have multiple representations in files.
	 * Common are multiple resolutions or file formats for images for example
	 */
	private ArrayList instances;
	private ArrayList rights;
	private Team artist;

	public ArrayList getInstances(){
		return instances;
	}

	/**
	 * 
	 * @param instances
	 */
	public void setInstances(ArrayList instances){
		;
	}

	public Team getArtist(){
		return artist;
	}

	/**
	 * 
	 * @param artist
	 */
	public void setArtist(Team artist){
		;
	}

	public ArrayList getRights(){
		return rights;
	}

	/**
	 * 
	 * @param rights
	 */
	public void setRights(ArrayList rights){
		;
	}

	public MultilanguageString getTitle(){
		return title;
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(MultilanguageString title){
		;
	}

	public Calendar getMediaCreated(){
		return mediaCreated;
	}

	/**
	 * 
	 * @param mediaCreated
	 */
	public void setMediaCreated(Calendar mediaCreated){
		;
	}

	public MultilanguageString getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(MultilanguageString description){
		;
	}

}