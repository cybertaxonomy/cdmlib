/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * A {@link Media media} is any kind of media that represents a media object. 
 * This media object can have multiple {@link MediaRepresentation media representations} that differ in MIME-type 
 * and/or quality. 
 * E.g. 
 * (1) an image can have a tiff and a jpg media representation. 
 * (2) an formatted text can have a text/html or an application/pdf representation. 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:34
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
@Indexed
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class Media extends IdentifiableEntity implements Cloneable {
	private static final long serialVersionUID = -1927421567263473658L;
	private static final Logger logger = Logger.getLogger(Media.class);

    // TODO once hibernate annotations support custom collection type
	// private MultilanguageText title = new MultilanguageText();
	@XmlElement(name = "MediaTitle")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @IndexedEmbedded
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE, CascadeType.DELETE_ORPHAN, CascadeType.REFRESH})
    @NotNull
    @NotEmpty(groups = Level2.class)
	private Map<Language,LanguageString> title = new HashMap<Language,LanguageString>();
	
	//creation date of the media (not of the record) 
	@XmlElement(name = "MediaCreated", type= String.class)
	@XmlJavaTypeAdapter(DateTimeAdapter.class)
	@Type(type="dateTimeUserType")
	@Basic(fetch = FetchType.LAZY)
	private DateTime mediaCreated;
	
	 // TODO once hibernate annotations support custom collection type
	// private MultilanguageText description = new MultilanguageText();
	@XmlElement(name = "MediaDescription")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @IndexedEmbedded
    @JoinTable(name = "Media_Description")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE,CascadeType.DELETE,CascadeType.DELETE_ORPHAN, CascadeType.REFRESH})
    @NotNull
	private Map<Language,LanguageString> description = new HashMap<Language,LanguageString>();
	
	//A single medium such as a picture can have multiple representations in files. 
	//Common are multiple resolutions or file formats for images for example
	@XmlElementWrapper(name = "MediaRepresentations")
	@XmlElement(name = "MediaRepresentation")
	@OneToMany(mappedBy="media",fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN, CascadeType.REFRESH})
	@NotNull
	@NotEmpty(groups = Level2.class)
	private Set<MediaRepresentation> representations = new HashSet<MediaRepresentation>();
	
	@XmlElement(name = "Artist")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	private AgentBase artist;


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
	public static Media NewInstance(String uri, Integer size, String mimeType, String suffix){
		MediaRepresentation representation = MediaRepresentation.NewInstance(mimeType, suffix, uri, size);
		if (representation == null){
			return null;
		}
		Media media = new Media();
		media.addRepresentation(representation);
		return media;
	}
	
	/**
	 * Constructor
	 */
	protected Media() {
		super();
	}

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

	@Deprecated // will be removed in next release; use getAllTitles instead
	public Map<Language,LanguageString> getTitle(){
		return getAllTitles();
	}
	
	public Map<Language,LanguageString> getAllTitles(){
		if(title == null) {
			this.title = new HashMap<Language,LanguageString>();
		}
		return this.title;
	}
	
	public LanguageString getTitle(Language language){
		return getAllTitles().get(language);
	}
	
	public void addTitle(LanguageString title){
		this.title.put(title.getLanguage(), title);
	}
	
	public void removeTitle(Language language){
		this.title.remove(language);
	}

	public DateTime getMediaCreated(){
		return this.mediaCreated;
	}
	
	public void setMediaCreated(DateTime mediaCreated){
		this.mediaCreated = mediaCreated;
	}

	@Deprecated // will be removed in next release; use getAllDescriptions instead
	public Map<Language,LanguageString> getDescription(){
		return getAllDescriptions();
	}
	
	public Map<Language,LanguageString> getAllDescriptions(){
		if(this.description == null) {
			this.description = new HashMap<Language,LanguageString>();
		}
		return this.description;
	}
	
	public LanguageString getDescription(Language language){
		return getAllDescriptions().get(language);
	}
	
	public void addDescription(LanguageString description){
		this.description.put(description.getLanguage(), description);
	}
	
	public void addDescription(String text, Language language){
		this.description.put(language, LanguageString.NewInstance(text, language));
	}
	
	public void removeDescription(Language language){
		this.description.remove(language);
	}
	
