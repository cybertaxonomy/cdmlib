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
import java.util.UUID;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 05.02.2008 14:58:36
 *
 */
public class ReferenceSTO extends BaseSTO implements IReferenceSTO {

	private String authorship;

	/**
	 * formatted string containing the entire reference citation including microreference
	 */
	private String fullCitation;

	private Set<MediaSTO> media = new HashSet<MediaSTO>();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.IReferenceSTO#getAuthorship()
	 */
	public String getAuthorship() {
		return authorship;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.IReferenceSTO#setAuthorship(java.lang.String)
	 */
	public void setAuthorship(String authorship) {
		this.authorship = authorship;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.IReferenceSTO#getFullCitation()
	 */
	public String getFullCitation() {
		return fullCitation;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.IReferenceSTO#setFullCitation(java.lang.String)
	 */
	public void setFullCitation(String fullCitation) {
		this.fullCitation = fullCitation;
	}

	
	public Set<MediaSTO> getMedia() {
		return media;
	}
	public void setMedia(Set<MediaSTO> media) {
		this.media = media;
	}

	public void addMedia(MediaSTO media) {
		this.media.add(media);
	}


}
