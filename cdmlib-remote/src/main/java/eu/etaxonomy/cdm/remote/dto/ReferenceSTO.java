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
 * @created 05.02.2008 14:58:36
 *
 */
public class ReferenceSTO extends BaseSTO {

	private String authorship;

	/**
	 * formatted string containing the entire reference citation including microreference
	 */
	private String fullCitation;

	private Set<IdentifiedString> mediaUri;

	public String getAuthorship() {
		return authorship;
	}

	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}

	public String getFullCitation() {
		return fullCitation;
	}

	public void setFullCitation(String fullCitation) {
		this.fullCitation = fullCitation;
	}

	public Set<IdentifiedString> getMediaUri() {
		return mediaUri;
	}

	public void setMediaUri(Set<IdentifiedString> mediaUri) {
		this.mediaUri = mediaUri;
	}
	public void addMediaUri(String mediaUri, UUID mediaUUID) {
		this.mediaUri.add(new IdentifiedString(mediaUri, mediaUUID.toString()));
	}
	public void addMediaUri(String mediaUri, String mediaUUID) {
		this.mediaUri.add(new IdentifiedString(mediaUri, mediaUUID));
	}

}
