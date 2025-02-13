/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.strategy.cache.occurrence.MediaSpecimenDefaultCacheStrategy;

/**
 * Instances of MediaSpecimen represent a specimen which IS a media (photo, drawing, ...).
 * The individuum shown by the media may or may not be a collection specimen on it's own.
 * Often it is not, which may be the reason why a picture (or other media) is taken instead.
 *
 * This is often the case for older (type) specimen which have only be drawn or painted.
 * Also it may be the cases for small biota which can not be individualized
 * or preserved accordingly and may therefore be photographed instead.
 *
 * The media may or may not be part of a collection and have it's own catalog/collection
 * number.
 * It also may or may not be available online. If it is online the according URIs should
 * be given via the {@link MediaRepresentation}s.
 * Finally the media may be published in a print publication or not. If published in a print
 * publication this publication should be referenced by the media's source(s).
 *
 * @author a.mueller
 * @since 14-Jul-2013 13:06:22
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaSpecimen", propOrder = {
    "mediaSpecimen"
})
@XmlRootElement(name = "MediaSpecimen")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase")
@Audited
public class MediaSpecimen extends DerivedUnit {

	private static final long serialVersionUID = -5717424451590705378L;
	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

// ************** ATTRIBUTES ****************************/

	@XmlElement(name = "sequence")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Media mediaSpecimen;

// ****************** FACTORY METHOD *****************/

    /**
     * Factory method.
     * @param type must be {@link SpecimenOrObservationType#Media} or a subtype of it.
     */
    public static MediaSpecimen NewInstance(SpecimenOrObservationType type){
        return new MediaSpecimen(type);
    }

// ******************* CONSTRUCTOR *************************/

    private MediaSpecimen() {
		this(SpecimenOrObservationType.Media);
	}

	private MediaSpecimen(SpecimenOrObservationType type) {
		super(type);
	}

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = MediaSpecimenDefaultCacheStrategy.NewInstance();
    }

//************ GETTER / SETTER  **********************************/

	/**
	 * The media which represents this specimen. It is important to realize
	 * that a media specimen is not a media which only shows the specimen
	 * but it is the specimen itself. Therefore this method should only be used
	 * for specimen which ARE media.<BR>
	 * This is often the case for older (type) specimen which have only be drawn or painted.
	 * Also it may be the cases for small biota which can not be individualized
	 * or preserved accordingly and may therefore be photographed instead.
	 */
	public Media getMediaSpecimen() {
		return mediaSpecimen;
	}

	/**
	 * @see #getMediaSpecimen()
	 */
	public void setMediaSpecimen(Media mediaSpecimen) {
		this.mediaSpecimen = mediaSpecimen;
	}

// ************* Convenience Getter / Setter ************/



//*********** CLONE **********************************/


	/**
	 * Clones <i>this</i> dna sample. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> dna sample
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link Specimen Specimen}.
	 * @throws CloneNotSupportedException
	 *
	 * @see Specimen#clone()
	 * @see DerivedUnit#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MediaSpecimen clone() {
		MediaSpecimen result = (MediaSpecimen)super.clone();

		//no changes to: mediaSpecimen
		return result;
	}
}
