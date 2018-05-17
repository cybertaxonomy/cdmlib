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
 * @since 08-Nov-2007 13:06:35
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MovieFile", propOrder = {
    "duration"
})
@XmlRootElement(name = "MovieFile")
@Entity
@Audited
public class MovieFile extends MediaRepresentationPart {
	private static final long serialVersionUID = 8650308822737671731L;
	private static final Logger logger = Logger.getLogger(MovieFile.class);
	
	//Length of movie in seconds
	@XmlElement(name = "Duration")
	private int duration;

	public static MovieFile NewInstance(URI uri, Integer size){
		logger.debug("NewInstance");
		return new MovieFile(uri, size);
	}
	
	/**
	 * Factory method
	 * @return
	 */
	public static MovieFile NewInstance(){
		return new MovieFile();
	}
	
	/**
	 * Constructor
	 */
	protected MovieFile() {
		super();
	}
	
	/**
	 * Constructor
	 */
	protected MovieFile(URI uri, Integer size) {
		super(uri, size);
	}
	
	
	/**
	 * The Length of the movie in seconds
	 * 
	 * @return
	 */
	public int getDuration(){
		return this.duration;
	}

	/**
	 * Sets the Length of the movie in seconds
	 * @param duration    duration
	 */
	public void setDuration(int duration){
		this.duration = duration;
	}

}
