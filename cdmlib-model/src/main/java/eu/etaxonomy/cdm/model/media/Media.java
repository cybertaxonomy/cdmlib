/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.NotEmpty;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.media.MediaDefaultCacheStrategy;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * A {@link Media media} is any kind of media that represents a media object.
 * This media object can have multiple {@link MediaRepresentation media representations} that differ in MIME-type
 * and/or quality.
 * E.g.
 * (1) an image can have a tiff and a jpg media representation.
 * (2) an formatted text can have a text/html or an application/pdf representation.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Media", propOrder = {
    "title",
    "mediaCreated",
    "description",
    "representations",
    "artist"
})
@XmlRootElement(name = "Media")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Media extends IdentifiableEntity<IIdentifiableEntityCacheStrategy>
        implements IMultiLanguageTextHolder, IIntextReferenceTarget, Cloneable {
    private static final long serialVersionUID = -1927421567263473658L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Media.class);

    // TODO once hibernate annotations support custom collection type
    // private MultilanguageText title = new MultilanguageText();
    @XmlElement(name = "MediaTitle")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="title_mapkey_id")
    @IndexedEmbedded
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE, CascadeType.REFRESH})
    @NotNull
    @NotEmpty(groups = Level2.class)
    private Map<Language,LanguageString> title = new HashMap<>();

    //creation date of the media (not of the record)
    @XmlElement(name ="MediaCreated" )
    @Embedded
    @IndexedEmbedded
    private TimePeriod mediaCreated;


    // TODO once hibernate annotations support custom collection type
    // private MultilanguageText description = new MultilanguageText();
    @XmlElement(name = "MediaDescription")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="description_mapkey_id")
    @JoinTable(name = "Media_Description")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE, CascadeType.REFRESH})
    @IndexedEmbedded
    @NotNull
    private Map<Language,LanguageString> description = new HashMap<>();

    //A single medium such as a picture can have multiple representations in files.
    //Common are multiple resolutions or file formats for images for example
    @XmlElementWrapper(name = "MediaRepresentations")
    @XmlElement(name = "MediaRepresentation")
    @OneToMany(mappedBy="media",fetch = FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.REFRESH})
    @NotNull
    @NotEmpty(groups = Level2.class)
    private Set<MediaRepresentation> representations = new HashSet<>();

    @XmlElement(name = "Artist")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private AgentBase<?> artist;

//************************* FACTORY METHODS *******************************/

    /**
     * Factory method
     * @return
     */
    public static Media NewInstance(){
        return new Media();
    }


    /**
     * Factory method which creates a new media, adds a reprsentation including mime type and suffix information
     * and adds to the later a representation part for a given uri and size
     * Returns <code>null</code> if uri is empty
     * @return Media
     */
    public static Media NewInstance(URI uri, Integer size, String mimeType, String suffix){
    	//TODO improve type recognition
    	Class<? extends MediaRepresentationPart> clazz = null;
    	if (StringUtils.isNotBlank(mimeType)){
    		if (mimeType.matches("image.*")){
    			clazz = ImageFile.class;
    		}
    	}
    	if (StringUtils.isNotBlank(suffix)){
    		if (suffix.matches("\\.(gif|jpe?g|tiff?)")){
    			clazz = ImageFile.class;
    		}
    	}else if (uri != null){
    		if (uri.toString().matches("\\.(gif|jpe?g|tiff?)")){
    			clazz = ImageFile.class;
    		}
    	}
    	MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix, uri, size,clazz);
        if (representation == null){
            return null;
        }
        Media media = new Media();
        media.addRepresentation(representation);
        return media;
    }

//********************************* CONSTRUCTOR **************************/

    /**
     * Constructor
     */
    protected Media() {
        super();
        setMediaCacheStrategy();
    }

    private void setMediaCacheStrategy() {
//      if (getClass() == Media.class){
    	this.cacheStrategy = MediaDefaultCacheStrategy.NewInstance();
//      }
    }

