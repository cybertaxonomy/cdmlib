/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
import java.util.Set;

/**
 * @author a.kohlbecker
 * @version 1.0
 * @created 11.12.2007 12:13:42
 *
 */
public class MediaSTO extends BaseSTO implements IBaseSTO{

	/**
	 * The title of the Media instance
	 */
	private String title;
	
	/**
	 * A single medium such as a picture can have multiple representations in files. 
	 * Common are multiple resolutions or file
	 * formats for images for example
	 */
	
	private Set<MediaRepresentationSTO> representations;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Set<MediaRepresentationSTO> getRepresentations() {
		return representations;
	}
	public void setRepresentations(Set<MediaRepresentationSTO> representations) {
		this.representations = representations;
	}
	
}
