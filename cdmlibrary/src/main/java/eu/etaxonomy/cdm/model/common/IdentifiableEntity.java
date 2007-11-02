/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:52
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
	 * @param newVal
	 */
	public void setExtensions(ArrayList newVal){
		extensions = newVal;
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

	public String getLsid(){
		return lsid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLsid(String newVal){
		lsid = newVal;
	}

	public String getTitleCache(){
		return titleCache;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTitleCache(String newVal){
		titleCache = newVal;
	}

	public boolean getHasProtectedTitleCache(){
		return hasProtectedTitleCache;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHasProtectedTitleCache(boolean newVal){
		hasProtectedTitleCache = newVal;
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