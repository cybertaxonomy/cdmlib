/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.validator.constraints.NotEmpty;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.StripHtmlBridge;
import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;
import eu.etaxonomy.cdm.jaxb.LSIDAdapter;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * Superclass for the primary CDM classes that can be referenced from outside via LSIDs and contain a simple generated title string as a label for human reading.
 * All subclasses inherit the ability to store additional properties that are stored as {@link Extension Extensions}, basically a string value with a type term.
 * Any number of right statements can be attached as well as multiple {@link OriginalSourceBase} objects. 
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
    "credits",
    "sources"
})
@MappedSuperclass
public abstract class IdentifiableEntity<S extends IIdentifiableEntityCacheStrategy> extends AnnotatableEntity 
		implements IIdentifiableEntity /*, ISourceable<IdentifiableSource> */ {
	private static final long serialVersionUID = -5610995424730659058L;
	private static final Logger logger = Logger.getLogger(IdentifiableEntity.class);

	@XmlTransient
	public static final boolean PROTECTED = true;
	@XmlTransient
	public static final boolean NOT_PROTECTED = false;
	
	@XmlElement(name = "LSID", type = String.class)
	@XmlJavaTypeAdapter(LSIDAdapter.class)
	@Embedded
	private LSID lsid;
	
	@XmlElement(name = "TitleCache", required = true)
	@XmlJavaTypeAdapter(FormattedTextAdapter.class)
	@Column(length=255, name="titleCache")
	@Fields({@Field(index = org.hibernate.search.annotations.Index.TOKENIZED),
	     	 @Field(name = "titleCache_forSort", index = org.hibernate.search.annotations.Index.UN_TOKENIZED)
	})
	@FieldBridge(impl=StripHtmlBridge.class)
	@Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.ALL)
	@NotEmpty(groups = Level2.class) // implictly NotNull
	@Size(max = 255)
	protected String titleCache;
	
	//if true titleCache will not be automatically generated/updated
	@XmlElement(name = "ProtectedTitleCache")
	protected boolean protectedTitleCache;
	
    @XmlElementWrapper(name = "Rights", nillable = true)
    @XmlElement(name = "Rights")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
	//TODO
	@Merge(MergeMode.ADD_CLONE)
	private Set<Rights> rights;
    
    @XmlElementWrapper(name = "Credits", nillable = true)
    @XmlElement(name = "Credit")
    @IndexColumn(name="sortIndex", base = 0)
	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
    //TODO
	@Merge(MergeMode.ADD_CLONE)
	private List<Credit> credits;
	
    @XmlElementWrapper(name = "Extensions", nillable = true)
    @XmlElement(name = "Extension")
    @OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
	@Merge(MergeMode.ADD_CLONE)
	private Set<Extension> extensions;
	
    @XmlElementWrapper(name = "Sources", nillable = true)
    @XmlElement(name = "IdentifiableSource")
    @OneToMany(fetch = FetchType.LAZY)		
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN})
	@Merge(MergeMode.ADD_CLONE)
	private Set<IdentifiableSource> sources;
    
    @XmlTransient
	@Transient
	protected S cacheStrategy;
	
    protected IdentifiableEntity(){
    	initListener();
    }
    
    protected void initListener(){
    	PropertyChangeListener listener = new PropertyChangeListener() {
        	public void propertyChange(PropertyChangeEvent e) {
        		if (!e.getPropertyName().equals("titleCache") && ! isProtectedTitleCache()){
        			titleCache = null;
        		}
        	}
    	};
    	addPropertyChangeListener(listener);  
    }

	/**
	 * By default, we expect most cdm objects to be abstract things 
	 * i.e. unable to return a data representation.
	 * 
	 * Specific subclasses (e.g. Sequence) can override if necessary.
	 */
	public byte[] getData() {
		return null;
	}

