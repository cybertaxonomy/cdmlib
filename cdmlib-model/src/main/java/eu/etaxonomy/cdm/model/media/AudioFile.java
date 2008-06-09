/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import org.apache.log4j.Logger;

import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:11
 */
@Entity
public class AudioFile extends MediaRepresentationPart {
	private static final Logger logger = Logger.getLogger(AudioFile.class);
	
	//length of recording in seconds
	private int duration;


	public static AudioFile NewInstance(String uri, Integer size){
		return new AudioFile(uri, size);
	}

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
	
	/**
	 * Constructor
	 */
	protected AudioFile(String uri, Integer size) {
		super(uri, size);
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