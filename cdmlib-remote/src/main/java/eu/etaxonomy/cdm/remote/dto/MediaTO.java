/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 12:13:42
 *
 */
public class MediaTO extends BaseTO implements IBaseSTO{

	/**
	 * The title of the Media instance
	 */
	private String title;
	
	/**
	 * The description of the Media.
	 */
	private String description;
	/**
	 * A single medium such as a picture can have multiple representations. 
	 * Common are multiple resolutions or file
	 * formats for images for example
	 */
	
	private Set<MediaRepresentationSTO> representations = new HashSet<MediaRepresentationSTO>();
	/**
	 * several rightTOs in the SAME language, not different languages for the SAME rights UUID
	 */
	private Set<RightsSTO> rights;
	private IdentifiedString artist;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setTitle(LanguageString title) {
		if(title != null){
			this.title = title.getText();
		}
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setDescription(LanguageString description) {
		if(description != null){
			this.description = description.getText();
		}
	}
	
	public Set<MediaRepresentationSTO> getRepresentations() {
		return representations;
	}
	public void setRepresentations(Set<MediaRepresentationSTO> representations) {
		this.representations = representations;
	}
	
	public void addRepresenation(MediaRepresentationSTO representation){
		representations.add(representation);
	}
	
	public IdentifiedString getArtist() {
		return artist;
	}
	public void setArtist(IdentifiedString artist) {
		this.artist = artist;
	}
	public Set<RightsSTO> getRights() {
		return rights;
	}
	public void setRights(Set<RightsSTO> rights) {
		this.rights = rights;
	}
	
}