//******************************** CACHE *****************************************************/	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getTitleCache()
	 */
	// @Transient  - must not be transient, since this property needs to to be included in all serializations produced by the remote layer 
	public String getTitleCache(){
		if (protectedTitleCache){
			return this.titleCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (titleCache == null){
			this.titleCache = generateTitle();
			this.titleCache = getTruncatedCache(this.titleCache) ;
		}
		return titleCache;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setTitleCache(java.lang.String)
	 */
	public void setTitleCache(String titleCache){
		setTitleCache(titleCache, PROTECTED);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setTitleCache(java.lang.String, boolean)
	 */
	public void setTitleCache(String titleCache, boolean protectCache){
		titleCache = getTruncatedCache(titleCache);
		this.titleCache = titleCache;
		this.protectedTitleCache = protectCache;
	}
	
	
	/**
	 * @param cache
	 * @return
	 */
	@Transient
	protected String getTruncatedCache(String cache) {
		if (cache != null && cache.length() > 252){
			logger.warn("Truncation of cache: " + this.toString() + "/" + cache);
			cache = cache.substring(0, 252) + "...";
		}
		return cache;
	}
		
//**************************************************************************************
    
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getLsid()
	 */
	public LSID getLsid(){
		return this.lsid;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setLsid(java.lang.String)
	 */
	public void setLsid(LSID lsid){
		this.lsid = lsid;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getRights()
	 */
	public Set<Rights> getRights() {
		if(rights == null) {
			this.rights = new HashSet<Rights>();
		}
		return this.rights;		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addRights(eu.etaxonomy.cdm.model.media.Rights)
	 */
	public void addRights(Rights right){
		getRights().add(right);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeRights(eu.etaxonomy.cdm.model.media.Rights)
	 */
	public void removeRights(Rights right){
		getRights().remove(right);
	}

	
	public List<Credit> getCredits() {
		if(credits == null) {
			this.credits = new ArrayList<Credit>();
		}
		return this.credits;		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getCredits(int)
	 */
	public Credit getCredits(int index){
		return getCredits().get(index);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addCredit(eu.etaxonomy.cdm.model.common.Credit)
	 */
	public void addCredit(Credit credit){
		getCredits().add(credit);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addCredit(eu.etaxonomy.cdm.model.common.Credit, int)
	 */
	public void addCredit(Credit credit, int index){
		getCredits().add(index, credit);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeCredit(eu.etaxonomy.cdm.model.common.Credit)
	 */
	public void removeCredit(Credit credit){
		getCredits().remove(credit);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeCredit(int)
	 */
	public void removeCredit(int index){
		getCredits().remove(index);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getExtensions()
	 */
	public Set<Extension> getExtensions(){
		if(extensions == null) {
			this.extensions = new HashSet<Extension>();
		}
		return this.extensions;
	}

	public void addExtension(String value, ExtensionType extensionType){
		Extension.NewInstance(this, value, extensionType);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#addExtension(eu.etaxonomy.cdm.model.common.Extension)
	 */
	public void addExtension(Extension extension){
		if (extension != null){
			extension.setExtendedObj(this);
			getExtensions().add(extension);
		}
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#removeExtension(eu.etaxonomy.cdm.model.common.Extension)
	 */
	public void removeExtension(Extension extension){
		if (extension != null){
			extension.setExtendedObj(null);
			getExtensions().remove(extension);
		}
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#isProtectedTitleCache()
	 */
	public boolean isProtectedTitleCache() {
		return protectedTitleCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#setProtectedTitleCache(boolean)
	 */
	public void setProtectedTitleCache(boolean protectedTitleCache) {
		this.protectedTitleCache = protectedTitleCache;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ISourceable#getSources()
	 */
	public Set<IdentifiableSource> getSources() {
		if(sources == null) {
			this.sources = new HashSet<IdentifiableSource>();
		}
		return this.sources;		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ISourceable#addSource(eu.etaxonomy.cdm.model.common.OriginalSourceBase)
	 */
	public void addSource(IdentifiableSource source) {
		if (source != null){
			IdentifiableEntity oldSourcedObj = source.getSourcedObj();
			if (oldSourcedObj != null && oldSourcedObj != this){
				oldSourcedObj.getSources().remove(source);
			}
			getSources().add(source);
			source.setSourcedObj(this);
		}
	}
	 
	 /* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.ISourceable#removeSource(eu.etaxonomy.cdm.model.common.IOriginalSource)
	 */
	public void removeSource(IdentifiableSource source) {
		getSources().remove(source);
	}
	
//******************************** TO STRING *****************************************************/	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#toString()
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


	public int compareTo(IdentifiableEntity identifiableEntity) {

		 int result = 0;
		 
		 if (identifiableEntity == null) {
			 throw new NullPointerException("Cannot compare to null.");
		 }

		 // First, compare the name cache.
		 // TODO: Avoid using instanceof operator
		 // Use Class.getDeclaredMethod() instead to find out whether class has getNameCache() method?

		 String specifiedNameCache = "";
		 String thisNameCache = "";
		 String specifiedTitleCache = "";
		 String thisTitleCache = "";
		 String specifiedReferenceTitleCache = "";
		 String thisReferenceTitleCache = "";	
		 
		 if(identifiableEntity instanceof NonViralName) {
			 specifiedNameCache = HibernateProxyHelper.deproxy(identifiableEntity, NonViralName.class).getNameCache();
			 specifiedTitleCache = identifiableEntity.getTitleCache();
			 
		 } else if(identifiableEntity instanceof TaxonBase) {
			 TaxonBase taxonBase = HibernateProxyHelper.deproxy(identifiableEntity, TaxonBase.class);
			 
			 TaxonNameBase<?,?> taxonNameBase = taxonBase.getName();
			 specifiedNameCache = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class).getNameCache();
			 specifiedTitleCache = taxonNameBase.getTitleCache();
			 
			 //specifiedReferenceTitleCache = ((TaxonBase)identifiableEntity).getSec().getTitleCache();
//			 ReferenceBase referenceBase = taxonBase.getSec();
//			 if (referenceBase != null) {
//           FIXME: HibernateProxyHelper.deproxy(referenceBase, ReferenceBase.class) throws exception
//				 referenceBase = HibernateProxyHelper.deproxy(referenceBase, ReferenceBase.class);
//				 specifiedReferenceTitleCache = referenceBase.getTitleCache();
//			 }
		 }
		 
		 if(this instanceof NonViralName) {
			 thisNameCache = HibernateProxyHelper.deproxy(this, NonViralName.class).getNameCache();
			 thisTitleCache = getTitleCache();
		 } else if(this instanceof TaxonBase) {
			 TaxonNameBase<?,?> taxonNameBase= HibernateProxyHelper.deproxy(this, TaxonBase.class).getName();
			 thisNameCache = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class).getNameCache();
			 thisTitleCache = taxonNameBase.getTitleCache();
			 thisReferenceTitleCache = getTitleCache();
		 }
		 
		 // Compare name cache of taxon names

		 if (!specifiedNameCache.equals("") && !thisNameCache.equals("")) {
			 result = thisNameCache.compareTo(specifiedNameCache);
		 }
		 
		 // Compare title cache of taxon names
		 
		 if ((result == 0) && (!specifiedTitleCache.equals("") || !thisTitleCache.equals(""))) {
			 result = thisTitleCache.compareTo(specifiedTitleCache);
		 }
		 
		 // Compare title cache of taxon references
		 
		 if ((result == 0) && (!specifiedReferenceTitleCache.equals("") || !thisReferenceTitleCache.equals(""))) {
			 result = thisReferenceTitleCache.compareTo(specifiedReferenceTitleCache);
		 }
		 
		 return result;
	 }

		/**
		 * Returns the {@link eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy cache strategy} used to generate
		 * several strings corresponding to <i>this</i> identifiable entity
		 * (in particular taxon name caches and author strings).
		 * 
		 * @return  the cache strategy used for <i>this</i> identifiable entity
		 * @see     eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
		 */
		public S getCacheStrategy() {
			return this.cacheStrategy;
		}
		/** 
		 * @see 	#getCacheStrategy()
		 */
		
		public void setCacheStrategy(S cacheStrategy) {
			this.cacheStrategy = cacheStrategy;
		}
		
		public String generateTitle() {
			if (cacheStrategy == null){
				//logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
				return this.getClass() + ": " + this.getUuid();
			}else{
				return cacheStrategy.getTitleCache(this);
			}
		}
	 
//****************** CLONE ************************************************/
	 
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.AnnotatableEntity#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		IdentifiableEntity result = (IdentifiableEntity)super.clone();
		
		//Extensions
		result.extensions = new HashSet<Extension>();
		for (Extension extension : getExtensions() ){
			Extension newExtension = (Extension)extension.clone();
			result.addExtension(newExtension);
		}
		
		//OriginalSources
		result.sources = new HashSet<IdentifiableSource>();
		for (IdentifiableSource source : getSources()){
			IdentifiableSource newSource = (IdentifiableSource)source.clone();
			result.addSource(newSource);
		}
		
		//Rights
		result.rights = new HashSet<Rights>();
        for(Rights rights : getRights()) {
        	result.addRights(rights);
        }

		
		//Rights
		result.credits = new ArrayList<Credit>();
        for(Credit credit : getCredits()) {
        	result.addCredit(credit);
        }

        
		//result.setLsid(lsid);
		//result.setTitleCache(titleCache); 
		//result.setProtectedTitleCache(protectedTitleCache);  //must be after setTitleCache
		
		//no changes to: lsid, titleCache, protectedTitleCache
		
		//empty titleCache
		if (! protectedTitleCache){
			result.titleCache = null;
		}
		return result;
	}
	

}