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

import java.util.Set;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:27
 */
@MappedSuperclass
public abstract class IdentifiableEntity extends AnnotatableEntity implements IOriginalSource {
	public IdentifiableEntity() {
		super();
	}

	static Logger logger = Logger.getLogger(IdentifiableEntity.class);
	private String lsid;
	private String titleCache;
	//if true titleCache will not be automatically generated/updated
	private boolean protectedTitleCache;
	private Set<Rights> rights;
	private Set<Extension> extensions;
	private Set<OriginalSource> sources;

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
		if (protectedTitleCache){
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
		this.setProtectedTitleCache(true);
	}

	@OneToMany
	public Set<Rights> getRights(){
		return this.rights;
	}

	public void setRights(Set<Rights> rights) {
		this.rights = rights;
	}

	/**
	 * @param rights
	 * 
	 * @param right
	 */
	public void addRights(Rights right){
		this.rights.add(right);
	}

	/**
	 * 
	 * @param right
	 */
	public void removeRights(Rights right){
		this.rights.remove(right);
	}

	// (mappedBy="identifiableEntity")
	@OneToMany
	public Set<Extension> getExtensions(){
		return this.extensions;
	}

	public void setExtensions(Set<Extension> extensions) {
		this.extensions = extensions;
	}

	/**
	 * 
	 * @param extension    extension
	 */
	public void addExtension(Extension extension){
		this.extensions.add(extension);
	}

	/**
	 * 
	 * @param extension    extension
	 */
	public void removeExtension(Extension extension){
		this.extensions.remove(extension);
	}

	public boolean isProtectedTitleCache() {
		return protectedTitleCache;
	}

	public void setProtectedTitleCache(boolean protectedTitleCache) {
		this.protectedTitleCache = protectedTitleCache;
	}


	@OneToOne		
	public Set<OriginalSource> getSources() {
		return this.sources;		
	}

	protected void setSources(Set<OriginalSource> sources) {
		this.sources = sources;		
	}

	public void addSource(OriginalSource source) {
		this.sources.add(source);		
	}

	public void removeSource(OriginalSource source) {
		this.sources.remove(source);		
	}

}