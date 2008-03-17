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
	
	private Set<MediaInstanceSTO> instances;
	
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Set<MediaInstanceSTO> getInstances() {
		return instances;
	}
	public void setInstances(Set<MediaInstanceSTO> instances) {
		this.instances = instances;
	}
	public void addInstance(String uri, String mimeType, Integer heigth, Integer width){
		MediaInstanceSTO mi = new MediaInstanceSTO();
		mi.setHeigth(heigth);
		mi.setWidth(width);
		mi.setUri(uri);
		mi.setMimeType(mimeType);
		this.instances.add(mi);
	}
	
}