//************************* CLONE **************************/
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		Media result = (Media)super.clone();
		//description
		result.description = new HashMap<Language, LanguageString>();
		for (Language language: this.description.keySet()){
			result.description.put(language, this.description.get(language));
		}
		//title
		result.title = new HashMap<Language, LanguageString>();
		for (Language language: this.title.keySet()){
			result.title.put(language, this.title.get(language));
		}
		//media representations
		result.representations = new HashSet<MediaRepresentation>();
		for (MediaRepresentation mediaRepresentation: this.representations){
			result.representations.add((MediaRepresentation)mediaRepresentation.clone());
		}
		//no changes to: artist
		return result;
	}
	
	public int compareTo(Object o) {
		return 0;
	}
	/**
	 * @param mimeTypeRegexes
	 * @param size
	 * @param widthOrDuration
	 * @param height
	 * @return
	 * 
	 * 
	 */
	public MediaRepresentation findBestMatchingRepresentation(Integer size, Integer height, Integer widthOrDuration, String[] mimeTypes){
		// find best matching representations of each media
		Set<MediaRepresentation> reps = this.getRepresentations();
				
		List<Media> returnMedia = new ArrayList<Media>(reps.size());
		SortedMap<Integer, MediaRepresentation> prefRepresentations 
				= orderMediaRepresentations(mimeTypes, size, widthOrDuration, height);
			try {
				// take first one and remove all other representations
				MediaRepresentation prefOne = prefRepresentations.get(prefRepresentations.firstKey());
				
				return prefOne;
				
			} catch (NoSuchElementException nse) {
				/* IGNORE */
			}
			return null;
		}
	
	
	/**
	 * @param mimeTypeRegexes
	 * @param size
	 * @param widthOrDuration
	 * @param height
	 * @return
	 * 
	 * 
	 */
	private SortedMap<Integer, MediaRepresentation> orderMediaRepresentations(String[] mimeTypeRegexes,
			Integer size, Integer widthOrDuration, Integer height) {
		SortedMap<Integer, MediaRepresentation> prefRepr = new TreeMap<Integer, MediaRepresentation>();
		SortedMap<String, MediaRepresentation> sortedForSizeDistance = new TreeMap<String, MediaRepresentation>();		
		String keyString = "";
		for (String mimeTypeRegex : mimeTypeRegexes) {
			// getRepresentationByMimeType
			Pattern mimeTypePattern = Pattern.compile(mimeTypeRegex);
			int representationCnt = 0;
			for (MediaRepresentation representation : getRepresentations()) {
				
				Matcher mather = mimeTypePattern.matcher(representation.getMimeType());
				if (mather.matches()) {
					int dwa = 0;
					
					//first the size is used for comparison
					for (MediaRepresentationPart part : representation.getParts()) {
						if (part.getSize()!= null){
							int sizeOfPart = part.getSize();
							int distance = sizeOfPart - size;
							if (distance < 0) {
								distance*= -1;
							}
							dwa += distance;
						}
						//if height and width/duration is defined, add this information, too
						if (height != 0 && widthOrDuration != 0){
							int dw = 0;
							
							if (part instanceof ImageFile) {
								ImageFile image = (ImageFile) part;
								dw = image.getWidth() * image.getHeight() - height * widthOrDuration;
							}
							else if (part instanceof MovieFile){
								MovieFile movie = (MovieFile) part;
								dw = movie.getDuration() - widthOrDuration;
										
							}else if (part instanceof AudioFile){
								AudioFile audio = (AudioFile) part;
								dw = audio.getDuration() - widthOrDuration;
								
							}
							if (dw < 0) {
								dw *= -1;
							}
							dwa += dw;
							
						}
					}
					dwa = (representation.getParts().size() > 0 ? dwa / representation.getParts().size() : 0);
					
					//keyString =(dwa + representationCnt++) + '_' + representation.getMimeType();
					
					prefRepr.put((dwa + representationCnt++), representation);
					System.out.println(prefRepr.get(prefRepr.firstKey()) + " --- " + prefRepr.firstKey());
				}
					
			}				
						
		}
		return prefRepr;
	}
	
	/*
	 * Overriding the title cache methods here to avoid confusion with the title field
	 */
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 */
	@Override
	public String getTitleCache() {
		List<Language> languages = Arrays.asList(new Language[]{Language.DEFAULT()});
		LanguageString languageString = MultilanguageTextHelper.getPreferredLanguageString(title, languages);
		return languageString != null ? languageString.getText() : null;
	}
	
	@Override
	public String generateTitle() {
		return getTitleCache();
	}
	
	/*
	 * (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#setTitleCache(java.lang.String)
	 */
	@Override
	public void setTitleCache(String titleCache) {
		addTitle(LanguageString.NewInstance(titleCache, Language.DEFAULT()));
	}
	
}