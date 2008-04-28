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
 * Superclass for the primary CDM classes that can be referenced from outside via LSIDs and contain a simple generated title string as a label for human reading.
 * All subclasses inherit the ability to store additional properties that are stored as {@link Extension Extensions}, basically a string value with a type term.
 * Any number of right statements can be attached as well as multiple {@link OriginalSource} objects. 
 * Original sources carry a reference to the source, an ID within that source and the original title/label of this object as it was used in that source (originalNameString).
 * A Taxon for example that was taken from 2 sources like FaunaEuropaea and IPNI would have two originalSource objects.
 * The originalSource representing that taxon as it was found in IPNI would contain IPNI as the reference, the IPNI id of the taxon and the name of the taxon exactly as it was used in IPNI.
 *  
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:27
 */
@MappedSuperclass
public abstract class IdentifiableEntity<T extends IdentifiableEntity> extends AnnotatableEntity<T> implements IOriginalSource {
	static Logger logger = Logger.getLogger(IdentifiableEntity.class);

	public final boolean PROTECTED = true;
	public final boolean NOT_PROTECTED = true;
	
	private String lsid;
	private String titleCache;
	//if true titleCache will not be automatically generated/updated
	private boolean protectedTitleCache;
	private Set<Rights> rights = new HashSet<Rights>();
	private Set<Extension> extensions = new HashSet<Extension>();
	private Set<OriginalSource> sources = new HashSet<OriginalSource>();

	
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
	public void setTitleCache(String titleCache, boolean protectCache){
		this.titleCache = titleCache;
		this.setProtectedTitleCache(protectCache);
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
	
	/**
	 * Overrides {@link eu.etaxonomy.cdm.model.common.CdmBase#toString()}.
	 * This returns an String that identifies the object well without beeing necessarily unique.
	 * Specification: This method should never call other object' methods so it can be well used for debugging 
	 * without problems like lazy loading, unreal states etc.
	 * Note: If overriding this method's javadoc always copy or link the above requirement. 
	 * If not overwritten by a subclass method returns the class, id and uuid as a string for any CDM object. 
	 * For example: Taxon#13<b5938a98-c1de-4dda-b040-d5cc5bfb3bc0>
	 * @see java.lang.Object#toString()
	 */
	 @Override
	public String toString() {
		String result;
		if (titleCache == null){
			result = super.toString();
		}else{
			result = this.titleCache;
		}
		return result;	
	}

}