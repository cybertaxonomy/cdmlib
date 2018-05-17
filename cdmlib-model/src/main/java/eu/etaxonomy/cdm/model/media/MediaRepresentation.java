/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.media;


import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * A media representation is basically anything having a <a
 * href="http://iana.org/assignments/media-types/">MIME Media Type</a>. A media
 * representation consists of one or more parts. Each of them having the same
 * MIME Type, file suffix (if existing) and quality (more or less).
 * E.g. a list of jpg files that represent a scanned article of multiple pages.
 *
 * @author m.doering
 * @version 1.0
 * @since 08-Nov-2007 13:06:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaRepresentation", propOrder = {
	"mimeType",
    "suffix",
    "media",
    "mediaRepresentationParts"
})
@Entity
@Audited
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class MediaRepresentation extends VersionableEntity implements Cloneable{
	private static final long serialVersionUID = -1520078266008619806L;
	private static final Logger logger = Logger.getLogger(MediaRepresentation.class);

	//http://www.iana.org/assignments/media-types
	@XmlElement(name = "MimeType")
	private String mimeType;

	//the file suffix (e.g. jpg, tif, mov)
	@XmlElement(name = "Suffix")
	private String suffix;

	@XmlElement(name = "Media")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Media media;

	@XmlElementWrapper(name = "MediaRepresentationParts")
    @XmlElements({
        @XmlElement(name = "AudioFile", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = AudioFile.class),
        @XmlElement(name = "ImageFile", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = ImageFile.class),
        @XmlElement(name = "MovieFile", namespace = "http://etaxonomy.eu/cdm/model/media/1.0", type = MovieFile.class)
    })
    @OneToMany (fetch= FetchType.LAZY, orphanRemoval=true)
	@OrderColumn(name="sortIndex")
	@JoinColumn (name = "representation_id",  nullable=false)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.REFRESH})
	private List<MediaRepresentationPart> mediaRepresentationParts = new ArrayList<>();



	/**
	 * Factory method
	 * @return
	 */
	public static MediaRepresentation NewInstance(){
		logger.debug("NewInstance");
		return new MediaRepresentation();
	}

	/**
	 * Factory method which sets the mime type and the suffix
	 * @return
	 */
	public static MediaRepresentation NewInstance(String mimeType, String suffix){
		MediaRepresentation result  = new MediaRepresentation();
		result.setMimeType(mimeType);
		result.setSuffix(suffix);
		return result;
	}

	/**
	 * Factory method which creates a new media representation and adds a media representation part
	 * for the {@link URI uri} and the given size.
	 * Returns <code>null</code> if uri is empty
	 * @return
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static<T extends MediaRepresentationPart> MediaRepresentation NewInstance(String mimeType, String suffix, URI uri, Integer size, Class<T> clazz) {
		if (uri == null || CdmUtils.isEmpty(uri.toString())){
			return null;
		}
		MediaRepresentationPart part;
		if (clazz != null){
			try {
				Constructor<T> constr = clazz.getDeclaredConstructor();
				constr.setAccessible(true);
				part = constr.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			part.setUri(uri);
			part.setSize(size);
		}else{
			part = MediaRepresentationPart.NewInstance(uri, size);
		}
		MediaRepresentation result  = new MediaRepresentation();
		result.setMimeType(mimeType);
		result.setSuffix(suffix);
		result.addRepresentationPart(part);
		return result;
	}


	protected MediaRepresentation(){
		super();
	}

/***************  getter /setter *************************************/


	public String getMimeType(){
		return this.mimeType;
	}

	/**
	 *
	 * @param mimeType    mimeType
	 */
	public void setMimeType(String mimeType){
		this.mimeType = mimeType;
	}


	public String getSuffix(){
		return this.suffix;
	}

	/**
	 *
	 * @param mimeType    mimeType
	 */
	public void setSuffix(String suffix){
		this.suffix = suffix;
	}

	public Media getMedia() {
		return media;
	}

	/**
	 * @deprecated for internal (bidirectional) use only
	 * @param media
	 */
	@Deprecated
	protected void setMedia(Media media) {
		this.media = media;
	}


	public List<MediaRepresentationPart> getParts(){
		return this.mediaRepresentationParts;
	}

	@SuppressWarnings("deprecation")
	public void addRepresentationPart(MediaRepresentationPart mediaRepresentationPart){
		if (mediaRepresentationPart != null){
			this.getParts().add(mediaRepresentationPart);
			mediaRepresentationPart.setMediaRepresentation(this);
		}
	}
	@SuppressWarnings("deprecation")
	public void removeRepresentationPart(MediaRepresentationPart representationPart){
		this.getParts().remove(representationPart);
		if (representationPart != null){
			representationPart.setMediaRepresentation(null);
		}
	}

//************************* CLONE **************************/
		/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		MediaRepresentation result = (MediaRepresentation)super.clone();

		//media representations
		result.mediaRepresentationParts = new ArrayList<MediaRepresentationPart>();
		for (MediaRepresentationPart mediaRepresentationPart: this.mediaRepresentationParts){
			result.mediaRepresentationParts.add((MediaRepresentationPart)mediaRepresentationPart.clone());
		}
		//media
		//this.getMedia().addRepresentation(result);
		this.setMedia(null);

		//no changes to: mimeType, suffix
		return result;
	}



}
