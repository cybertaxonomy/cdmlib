/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.description.FeatureNode;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:11
 */
@Entity
public class AudioFile extends MediaInstance {
	private static final Logger logger = Logger.getLogger(AudioFile.class);
	
	//length of recording in seconds
	private int duration;
	
	/**
	 * Factory method
	 * @return
	 */
	public static AudioFile NewInstance(){
		return new AudioFile();
	}
	
	/**
	 * Constructor
	 */
	protected AudioFile() {
		super();
	}
	
	public int getDuration(){
		return this.duration;
	}

	/**
	 * 
	 * @param duration    duration
	 */
	public void setDuration(int duration){
		this.duration = duration;
	}

}