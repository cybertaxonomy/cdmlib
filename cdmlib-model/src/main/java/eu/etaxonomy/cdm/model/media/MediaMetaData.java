/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.media;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * This class is for storing metadata about media files
 * (e.g. Exif, IPCT, PhotoshopMetadata, etc) in a key-value format.<BR>
 * In principle auditing would be not necessary as the metadata is considered
 * to be cached only from the original file and therefore can be reconstructed
 * and updated when ever the file is available. For technical reasons auditing
 * of <code>MediaMetaData</code> entities had to be activated nevertheless.<BR>
 * Data should only contain data which can not be stored otherwise
 * (height, width, size, authorship, ...).
 *
 * see also #9009
 *
 * @author a.mueller
 * @since 10.06.2020
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaMetaData", propOrder = {
    "mediaRepresentation",
    "key",
    "value"
 })
@Entity
@Audited  //necessary because otherwise mapping from MediaRepresentationPart to MediaMetaData is not possible
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class MediaMetaData extends CdmBase implements Cloneable {

    private static final long serialVersionUID = -2523716526037575324L;

    @XmlElement(name = "MediaRepresentation")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mediaRepresentation_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
    private MediaRepresentationPart mediaRepresentation;

    @Column(name="pairkey") //to avoid conflicts with SQL keywords
    private String key;

    @Column(name="pairvalue")  //to avoid conflicts with SQL keywords
    private String value;

// ************************** FACTORY ***********************/

    public static MediaMetaData NewInstance(MediaRepresentationPart mediaRepresentation,
            String key, String value) {
        return new MediaMetaData(mediaRepresentation, key, value);
    }

// ************************* CONSTRUCTOR *******************/

    private MediaMetaData(){}

    private MediaMetaData(MediaRepresentationPart mediaRepresentation, String key, String value){
        setMediaRepresentation(mediaRepresentation);
        this.key = key;
        this.value = value;
    }

//*********************** GETTER / SETTER **********************/

    public MediaRepresentationPart getMediaRepresentation() {
        return mediaRepresentation;
    }

    protected void setMediaRepresentation(MediaRepresentationPart mediaRepresentation) {
        this.mediaRepresentation = mediaRepresentation;
        if (mediaRepresentation != null){
            mediaRepresentation.addMediaMetaData(this);
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

// ************************* clone ************************/

    @Override
    public MediaMetaData clone() throws CloneNotSupportedException {
        MediaMetaData result = (MediaMetaData)super.clone();

        //need to change mediaRepresentation?

        //no change: key, value
        return result;
    }

// ************************** toString ************************/

    @Override
    public String toString() {
        return "MediaMetaData [" + key + "=" + value + "]";
    }
}
