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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:27
 */
@MappedSuperclass
public abstract class IdentifiableEntity<T extends IdentifiableEntity> extends AnnotatableEntity<T> implements IOriginalSource {
	static Logger logger = Logger.getLogger(IdentifiableEntity.class);

	private String lsid;
	private String titleCache;
	//if true titleCache will not be automatically generated/updated
	private boolean protectedTitleCache;
	private Set<Rights> rights = new HashSet();
	private Set<Extension> extensions = new HashSet();
	private Set<OriginalSource> sources = new HashSet();

	
	public String getLsid(){
		return this.lsid;
	}
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
	public void setTitleCache(String titleCache){
		this.titleCache = titleCache;
		this.setProtectedTitleCache(true);
	}

	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Rights> getRights(){
		return this.rights;
	}

	protected void setRights(Set<Rights> rights) {
		this.rights = rights;
	}
	public void addRights(Rights right){
		this.rights.add(right);
	}
	public void removeRights(Rights right){
		this.rights.remove(right);
	}

	@OneToMany//(mappedBy="extendedObj")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Extension> getExtensions(){
		return this.extensions;
	}
	protected void setExtensions(Set<Extension> extensions) {
		this.extensions = extensions;
	}
	public void addExtension(Extension extension){
		this.extensions.add(extension);
	}
	public void removeExtension(Extension extension){
		this.extensions.remove(extension);
	}

	
	public boolean isProtectedTitleCache() {
		return protectedTitleCache;
	}

	public void setProtectedTitleCache(boolean protectedTitleCache) {
		this.protectedTitleCache = protectedTitleCache;
	}


	@OneToMany //(mappedBy="sourcedObj")		
	@Cascade({CascadeType.SAVE_UPDATE})
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