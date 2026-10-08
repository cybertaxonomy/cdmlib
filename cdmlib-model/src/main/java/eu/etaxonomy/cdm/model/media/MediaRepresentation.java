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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * A media representation is basically anything having a
 * <a href="http://iana.org/assignments/media-types/">MIME Media Type</a>
 * that can be referenced by an URI.
 * <p>
 * Formerly split into {@code MediaRepresentation} and {@code MediaRepresentationPart};
 * the part attributes ({@link #uri}, {@link #size}, metadata) are now held here directly.
 * Typed variants are {@link ImageFile}, {@link AudioFile} and {@link MovieFile}.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:34
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MediaRepresentation", propOrder = {
        "mimeType",
        "suffix",
        "media",
        "uri",
        "size",
        "mediaMetaData"
})
@Entity
@Audited
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class MediaRepresentation extends VersionableEntity {

    private static final long serialVersionUID = -1520078266008619806L;
    private static final Logger logger = LogManager.getLogger();

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
    private Media media;

    // where the media file is stored
    @XmlElement(name = "URI")
    @Type(type="uriUserType")
    private URI uri;

    // in bytes
    @XmlElement(name = "Size")
    private Integer size;

    @XmlElementWrapper(name = "MediaMetaDatas")
    @XmlElement(name = "MediaMetaData")
    @OneToMany (mappedBy="mediaRepresentation", fetch= FetchType.LAZY, orphanRemoval=true)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.REFRESH})
    private Set<MediaMetaData> mediaMetaData = new HashSet<>();

//********************* FACTORY ***********************************************/

    public static MediaRepresentation NewInstance(){
        return new MediaRepresentation();
    }

    /**
     * Factory method which creates a new media representation for the given
     * {@link URI uri} and size. If {@code clazz} is a subclass of
     * {@link MediaRepresentation}, an instance of that type is created.
     * Returns <code>null</code> if uri is empty.
     */
    public static <T extends MediaRepresentation> MediaRepresentation NewInstance(
            URI uri, String mimeType, String suffix, Integer size, Class<T> clazz) {
        if (uri == null || isBlank(uri.toString())){
            return null;
        }
        MediaRepresentation result;
        if (clazz != null && clazz != MediaRepresentation.class){
            try {
                Constructor<T> constr = clazz.getDeclaredConstructor();
                constr.setAccessible(true);
                result = constr.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else{
            result = new MediaRepresentation();
        }
        result.setMimeType(mimeType);
        result.setSuffix(suffix);
        result.setUri(uri);
        result.setSize(size);
        return result;
    }

    public static MediaRepresentation NewInstance(URI uri, String mimeType,
            String suffix, Integer size) {
        MediaRepresentation result = new MediaRepresentation(uri, mimeType, suffix, size);
        return result;
    }

// ************************ CONSTRUCTOR *********************************/

    protected MediaRepresentation(){}

    protected MediaRepresentation(URI uri, String mimeType, String suffix, Integer size) {
        this();
        this.setUri(uri);
        this.setMimeType(mimeType);
        this.setSuffix(suffix);
        this.setSize(size);
    }


//***************  Getter /Setter *************************************/


    public String getMimeType(){
        return this.mimeType;
    }
    public void setMimeType(String mimeType){
        this.mimeType = mimeType;
    }


    public String getSuffix(){
        return this.suffix;
    }
    public void setSuffix(String suffix){
        this.suffix = suffix;
    }

    public Media getMedia() {
        return media;
    }

    /**
     * @deprecated for internal (bidirectional) use only
     */
    @Deprecated
    protected void setMedia(Media media) {
        this.media = media;
    }

    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Integer getSize() {
        return this.size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }

    public void addMediaMetaData(MediaMetaData metaData){
        this.mediaMetaData.add(metaData);
        if(metaData.getMediaRepresentation() != this){
            metaData.setMediaRepresentation(this);
        }
    }

    public Set<MediaMetaData> getMediaMetaData() {
        return mediaMetaData;
    }

    public void removeMediaMetaData(MediaMetaData metaData){
        this.mediaMetaData.remove(metaData);
        if(metaData.getMediaRepresentation() == this){
            metaData.setMediaRepresentation(null);
        }
    }

//************************* CLONE **************************/

    @Override
    public MediaRepresentation clone() throws CloneNotSupportedException{
        MediaRepresentation result = (MediaRepresentation)super.clone();

        result.mediaMetaData = new HashSet<>();
        for (MediaMetaData metaData : this.getMediaMetaData()) {
            result.addMediaMetaData(metaData.clone());
        }

        //media
        this.setMedia(null);

        //no changes to: mimeType, suffix, size, uri
        return result;
    }
}
