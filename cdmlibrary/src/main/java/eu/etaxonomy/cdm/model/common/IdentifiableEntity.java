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
 * @created 08-Nov-2007 13:06:27
 */
@MappedSuperclass
public abstract class IdentifiableEntity extends AnnotatableEntity {
	static Logger logger = Logger.getLogger(IdentifiableEntity.class);
	private String lsid;
	private String titleCache;
	//if true titleCache will not be automatically generated/updated
	private boolean hasProtectedTitleCache;
	private ArrayList<Rights> rights;
	private ArrayList<Extension> extensions;

	public String getLsid(){
		return this.lsid;
	}

	/**
	 * 
	 * @param lsid    lsid
	 */
	public void setLsid(String lsid){
		this.lsid = lsid;
	}

	public abstract String generateTitle();

	public String getTitleCache(){
		if (hasProtectedTitleCache){
			return this.titleCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (titleCache == null){
			this.titleCache = generateTitle();
		}
		return titleCache;
	}

	/**
	 * 
	 * @param titleCache    titleCache
	 */
	public void setTitleCache(String titleCache){
		this.titleCache = titleCache;
		this.setHasProtectedTitleCache(true);
	}

	/**
	 * 
	 * @param hasProtectedTitleCache    hasProtectedTitleCache
	 */
	public void setHasProtectedTitleCache(boolean hasProtectedTitleCache){
		this.hasProtectedTitleCache = hasProtectedTitleCache;
	}

	public boolean hasProtectedTitleCache(){
		return false;
	}


public ArrayList<Rights> getRights(){
		return this.rights;
	}

	/**
	 * @param rights
	 * 
	 * @param right
	 */
	public void addRights(Rights right){

	}

	/**
	 * 
	 * @param right
	 */
	public void removeRights(Rights right){

	}

	public ArrayList<Extension> getExtensions(){
		return this.extensions;
	}

	/**
	 * 
	 * @param extension    extension
	 */
	public void addExtension(Extension extension){

	}

	/**
	 * 
	 * @param extension    extension
	 */
	public void removeExtension(Extension extension){

	}

}