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
import org.hibernate.annotations.Index;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import eu.etaxonomy.cdm.model.media.Rights;


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
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifiableEntity", propOrder = {
    "lsid",
    "titleCache",
    "protectedTitleCache",
    "rights",
    "extensions",
    "sources"
})
@MappedSuperclass
public abstract class IdentifiableEntity<T extends IdentifiableEntity> extends AnnotatableEntity<T> implements IOriginalSource, IIdentifiableEntitiy<T> {
	static Logger logger = Logger.getLogger(IdentifiableEntity.class);

	@XmlTransient
	public final boolean PROTECTED = true;
	@XmlTransient
	public final boolean NOT_PROTECTED = false;
	
	@XmlElement(name = "LSID")
	private String lsid;
	
	@XmlElement(name = "TitleCache", required = true)
	private String titleCache;
	
	//if true titleCache will not be automatically generated/updated
	@XmlElement(name = "ProtectedTitleCache")
	private boolean protectedTitleCache;
	
    @XmlElementWrapper(name = "Rights")
    @XmlElement(name = "Rights")
	private Set<Rights> rights = getNewRightsSet();
	
    @XmlElementWrapper(name = "Extensions")
    @XmlElement(name = "Extension")
	private Set<Extension> extensions = getNewExtensionSet();
	
    @XmlElementWrapper(name = "Sources")
    @XmlElement(name = "OriginalSource")
	private Set<OriginalSource> sources = getNewOriginalSourcesSet();

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#getLsid()
	 */
	public String getLsid(){
		return this.lsid;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#setLsid(java.lang.String)
	 */
	public void setLsid(String lsid){
		this.lsid = lsid;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#generateTitle()
	 */
	public abstract String generateTitle();

	//@Index(name="titleCacheIndex")
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#getTitleCache()
	 */
	@Column(length=330)
	public String getTitleCache(){
		if (protectedTitleCache){
			return this.titleCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (titleCache == null){
			this.setTitleCache(generateTitle(),protectedTitleCache) ; //for truncating
		}
		return titleCache;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#setTitleCache(java.lang.String)
	 */
	public void setTitleCache(String titleCache){
		//TODO truncation of title cach
		if (titleCache != null && titleCache.length() > 328){
			logger.warn("Truncation of title cache: " + this.toString());
			titleCache = titleCache.substring(0, 325) + "...";
		}
		this.titleCache = titleCache;
		this.setProtectedTitleCache(PROTECTED);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#setTitleCache(java.lang.String, boolean)
	 */
	public void setTitleCache(String titleCache, boolean protectCache){
		this.titleCache = titleCache;
		this.setProtectedTitleCache(protectCache);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#getRights()
	 */
	@ManyToMany
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Rights> getRights(){
		return this.rights;
	}

	protected void setRights(Set<Rights> rights) {
		this.rights = rights;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#addRights(eu.etaxonomy.cdm.model.media.Rights)
	 */
	public void addRights(Rights right){
		this.rights.add(right);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#removeRights(eu.etaxonomy.cdm.model.media.Rights)
	 */
	public void removeRights(Rights right){
		this.rights.remove(right);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#getExtensions()
	 */
	@OneToMany//(mappedBy="extendedObj")
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<Extension> getExtensions(){
		return this.extensions;
	}
	protected void setExtensions(Set<Extension> extensions) {
		this.extensions = extensions;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#addExtension(eu.etaxonomy.cdm.model.common.Extension)
	 */
	public void addExtension(Extension extension){
		this.extensions.add(extension);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#removeExtension(eu.etaxonomy.cdm.model.common.Extension)
	 */
	public void removeExtension(Extension extension){
		this.extensions.remove(extension);
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#isProtectedTitleCache()
	 */
	public boolean isProtectedTitleCache() {
		return protectedTitleCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#setProtectedTitleCache(boolean)
	 */
	public void setProtectedTitleCache(boolean protectedTitleCache) {
		this.protectedTitleCache = protectedTitleCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#getSources()
	 */
	@OneToMany //(mappedBy="sourcedObj")		
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<OriginalSource> getSources() {
		return this.sources;		
	}
	protected void setSources(Set<OriginalSource> sources) {
		this.sources = sources;		
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#addSource(eu.etaxonomy.cdm.model.common.OriginalSource)
	 */
	public void addSource(OriginalSource source) {
		if (source != null){
			IdentifiableEntity oldSourcedObj = source.getSourcedObj();
			if (oldSourcedObj != null && oldSourcedObj != this){
				oldSourcedObj.getSources().remove(source);
			}
			this.sources.add(source);
			source.setSourcedObj(this);
		}
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#removeSource(eu.etaxonomy.cdm.model.common.OriginalSource)
	 */
	public void removeSource(OriginalSource source) {
		this.sources.remove(source);		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntitiy#toString()
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
	 
//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
	 */
	public Object clone() throws CloneNotSupportedException{
		IdentifiableEntity result = (IdentifiableEntity)super.clone();
		
		//Extensions
		Set<Extension> newExtensions = getNewExtensionSet();
		for (Extension extension : this.extensions ){
			Extension newExtension = (Extension)extension.clone(this);
			newExtensions.add(newExtension);
		}
		result.setExtensions(newExtensions);
		
		//OriginalSources
		Set<OriginalSource> newOriginalSources = getNewOriginalSourcesSet();
		for (OriginalSource originalSource : this.sources){
			OriginalSource newSource = (OriginalSource)originalSource.clone(this);
			newOriginalSources.add(newSource);	
		}
		result.setSources(newOriginalSources);
		
		//Rights
		Set<Rights> rights = getNewRightsSet();
		rights.addAll(this.rights);
		result.setRights(rights);
		
		//result.setLsid(lsid);
		//result.setTitleCache(titleCache); 
		//result.setProtectedTitleCache(protectedTitleCache);  //must be after setTitleCache
		
		//no changes to: lsid, titleCache, protectedTitleCache
		return result;
	}
	
	@Transient
	private Set<Extension> getNewExtensionSet(){
		return new HashSet<Extension>();
	}
	
	@Transient
	private Set<OriginalSource> getNewOriginalSourcesSet(){
		return new HashSet<OriginalSource>();
	}
	
	@Transient
	private Set<Rights> getNewRightsSet(){
		return new HashSet<Rights>();
	}

}