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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:00
 */
public class AudioFile extends MediaInstance {
	static Logger logger = Logger.getLogger(AudioFile.class);

	//length of recording in seconds
	@Description("length of recording in seconds")
	private int duration;

	public int getDuration(){
		return duration;
	}

	/**
	 * 
	 * @param duration
	 */
	public void setDuration(int duration){
		;
	}

}