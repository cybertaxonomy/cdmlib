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
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
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
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.NotEmpty;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.search.StripHtmlBridge;
import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;
import eu.etaxonomy.cdm.jaxb.LSIDAdapter;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
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
    "credits",
    "extensions",
    "identifiers",
    "rights",
    "sources"
})
@Audited
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
    @Column(name="titleCache")
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.ALL)
    @NotEmpty(groups = Level2.class) // implictly NotNull
    @Size(max = 800)  //see #1592
    @Fields({
        @Field(store=Store.YES),
        @Field(name = "titleCache__sort", analyze = Analyze.NO, store=Store.YES)
    })
    @FieldBridge(impl=StripHtmlBridge.class)
    protected String titleCache;

    //if true titleCache will not be automatically generated/updated
    @XmlElement(name = "ProtectedTitleCache")
    protected boolean protectedTitleCache;

    @XmlElementWrapper(name = "Rights", nillable = true)
    @XmlElement(name = "Rights")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    //TODO
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private Set<Rights> rights = new HashSet<Rights>();

    @XmlElementWrapper(name = "Credits", nillable = true)
    @XmlElement(name = "Credit")
    @IndexColumn(name="sortIndex", base = 0)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    //TODO
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private List<Credit> credits = new ArrayList<Credit>();

    @XmlElementWrapper(name = "Extensions", nillable = true)
    @XmlElement(name = "Extension")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private Set<Extension> extensions = new HashSet<Extension>();

    @XmlElementWrapper(name = "Identifiers", nillable = true)
    @XmlElement(name = "Identifier")
    @OrderColumn(name="sortIndex")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private List<Identifier> identifiers = new ArrayList<Identifier>();

    @XmlElementWrapper(name = "Sources", nillable = true)
    @XmlElement(name = "IdentifiableSource")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private Set<IdentifiableSource> sources = new HashSet<IdentifiableSource>();

    @XmlTransient
    @Transient
    protected S cacheStrategy;

    protected IdentifiableEntity(){
        initListener();
    }

    @Override
    public void initListener(){
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (! "titleCache".equals(ev.getPropertyName()) && !"cacheStrategy".equals(ev.getPropertyName()) && ! isProtectedTitleCache()){
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
    @Override
    public byte[] getData() {
        return null;
    }

//******************************** CACHE *****************************************************/

    // @Transient  - must not be transient, since this property needs to to be included in all serializations produced by the remote layer
    @Override
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

    @Deprecated
    @Override
    public void setTitleCache(String titleCache){
    	//TODO shouldn't we call setTitleCache(String, boolean),but is this conformant with Java Bean Specification?
    	this.titleCache = getTruncatedCache(titleCache);
    }

    @Override
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
        int maxLength = 800;
    	if (cache != null && cache.length() > maxLength){
            logger.warn("Truncation of cache: " + this.toString() + "/" + cache);
            cache = cache.substring(0, maxLength - 4) + "...";   //TODO do we need -4 or is -3 enough
        }
        return cache;
    }

//**************************************************************************************

    @Override
    public LSID getLsid(){
        return this.lsid;
    }
    @Override
    public void setLsid(LSID lsid){
        this.lsid = lsid;
    }
    @Override
    public Set<Rights> getRights() {
        if(rights == null) {
            this.rights = new HashSet<Rights>();
        }
        return this.rights;
    }

    @Override
    public void addRights(Rights right){
        getRights().add(right);
    }
    @Override
    public void removeRights(Rights right){
        getRights().remove(right);
    }


    @Override
    public List<Credit> getCredits() {
        if(credits == null) {
            this.credits = new ArrayList<Credit>();
        }
        return this.credits;
    }

    @Override
    public Credit getCredits(Integer index){
        return getCredits().get(index);
    }

    @Override
    public void addCredit(Credit credit){
        getCredits().add(credit);
    }


    @Override
    public void addCredit(Credit credit, int index){
        getCredits().add(index, credit);
    }

    @Override
    public void removeCredit(Credit credit){
        getCredits().remove(credit);
    }

    @Override
    public void removeCredit(int index){
        getCredits().remove(index);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.common.IIdentifiableEntity#getIdentifiers()
     */
    @Override
    public List<Identifier> getIdentifiers(){
        if(this.identifiers == null) {
            this.identifiers = new ArrayList<Identifier>();
        }
        return this.identifiers;
    }
    /**
     * @param type
     * @return a set of identifier value strings
     */
    public Set<String> getIdentifiers(DefinedTerm type){
       return getIdentifiers(type.getUuid());
    }
    /**
     * @param identifierTypeUuid
     * @return a set of identifier value strings
     */
    public Set<String> getIdentifiers(UUID identifierTypeUuid){
        Set<String> result = new HashSet<String>();
        for (Identifier identifier : getIdentifiers()){
            if (identifier.getType().getUuid().equals(identifierTypeUuid)){
                result.add(identifier.getIdentifier());
            }
        }
        return result;
    }

    @Override
    public Identifier addIdentifier(String identifier, DefinedTerm identifierType){
    	Identifier result = Identifier.NewInstance(this, identifier, identifierType);
    	return result;
    }

     @Override
    public void addIdentifier(int index, Identifier identifier){
        if (identifier != null){
        	if (identifier.getIdentifiedObj() != null && ! identifier.getIdentifiedObj().equals(this)){
        		identifier.getIdentifiedObj().removeIdentifier(identifier);
        	}
        	identifier.setIdentifiedObj(this);
        	//deduplication
        	int oldIndex = getIdentifiers().indexOf(identifier);
        	if(oldIndex > -1){
        		getIdentifiers().remove(identifier);
        		if (oldIndex < index){
        			index--;
        		}
        	}
        	getIdentifiers().add(index, identifier);
        }
    }

    @Override
    public void addIdentifier(Identifier identifier){
        addIdentifier(getIdentifiers().size(), identifier);
    }

    @Override
    public void removeIdentifier(Identifier identifier){
        if (identifier != null){
        	identifier.setIdentifiedObj(null);
            getIdentifiers().remove(identifier);
        }
    }
    @Override
    public void removeIdentifier(int index){
    	getIdentifiers().remove(index);
    }

    @Override
    public Set<Extension> getExtensions(){
        if(extensions == null) {
            this.extensions = new HashSet<Extension>();
        }
        return this.extensions;
    }
    /**
     * @param type
     * @return a Set of extension value strings
     */
    public Set<String> getExtensions(ExtensionType type){
       return getExtensions(type.getUuid());
    }
    /**
     * @param extensionTypeUuid
     * @return a Set of extension value strings
     */
    public Set<String> getExtensions(UUID extensionTypeUuid){
        Set<String> result = new HashSet<String>();
        for (Extension extension : getExtensions()){
            if (extension.getType().getUuid().equals(extensionTypeUuid)){
                result.add(extension.getValue());
            }
        }
        return result;
    }

    public void addExtension(String value, ExtensionType extensionType){
        Extension.NewInstance(this, value, extensionType);
    }

    @Override
    public void addExtension(Extension extension){
        if (extension != null){
            extension.setExtendedObj(this);
            getExtensions().add(extension);
        }
    }
    @Override
    public void removeExtension(Extension extension){
        if (extension != null){
            extension.setExtendedObj(null);
            getExtensions().remove(extension);
        }
    }

    @Override
    public boolean isProtectedTitleCache() {
        return protectedTitleCache;
    }

    @Override
    public void setProtectedTitleCache(boolean protectedTitleCache) {
        this.protectedTitleCache = protectedTitleCache;
    }

    @Override
    public Set<IdentifiableSource> getSources() {
        if(sources == null) {
            this.sources = new HashSet<IdentifiableSource>();
        }
        return this.sources;
    }

    @Override
    public void addSource(IdentifiableSource source) {
        if (source != null){
            IdentifiableEntity<?> oldSourcedObj = source.getSourcedObj();
            if (oldSourcedObj != null && oldSourcedObj != this){
                oldSourcedObj.getSources().remove(source);
            }
            getSources().add(source);
            source.setSourcedObj(this);
        }
    }

    @Override
    public void addSources(Set<IdentifiableSource> sources) {
        if (sources != null){
        	for (IdentifiableSource source: sources){
	            IdentifiableEntity<?> oldSourcedObj = source.getSourcedObj();
	            if (oldSourcedObj != null && oldSourcedObj != this){
	                oldSourcedObj.getSources().remove(source);
	            }
	            getSources().add(source);
	            source.setSourcedObj(this);
        	}
        }
    }

    @Override
    public void removeSources() {
       this.sources.clear();
    }

    @Override
    public IdentifiableSource addSource(OriginalSourceType type, String id, String idNamespace, Reference citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        IdentifiableSource source = IdentifiableSource.NewInstance(type, id, idNamespace, citation, microCitation);
        addSource(source);
        return source;
    }


    @Override
    public IdentifiableSource addImportSource(String id, String idNamespace, Reference<?> citation, String microCitation) {
        if (id == null && idNamespace == null && citation == null && microCitation == null){
            return null;
        }
        IdentifiableSource source = IdentifiableSource.NewInstance(OriginalSourceType.Import, id, idNamespace, citation, microCitation);
        addSource(source);
        return source;
    }


    @Override
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
         String thisGenusString = "";
         String specifiedGenusString = "";
         int thisrank_order = 0;
         final String HYBRID_SIGN = "\u00D7";
         final String QUOT_SIGN = "[\\u02BA\\u0022\\u0022]";
         //TODO we can remove all the deproxies here except for the first one
         identifiableEntity = HibernateProxyHelper.deproxy(identifiableEntity, IdentifiableEntity.class);
         if(identifiableEntity instanceof NonViralName) {
             specifiedNameCache = HibernateProxyHelper.deproxy(identifiableEntity, NonViralName.class).getNameCache();
             specifiedTitleCache = identifiableEntity.getTitleCache();
            if (identifiableEntity instanceof BotanicalName){
            	 if (((BotanicalName)identifiableEntity).isAutonym()){
            		 boolean isProtected = false;
            		 String oldNameCache = ((BotanicalName) identifiableEntity).getNameCache();
            		 if ( ((BotanicalName)identifiableEntity).isProtectedNameCache()){
            			 isProtected = true;
            		 }
            		 ((BotanicalName)identifiableEntity).setProtectedNameCache(false);
            		 ((BotanicalName)identifiableEntity).setNameCache(null, false);
            		 specifiedNameCache = ((BotanicalName) identifiableEntity).getNameCache();
            		 ((BotanicalName)identifiableEntity).setNameCache(oldNameCache, isProtected);

            	 }
             }

         } else if(identifiableEntity instanceof TaxonBase) {
             TaxonBase taxonBase = HibernateProxyHelper.deproxy(identifiableEntity, TaxonBase.class);

             TaxonNameBase<?,?> taxonNameBase = taxonBase.getName();


             NonViralName nonViralName = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class);
             specifiedNameCache = nonViralName.getNameCache();
             specifiedTitleCache = taxonNameBase.getTitleCache();

             specifiedReferenceTitleCache = ((TaxonBase)identifiableEntity).getSec().getTitleCache();
             Reference reference = taxonBase.getSec();
             if (reference != null) {
                 reference = HibernateProxyHelper.deproxy(reference, Reference.class);
                 specifiedReferenceTitleCache = reference.getTitleCache();
             }
         }

         if(this.isInstanceOf(NonViralName.class)) {
             thisNameCache = HibernateProxyHelper.deproxy(this, NonViralName.class).getNameCache();
             thisTitleCache = getTitleCache();

             if (this instanceof BotanicalName){
            	 if (((BotanicalName)this).isAutonym()){
            		 boolean isProtected = false;
            		 String oldNameCache = ((BotanicalName) this).getNameCache();
            		 if ( ((BotanicalName)this).isProtectedNameCache()){
            			 isProtected = true;
            		 }
            		 ((BotanicalName)this).setProtectedNameCache(false);
            		 ((BotanicalName)this).setNameCache(null, false);
            		 thisNameCache = ((BotanicalName) this).getNameCache();
            		 ((BotanicalName)this).setNameCache(oldNameCache, isProtected);
            	 }
             }
         } else if(this.isInstanceOf(TaxonBase.class)) {
             TaxonNameBase<?,?> taxonNameBase= HibernateProxyHelper.deproxy(this, TaxonBase.class).getName();
             NonViralName nonViralName = HibernateProxyHelper.deproxy(taxonNameBase, NonViralName.class);
             thisNameCache = nonViralName.getNameCache();
             thisTitleCache = taxonNameBase.getTitleCache();
             thisReferenceTitleCache = ((TaxonBase)this).getSec().getTitleCache();
             thisGenusString = nonViralName.getGenusOrUninomial();
         }

         // Compare name cache of taxon names



         if (!specifiedNameCache.equals("") && !thisNameCache.equals("")) {

        	 thisNameCache = thisNameCache.replaceAll(HYBRID_SIGN, "");
        	 thisNameCache = thisNameCache.replaceAll(QUOT_SIGN, "");


        	 specifiedNameCache = specifiedNameCache.replaceAll(HYBRID_SIGN, "");
        	 specifiedNameCache = specifiedNameCache.replaceAll(QUOT_SIGN, "");


             result = thisNameCache.compareTo(specifiedNameCache);
         }

         // Compare title cache of taxon names

         if ((result == 0) && (!specifiedTitleCache.equals("") || !thisTitleCache.equals(""))) {
        	 thisTitleCache = thisTitleCache.replaceAll(HYBRID_SIGN, "");
        	 thisTitleCache = thisTitleCache.replaceAll(QUOT_SIGN, "");

        	 specifiedTitleCache = specifiedTitleCache.replaceAll(HYBRID_SIGN, "");
        	 specifiedTitleCache = specifiedTitleCache.replaceAll(QUOT_SIGN, "");
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

    @Override
    public String generateTitle() {
        if (getCacheStrategy() == null){
            //logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
            return this.getClass() + ": " + this.getUuid();
        }else{
            return getCacheStrategy().getTitleCache(this);
        }
    }

//****************** CLONE ************************************************/

    @Override
    public Object clone() throws CloneNotSupportedException{
        IdentifiableEntity<?> result = (IdentifiableEntity<?>)super.clone();

        //Extensions
        result.extensions = new HashSet<Extension>();
        for (Extension extension : getExtensions() ){
            Extension newExtension = (Extension)extension.clone();
            result.addExtension(newExtension);
        }

        //Identifier
        result.identifiers = new ArrayList<Identifier>();
        for (Identifier<?> identifier : getIdentifiers() ){
        	Identifier<?> newIdentifier = (Identifier<?>)identifier.clone();
            result.addIdentifier(newIdentifier);
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


        //Credits
        result.credits = new ArrayList<Credit>();
        for(Credit credit : getCredits()) {
            result.addCredit(credit);
        }

        //no changes to: lsid, titleCache, protectedTitleCache

        //empty titleCache
        if (! protectedTitleCache){
            result.titleCache = null;
        }

        result.initListener();
        return result;
    }


}