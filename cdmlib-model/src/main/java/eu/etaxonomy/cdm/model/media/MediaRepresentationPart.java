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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * A media representation part is a resource that can be referenced by an URI.
 * It represents a part of or the entire media. <br>
 * E.g. a jpg file or a website
 *
 * @author a.mueller
 * @since 09.06.2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaRepresentationPart", propOrder = {
		"uri",
        "size",
        "mediaRepresentation"
  })
@Entity
@Audited
public class MediaRepresentationPart extends VersionableEntity implements Cloneable{
	private static final long serialVersionUID = -1674422508643785796L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MediaRepresentationPart.class);

	// where the media file is stored
	@XmlElement(name = "URI")
	@Type(type="uriUserType")
	private URI uri;

	// in bytes
	@XmlElement(name = "Size")
	private Integer size;

	// the MediaRepresentation of this MediaRepresentationPart
	@XmlElement(name = "MediaRepresentation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "representation_id", nullable = false, updatable = false, insertable = false)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private MediaRepresentation mediaRepresentation;


// *************** FACTORY METHOD *********************************/

	public static MediaRepresentationPart NewInstance(URI uri, Integer size) {
		MediaRepresentationPart result = new MediaRepresentationPart(uri, size);
		return result;
	}


	protected MediaRepresentationPart() {
		super();
	}

	protected MediaRepresentationPart(URI uri, Integer size) {
		this();
		this.setUri(uri);
		this.setSize(size);
	}

//*************** GETTER / SETTER *************************************/

	public MediaRepresentation getMediaRepresentation() {
		return this.mediaRepresentation;
	}

	/**
	 *  @deprecated for internal (bidirectional) use only
	 */
	@Deprecated
	protected void setMediaRepresentation(MediaRepresentation mediaRepresentation) {
		this.mediaRepresentation = mediaRepresentation;
	}

	//uri
	public URI getUri() {
		return this.uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	//size
	public Integer getSize() {
		return this.size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}

//************************* CLONE **************************/

	@Override
	public Object clone() throws CloneNotSupportedException{
		MediaRepresentationPart result = (MediaRepresentationPart)super.clone();

		//media representation
		result.setMediaRepresentation(null);

		//no changes to: size, uri
		return result;
	}
}
