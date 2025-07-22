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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.search.StripHtmlBridge;
import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;
import eu.etaxonomy.cdm.jaxb.LSIDAdapter;
import eu.etaxonomy.cdm.model.media.ExternalLink;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.reference.ICdmTarget;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.IdentifierType;
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
 * Original sources carry a reference to the source, an ID within that source and the original title/label of this object as it was used in that source (originalInfo).
 * A Taxon for example that was taken from 2 sources like FaunaEuropaea and IPNI would have two originalSource objects.
 * The originalSource representing that taxon as it was found in IPNI would contain IPNI as the reference, the IPNI id of the taxon and the name of the taxon exactly as it was used in IPNI.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:27
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IdentifiableEntity", propOrder = {
    "lsid",
    "titleCache",
    "protectedTitleCache",
    "credits",
    "extensions",
    "identifiers",
    "links",
    "rights"
})
@Audited
@MappedSuperclass
public abstract class IdentifiableEntity<S extends IIdentifiableEntityCacheStrategy>
        extends SourcedEntityBase<IdentifiableSource>
        implements IIdentifiableEntity /*, ISourceable<IdentifiableSource> */ {

    private static final long serialVersionUID = 7912083412108359559L;
    private static final Logger logger = LogManager.getLogger();

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
    @Column(name="titleCache", length=800) //see #1592
    @Match(value=MatchMode.CACHE, cacheReplaceMode=ReplaceMode.ALL)
    @NotEmpty(groups = Level2.class) // implicitly NotNull
    @Fields({
        @Field(store=Store.YES),
        //  If the field is only needed for sorting and nothing else, you may configure it as
        //  un-indexed and un-stored, thus avoid unnecessary index growth.
        @Field(name = "titleCache__sort", analyze = Analyze.NO, store=Store.NO, index = Index.NO)
    })
    @SortableField(forField = "titleCache__sort")
    @FieldBridge(impl=StripHtmlBridge.class)
    protected String titleCache;

    //if true titleCache will not be automatically generated/updated
    @XmlElement(name = "ProtectedTitleCache")
    protected boolean protectedTitleCache;

    @XmlElementWrapper(name = "Rights", nillable = true)
    @XmlElement(name = "Rights")
    @ManyToMany(fetch = FetchType.LAZY)  //#5762 M:N now
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    //TODO
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private Set<Rights> rights = new HashSet<>();

    @XmlElementWrapper(name = "Credits", nillable = true)
    @XmlElement(name = "Credit")
    @OrderColumn(name="sortIndex")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    //TODO
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private List<Credit> credits = new ArrayList<>();

    @XmlElementWrapper(name = "Extensions", nillable = true)
    @XmlElement(name = "Extension")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private Set<Extension> extensions = new HashSet<>();

    @XmlElementWrapper(name = "Identifiers", nillable = true)
    @XmlElement(name = "Identifier")
    @OrderColumn(name="sortIndex")
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    @NotNull
    private List<Identifier> identifiers = new ArrayList<>();

    @XmlElementWrapper(name = "Links", nillable = true)
    @XmlElement(name = "Link")
    @OneToMany(fetch=FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    @Merge(MergeMode.ADD_CLONE)
    private Set<ExternalLink> links = new HashSet<>();

    @XmlTransient
    @Transient
    protected IIdentifiableEntityCacheStrategy cacheStrategy;

    protected IdentifiableEntity(){
        initListener();
    }

    @Override
    public void initListener(){
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (! "titleCache".equals(ev.getPropertyName())
                        && !"cacheStrategy".equals(ev.getPropertyName())
                        && ! isProtectedTitleCache()){
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
        //removed due to #5849
//        if(StringUtils.isBlank(titleCache)){
//            titleCache = this.toString();
//        }
        return titleCache;
    }

    @Deprecated
    @Override
    public void setTitleCache(String titleCache){
    	//TODO shouldn't we call setTitleCache(String, boolean), but is this conformant with JavaBean specification?
    	this.titleCache = getTruncatedCache(titleCache);
    }

    @Override
    public void setTitleCache(String titleCache, boolean protectCache){
        titleCache = getTruncatedCache(titleCache);
        this.titleCache = titleCache;
        this.protectedTitleCache = protectCache;
    }

    @Override
    public String resetTitleCache() {
        if(!protectedTitleCache){
            titleCache = null;
        }
        return getTitleCache();
    }

    @Transient
    protected String getTruncatedCache(String cache) {
        int maxLength = 800;
    	if (cache != null && cache.length() > maxLength){
            logger.warn("Truncation of cache: " + this.toString() + "/" + cache);
            cache = cache.substring(0, maxLength - 4) + "...";   //TODO do we need -4 or is -3 enough
        }
        return cache;
    }

    @Override
    public boolean isProtectedTitleCache() {
        return protectedTitleCache;
    }

    @Override
    public void setProtectedTitleCache(boolean protectedTitleCache) {
        this.protectedTitleCache = protectedTitleCache;
    }

    /**
     * @return true, if the current state of the titleCache (without generating it new)
     * is <code>null</code> or the empty string. This is primarily meant for internal use.
     */
    public boolean hasEmptyTitleCache(){
        return this.titleCache == null || "".equals(this.titleCache);
    }

    public boolean updateCaches(){
        if (this.protectedTitleCache == false){
            String oldTitleCache = this.titleCache;

            @SuppressWarnings("unchecked")
            String newTitleCache = cacheStrategy().getTitleCache(this);

            if ( oldTitleCache == null   || ! oldTitleCache.equals(newTitleCache) ){
                this.setTitleCache(null, false);
                String newCache = this.getTitleCache();

                if (newCache == null){
                    logger.warn("newCache should never be null");
                }
                if (oldTitleCache == null){
                    logger.info("oldTitleCache was illegaly null and has been fixed");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the caches with the given cache strategy
     * @param entityCacheStrategy
     * @return <code>true</code> if some cache was updated, <code>false</code> otherwise
     */
    public boolean updateCaches(S entityCacheStrategy){
        S oldCacheStrategy = this.cacheStrategy();
        this.cacheStrategy = entityCacheStrategy != null? entityCacheStrategy : this.cacheStrategy();
        boolean result = this.updateCaches();
        this.cacheStrategy = oldCacheStrategy;
        return result;
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

//************* RIGHTS *************************************

    public Set<Rights> getRights() {
        if(rights == null) {
            this.rights = new HashSet<>();
        }
        return this.rights;
    }
    public void addRights(Rights right){
        getRights().add(right);
    }
    public void removeRights(Rights right){
        getRights().remove(right);
    }


//********************** External Links **********************************************

    public Set<ExternalLink> getLinks(){
        return this.links;
    }
    public void setLinks(Set<ExternalLink> links){
        this.links = links;
    }
    public void addLink(ExternalLink link){
        if (link != null){
            links.add(link);
        }
    }
    public ExternalLink addLinkWebsite(URI uri, String description, Language descriptionLanguage){
        ExternalLink link = null;
        if (uri != null || description != null || descriptionLanguage != null){
            link = ExternalLink.NewWebSiteInstance(uri, description, descriptionLanguage);
            links.add(link);
        }
        return link;
    }
    public void removeLink(ExternalLink link){
        if(links.contains(link)) {
            links.remove(link);
        }
    }

//********************** CREDITS **********************************************

    public List<Credit> getCredits() {
        if(credits == null) {
            this.credits = new ArrayList<>();
        }
        return this.credits;
    }

    public Credit getCredits(Integer index){
        return getCredits().get(index);
    }

    public void addCredit(Credit credit){
        getCredits().add(credit);
    }

    public void addCredit(Credit credit, int index){
        getCredits().add(index, credit);
    }

    public void removeCredit(Credit credit){
        getCredits().remove(credit);
    }

    public void removeCredit(int index){
        getCredits().remove(index);
    }

    public boolean replaceCredit(Credit newObject, Credit oldObject){
        return replaceInList(this.credits, newObject, oldObject);
    }

//************ IDENTIFIERS ********************************************/

    @Override
    public List<Identifier> getIdentifiers(){
        if(this.identifiers == null) {
            this.identifiers = new ArrayList<>();
        }
        return this.identifiers;
    }
    /**
     * @return a set of identifier value strings
     */
    public Set<String> getIdentifierStrings(IdentifierType type){
       return getIdentifierStrings(type == null? null :type.getUuid());
    }
    /**
     * @param identifierTypeUuid
     * @return a set of identifier value strings
     */
    public Set<String> getIdentifierStrings(UUID identifierTypeUuid){
        Set<String> result = new HashSet<>();
        for (Identifier identifier : getIdentifiers()){
            if ( (identifier.getType()== null && identifierTypeUuid == null)
                || (identifier.getType().getUuid().equals(identifierTypeUuid))){
                result.add(identifier.getIdentifier());
            }
        }
        return result;
    }
    /**
     * Returns the first identifier value of the given type.
     * <code>null</code> if no such identifier exists.
     * @param identifierTypeUuid
     */
    public String getIdentifierString(UUID identifierTypeUuid){
        Set<Identifier> set = getIdentifiers(identifierTypeUuid);
        return set.isEmpty()? null : set.iterator().next().getIdentifier();
    }
    /**
     * Returns the first identifier of the given type.
     * <code>null</code> if no such identifier exists.
     * @param identifierTypeUuid
     */
    public Identifier getIdentifier(UUID identifierTypeUuid){
        Set<Identifier> set = getIdentifiers(identifierTypeUuid);
        return set.isEmpty()? null : set.iterator().next();
    }

    public Set<Identifier> getIdentifiers(UUID identifierTypeUuid){
        Set<Identifier> result = new HashSet<>();
        for (Identifier identifier : getIdentifiers()){
            if ( (identifier.getType() == null && identifierTypeUuid == null)
                || (identifier.getType() != null && identifier.getType().getUuid().equals(identifierTypeUuid))){
                result.add(identifier);
            }
        }
        return result;
    }

    @Override
    public Identifier addIdentifier(String identifier, IdentifierType identifierType){
    	Identifier result = Identifier.NewInstance(identifier, identifierType);
    	addIdentifier(result);
    	return result;
    }

    @Override
    public void addIdentifier(Integer index, Identifier identifier){
        if (identifier != null){
        	//deduplication
        	int oldIndex = getIdentifiers().indexOf(identifier);
        	if(oldIndex > -1){
        		getIdentifiers().remove(identifier);
        		if (index != null && oldIndex < index){
        			index--;
        		}
        	}

        	if (index != null){
        	    getIdentifiers().add(index, identifier);
        	}else{
        	    getIdentifiers().add(identifier);
        	}
        }
    }

    @Override
    public void addIdentifier(Identifier identifier){
        addIdentifier(null, identifier);
    }

    @Override
    public void removeIdentifier(Identifier identifier){
        if (identifier != null){
            getIdentifiers().remove(identifier);
        }
    }
    @Override
    public void removeIdentifier(int index){
    	getIdentifiers().remove(index);
    }

    @Override
    public boolean replaceIdentifier(Identifier newObject, Identifier oldObject){
        return replaceInList(this.identifiers, newObject, oldObject);
    }


    @Override
    public Set<Extension> getExtensions(){
        if(extensions == null) {
            this.extensions = new HashSet<>();
        }
        return this.extensions;
    }
    public Set<Extension> getFilteredExtensions(UUID extensionTypeUuid){
        Set<Extension> result = new HashSet<>();
        for (Extension extension : getExtensions()){
            if (extension.getType() != null && extension.getType().getUuid().equals(extensionTypeUuid)){
                result.add(extension);
            }
        }
        return result;
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
     * @see #hasExtension(UUID, String)
     */
    public Set<String> getExtensions(UUID extensionTypeUuid){
        Set<String> result = new HashSet<>();
        for (Extension extension : getExtensions()){
            if (extension.getType() != null && extension.getType().getUuid().equals(extensionTypeUuid)){
                result.add(extension.getValue());
            }
        }
        return result;
    }

    /**
     * @see #getExtensionsConcat(Collection, String)
     */
    public String getExtensionsConcat(UUID extensionTypeUuid, String separator){
        String result = null;
        for (Extension extension : getExtensions()){
            if (extension.getType() != null && extension.getType().getUuid().equals(extensionTypeUuid)){
                result = CdmUtils.concat(separator, result, extension.getValue());
            }
        }
        return result;
    }

    /**
     * Return all extensions matching the given extension type as
     * concatenated string. If extensionTypeUuids is a sorted collection
     * it is given in the correct order.
     * @param extensionTypeUuids collection of the extension types to be considered
     * @param separator the separator for concatenation
     * @return the concatenated extension string
     * @see #getExtensionsConcat(Collection, String)
     */
    public String getExtensionsConcat(Collection<UUID> extensionTypeUuids, String separator){
        String result = null;
        for (UUID uuid : extensionTypeUuids){
            String extension = getExtensionsConcat(uuid, separator);
            result = CdmUtils.concat(separator, result, extension);
        }
        return result;
    }

    /**
     * Has this entity an extension of given type with value 'value'.
     * If value is <code>null</code> <code>true</code> is returned if
     * an Extension exists with given type and 'value' is <code>null</code>.
     * @param extensionTypeUuid
     * @param value
     * @see #hasExtension(ExtensionType, String)
     * @see #getExtensions(UUID)
     */
    public boolean hasExtension(UUID extensionTypeUuid, String value) {
        for (String ext : this.getExtensions(extensionTypeUuid)){
            if (CdmUtils.nullSafeEqual(ext, value)){
                return true;
            }
        }
        return false;
    }

    /**
     * @see #hasExtension(UUID, String)
     */
    public boolean hasExtension(ExtensionType extensionType, String value) {
        return hasExtension(extensionType.getUuid(), value);
    }

    @Override
    public void addExtension(String value, ExtensionType extensionType){
        Extension.NewInstance(this, value, extensionType);
    }

    @Override
    public void addExtension(Extension extension){
        if (extension != null){
            getExtensions().add(extension);
        }
    }
    @Override
    public void removeExtension(Extension extension){
        if (extension != null){
            getExtensions().remove(extension);
        }
    }

    @Override
    public void addSource(IdentifiableSource source) {
        if (source != null){
            getSources().add(source);
        }
    }

    @Override
    public void addSources(Set<IdentifiableSource> sources) {
        if (sources != null){
        	for (IdentifiableSource source: sources){
	            getSources().add(source);
        	}
        }
    }

    @Override
    protected IdentifiableSource createNewSource(OriginalSourceType type, String idInSource, String idNamespace,
            Reference reference, String microReference, String originalInfo, ICdmTarget target) {
        return IdentifiableSource.NewInstance(type, idInSource, idNamespace, reference, microReference, originalInfo, target);
    }

//******************************** TO STRING *****************************************************/

    @Override
    public String toString() {
        String result;
        if (isBlank(titleCache)){
            result = super.toString();
        }else{
            result = this.titleCache;
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
    @Transient
    @java.beans.Transient
    public S cacheStrategy() {
        if (this.cacheStrategy == null){
            initDefaultCacheStrategy();
        }
        return (S)this.cacheStrategy;
    }
    public void setCacheStrategy(S cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    @Override
    public String generateTitle() {
        if (cacheStrategy() == null){
            //logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
            return this.getClass() + ": " + this.getUuid();
        }else{
            S cacheStrategy = cacheStrategy();
            return cacheStrategy.getTitleCache(this);
        }
    }

    /**
     * Subclasses should implement setting the default cache strategy
     */
    protected abstract void initDefaultCacheStrategy();

//****************** CLONE ************************************************/

    @Override
    public IdentifiableEntity<S> clone() throws CloneNotSupportedException{

        @SuppressWarnings("unchecked")
        IdentifiableEntity<S> result = (IdentifiableEntity<S>)super.clone();

        //Extensions
        result.extensions = new HashSet<>();
        for (Extension extension : getExtensions() ){
            Extension newExtension = extension.clone();
            result.addExtension(newExtension);
        }

        //Identifier
        result.identifiers = new ArrayList<>();
        for (Identifier identifier : getIdentifiers() ){
        	Identifier newIdentifier = identifier.clone();
            result.addIdentifier(newIdentifier);
        }

        //Rights  - reusable since #5762
        result.rights = new HashSet<>();
        for(Rights right : getRights()) {
            result.addRights(right);
        }

        //Credits
        result.credits = new ArrayList<>();
        for(Credit credit : getCredits()) {
            Credit newCredit = credit.clone();
            result.addCredit(newCredit);
        }

        //Links
        result.links = new HashSet<>();
        for(ExternalLink link : getLinks()) {
            ExternalLink newLink = link.clone();
            result.addLink(newLink);
        }

        //no changes to: lsid, titleCache, protectedTitleCache

        //empty titleCache
        if (! protectedTitleCache){
            result.titleCache = null;
        }

        result.initListener();  //TODO why do we need this, isnt't the listener in constructor enough?
        return result;
    }
}