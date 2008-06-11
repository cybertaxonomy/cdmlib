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

/**
 * @author a.kohlbecker
 * @created 05.06.2008
 * @version 1.0
 */
public class SpecimenSTO extends BaseSTO {
	private String specimenLabel;
	private Set<MediaSTO> media;
	public String getSpecimenLabel() {
		return specimenLabel;
	}
	/**
	 * @param specimenLabel
	 */
	public void setSpecimenLabel(String specimenLabel) {
		this.specimenLabel = specimenLabel;
	}
	public Set<MediaSTO> getMedia() {
		return media;
	}
	public void setMediaRepresentations(
			Set<MediaSTO> media) {
		this.media = media;
	}
	public void addMedia(MediaSTO mediaSTO){
		this.media.add(mediaSTO);
	}
	
	
	
}
