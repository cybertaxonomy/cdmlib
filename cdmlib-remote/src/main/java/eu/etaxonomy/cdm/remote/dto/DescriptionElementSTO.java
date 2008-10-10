/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto;

import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * DescriptionElementSTO is used to represent {@linkplain QuantitativeData}, {@linkplain CategoricalData}, {@linkplain TextData} or {@linkplain CommonTaxonName}
 * @author m.doering
 *
 */
public class DescriptionElementSTO extends BaseSTO {
		
	private Set<MediaSTO> media = new HashSet<MediaSTO>();
	private String description;
	private String language;
	private ReferenceSTO reference;
	private LocalisedTermSTO areaTerm;
	private LocalisedTermSTO mediaType;
	private String classType;
	
	public Set<MediaSTO> getMedia() {
		return media;
	}
	public void setMedia(Set<MediaSTO> media) {
		this.media = media;
	}
	public void addMedia(MediaSTO media) {
		this.media.add(media);
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public ReferenceSTO getReference() {
		return reference;
	}
	public void setReference(ReferenceSTO reference) {
		this.reference = reference;
	}
	public void setArea(LocalisedTermSTO area) {
		this.areaTerm = area;
	}
	public LocalisedTermSTO getArea() {
		return areaTerm;
	}
	public void setMediaType(LocalisedTermSTO mediaType) {
		this.mediaType = mediaType;
	}
	public LocalisedTermSTO getMediaType() {
		return mediaType;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}	
}
