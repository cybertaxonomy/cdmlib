/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import etaxonomy.cdm.model.agent.Team;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:59
 */
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
	 * @param newVal
	 */
	public void setInstances(ArrayList newVal){
		instances = newVal;
	}

	public Team getArtist(){
		return artist;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setArtist(Team newVal){
		artist = newVal;
	}

	public ArrayList getRights(){
		return rights;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRights(ArrayList newVal){
		rights = newVal;
	}

	public MultilanguageString getTitle(){
		return title;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTitle(MultilanguageString newVal){
		title = newVal;
	}

	public Calendar getMediaCreated(){
		return mediaCreated;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMediaCreated(Calendar newVal){
		mediaCreated = newVal;
	}

	public MultilanguageString getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(MultilanguageString newVal){
		description = newVal;
	}

}