/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.Set;
import java.util.UUID;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:59:44
 *
 */
public class ReferenceTO extends BaseTO implements IReferenceSTO{
	
	/**
	 * 	URIs like DOIs, LSIDs or Handles for this reference
	 */
	private String uri;
	/**
	 * formatted string containing the entire reference citation including microreference
	 */
	private String fullCitation;
	/**
	 * 
	 */
	private String authorship;
	
	private String citation;
	/**
	 * Details of the nomenclatural reference (protologue). These are mostly (implicitly) pages but can also be figures or
	 * tables or any other element of a publication. {only if a nomenclatural reference exists}
	 */
	private String microReference;
	/**
	 * year of the publication 
	 */
	private String year;

	private Set<MediaSTO> media;


	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getAuthorship() {
		return authorship;
	}

	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}

	public String getCitation() {
		return citation;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

	public String getMicroReference() {
		return microReference;
	}

	public void setMicroReference(String microReference) {
		this.microReference = microReference;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}


	public String getFullCitation() {
		return fullCitation;
	}

	public void setFullCitation(String fullCitation) {
		this.fullCitation = fullCitation;
	}


	public void addMedia(MediaSTO media) {
		this.media.add(media);
	}

	public Set<MediaSTO> getMedia() {
		return media;
	}

	public void setMedia(Set<MediaSTO> media) {
		this.media = media;
	}
}
