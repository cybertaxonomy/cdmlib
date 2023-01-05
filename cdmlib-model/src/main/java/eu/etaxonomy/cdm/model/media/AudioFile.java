/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.common.URI;

/**
 * @author m.doering
 * @since 08-Nov-2007 13:06:11
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
    private static final Logger logger = LogManager.getLogger();

	//length of recording in seconds
	@XmlElement(name = "Duration")
	private int duration;


// *************** FACTORY METHOD *********************************/

	public static AudioFile NewInstance(URI uri, Integer size){
		logger.debug("NewInstance");
		return new AudioFile(uri, size);
	}
	public static AudioFile NewInstance(){
		return new AudioFile();
	}

// ********************** CONSTRUCTOR ***************************/
	protected AudioFile() {
		super();
	}

	protected AudioFile(URI uri, Integer size) {
		super(uri, size);
	}

// ******************** GETTER / SETTER *************************/
	public int getDuration(){
		return this.duration;
	}
	public void setDuration(int duration){
		this.duration = duration;
	}

}
