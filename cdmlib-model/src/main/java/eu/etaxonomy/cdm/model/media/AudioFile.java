/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import java.net.URI;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:11
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AudioFile", propOrder = {
    "duration"
})
@XmlRootElement(name = "AudioFile")
@Entity
@Audited
public class AudioFile extends MediaRepresentationPart {
	private static final long serialVersionUID = 2327736023969971196L;
	private static final Logger logger = Logger.getLogger(AudioFile.class);
	
	//length of recording in seconds
	@XmlElement(name = "Duration")
	private int duration;

	public static AudioFile NewInstance(URI uri, Integer size){
		logger.debug("NewInstance");
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
	protected AudioFile(URI uri, Integer size) {
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