// ********************* GETTER / SETTER    **************************/

    public Set<MediaRepresentation> getRepresentations(){
        if(representations == null) {
            this.representations = new HashSet<MediaRepresentation>();
        }
        return this.representations;
    }

    @SuppressWarnings("deprecation")
    public void addRepresentation(MediaRepresentation representation){
        if (representation != null){
            this.getRepresentations().add(representation);
            representation.setMedia(this);
        }
    }

    @SuppressWarnings("deprecation")
    public void removeRepresentation(MediaRepresentation representation){
        this.getRepresentations().remove(representation);
        if (representation != null){
            representation.setMedia(null);
        }

    }

    public AgentBase getArtist(){
        return this.artist;
    }

    public void setArtist(AgentBase artist){
        this.artist = artist;
    }

//************************ title / title cache *********************************

    public LanguageString getTitle(){
        return getTitle(Language.DEFAULT());
    }

    public LanguageString getTitle(Language language){
        return title.get(language);
    }

    @Transient
    public Map<Language,LanguageString> getAllTitles(){
        if(title == null) {
            this.title = new HashMap<>();
        }
        return this.title;
    }
    /**
     * Adds the languageString to the {@link MultilanguageText multilanguage text}
     * used to be the title of <i>this</i> media.
     *
     * @param title		the languageString with the title in a particular language
     * @see    	   		#getTitle()
     * @see    	   		#putTitle(Language String)
    */
    public void putTitle(LanguageString title){
        this.title.put(title.getLanguage(), title);
    }

    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to be the title of <i>this</i> media.
     *
     * @param language	the language in which the title string is formulated
     * @param text		the title in a particular language
     *
     * @see    	   		#getTitle()
     * @see    	   		#putTitle(LanguageString)
     */
    public void putTitle(Language language, String title){
        this.title.put(language, LanguageString.NewInstance(title, language));
    }

    public void removeTitle(Language language){
        this.title.remove(language);
    }


    @Transient
    public String getTitleCacheByLanguage(Language lang){
        if (cacheStrategy != null){
            return ((MediaDefaultCacheStrategy)cacheStrategy).getTitleCacheByLanguage(this, lang);
        }else{
            return null;
        }

    }


    /**
     * Puts the title into the title field which is a multi-language string
     * with default language as language
     */
    @Override
    public void setTitleCache(String titleCache) {
        putTitle(LanguageString.NewInstance(titleCache, Language.DEFAULT()));
    }

    @Override
    public String getTitleCache(){
        if (protectedTitleCache){
            return this.titleCache;
        }
        // is title dirty, i.e. equal NULL?
        if (titleCache == null){
            this.titleCache = generateTitle();
            this.titleCache = getTruncatedCache(this.titleCache) ;
        }else{
            //do the same as listeners on dependend objects like representations parts
            //are not yet installed
            this.titleCache = generateTitle();
            this.titleCache = getTruncatedCache(this.titleCache) ;
        }
        return titleCache;
    }




    public TimePeriod getMediaCreated(){
        return this.mediaCreated;
    }

    public void setMediaCreated(TimePeriod mediaCreated){
        this.mediaCreated = mediaCreated;
    }

    //************* Descriptions  *********************/

    public Map<Language,LanguageString> getAllDescriptions(){
        if(this.description == null) {
            this.description = new HashMap<>();
        }
        return this.description;
    }

    public LanguageString getDescription(Language language){
        return getAllDescriptions().get(language);
    }

    public void addDescription(LanguageString description){
        this.description.put(description.getLanguage(), description);
    }

    public void putDescription(Language language, String text){
        this.description.put(language, LanguageString.NewInstance(text, language));
    }

    public void removeDescription(Language language){
        this.description.remove(language);
    }

// ************************ SOURCE ***************************/

    public IdentifiableSource addPrimaryMediaSource(Reference citation, String microCitation) {
        if (citation == null && microCitation == null){
            return null;
        }
        IdentifiableSource source = IdentifiableSource.NewPrimaryMediaSourceInstance(citation, microCitation);
        addSource(source);
        return source;
    }

//************************* CLONE **************************/

    @Override
    public Object clone() throws CloneNotSupportedException{
        Media result = (Media)super.clone();
        //description
        result.description = cloneLanguageString(this.description);

        //title
        result.title = cloneLanguageString(this.title);

        //media representations
        result.representations = new HashSet<>();
        for (MediaRepresentation mediaRepresentation: this.representations){
            result.representations.add((MediaRepresentation)mediaRepresentation.clone());
        }
        //no changes to: artist
        return result;
    }

    public int compareTo(Object o) {
        return 0;
    }



}
