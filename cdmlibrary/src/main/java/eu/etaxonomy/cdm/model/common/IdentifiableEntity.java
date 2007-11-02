/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:10
 */
@MappedSuperclass
public abstract class IdentifiableEntity extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(IdentifiableEntity.class);

	@Description("")
	private String lsid;
	@Description("")
	private String titleCache;
	//if true titleCache will not be automatically generated/updated
	@Description("if true titleCache will not be automatically generated/updated")
	private boolean hasProtectedTitleCache;
	private ArrayList rights;
	private ArrayList extensions;

	public ArrayList getExtensions(){
		return extensions;
	}

	/**
	 * 
	 * @param extensions
	 */
	public void setExtensions(ArrayList extensions){
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

	public String getLsid(){
		return lsid;
	}

	/**
	 * 
	 * @param lsid
	 */
	public void setLsid(String lsid){
		;
	}

	public String getTitleCache(){
		return titleCache;
	}

	/**
	 * 
	 * @param titleCache
	 */
	public void setTitleCache(String titleCache){
		;
	}

	public boolean getHasProtectedTitleCache(){
		return hasProtectedTitleCache;
	}

	/**
	 * 
	 * @param hasProtectedTitleCache
	 */
	public void setHasProtectedTitleCache(boolean hasProtectedTitleCache){
		;
	}

	@Transient
	public String getTitle(){
		return "";
	}

	public abstract String generateTitle();

	public boolean hasProtectedTitleCache(){
		return false;
	}

	@Transient
	public Extension[] getExtensions(){
		return null;
	}

	@Transient
	public Marker[] getMarkers(){
		return null;
	}

	/**
	 * 
	 * @param extension
	 */
	public void addExtension(Extension extension){

	}

	/**
	 * 
	 * @param marker
	 */
	public void addMarker(Marker marker){

	}

	/**
	 * 
	 * @param extension
	 */
	public void removeExtension(Extension extension){

	}

	/**
	 * 
	 * @param marker
	 */
	public void removeMarker(Marker marker){

	}

}