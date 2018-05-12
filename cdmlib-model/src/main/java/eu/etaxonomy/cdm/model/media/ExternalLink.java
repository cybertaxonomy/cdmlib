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
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.VersionableEntity;


/**
 * Class that represents a link to any web resource.
 * It represents a URL and a semantic description of this URL.
 *
 * TODO: may become annotatable in future
 *
 * @author a.mueller
 * @date 09.06.2017
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExternalLink", propOrder = {
    "uri",
    "size",
    "description"
})
@XmlRootElement(name = "ExternalLink")
@Entity
@Audited
public class ExternalLink extends VersionableEntity implements Cloneable{

    private static final long serialVersionUID = -5008959652949778843L;

    private static final Logger logger = Logger.getLogger(ExternalLink.class);

    /**
     * The {@link ExternalLinkType type} of this link.
     */
    @XmlAttribute(name ="ExternalLinkType")
    @Column(name="linkType", length=10)
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.media.ExternalLinkType")}
    )
    @Audited
    private ExternalLinkType linkType = ExternalLinkType.Unknown;

    // the URI to link to
    @XmlElement(name = "URI")
    @Type(type="uriUserType")
    private URI uri;

    @XmlElement(name = "Description")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="description_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE })
//    @JoinTable(name = "ExternalLink_LanguageString")
    private Map<Language,LanguageString> description = new HashMap<>();

    // the exptected size, mostly relevant for Files, can be null
    @XmlElement(name = "Size")
    private Integer size;

 // *********************************** FACTORY ******************************/

    public static ExternalLink NewInstance(ExternalLinkType type, URI uri){
        return NewInstance(type, uri, null, null);
    }

	public static ExternalLink NewInstance(ExternalLinkType type, URI uri, Map<Language,LanguageString> description){
		return NewInstance(type, uri, description, null);
	}

	public static ExternalLink NewInstance(ExternalLinkType type, URI uri, Map<Language,LanguageString> description, Integer size){
        return new ExternalLink(type, uri, description, size);
    }

    public static ExternalLink NewWebSiteInstance(URI uri){
        return NewInstance(ExternalLinkType.WebSite, uri, null, null);
    }

    public static ExternalLink NewWebSiteInstance(URI uri, Map<Language,LanguageString> description, Integer size){
        return NewInstance(ExternalLinkType.WebSite, uri, description, size);
    }


// *********************************** CONSTRUCTOR ******************************/

	protected ExternalLink(){
		super();
	}

	protected ExternalLink(ExternalLinkType type, URI uri, Map<Language,LanguageString> description, Integer size){
		super();
		if (type == null){
		    throw new NullPointerException("ExternalLinkType must not be null");
		}
		this.linkType = type;
		this.uri = uri;
		this.description = description;
		this.size = size;
	}

// ******************************* GETTER / SETTER ********************************/

	   public ExternalLinkType getLinkType() {
	        return this.linkType;
	    }
	    public void setLinkType(ExternalLinkType linkType) {
	        this.linkType = linkType;
	    }

	/**
	 * The URI of the external link
	 */
	public URI getUri() {
        return this.uri;
    }
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * The download size of the external link. Mostly relevant for files.
     */
    public Integer getSize() {
        return this.size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    /**
     * Returns the {@link MultilanguageText multilanguage text} used to describe
     * <i>this</i> individuals association. The different {@link LanguageString language strings}
     * contained in the multilanguage text should all have the same meaning.
     */
    public Map<Language,LanguageString> getDescription(){
        return this.description;
    }


    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the {@link MultilanguageText multilanguage text} used to describe
     * <i>this</i> individuals association.
     *
     * @param description   the language string describing the individuals association
     *                      in a particular language
     * @see                 #getDescription()
     * @see                 #putDescription(Language, String)
     *
     */
    public void putDescription(LanguageString description){
        this.description.put(description.getLanguage(),description);
    }
    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to describe <i>this</i> individuals association.
     *
     * @param text      the string describing the individuals association
     *                  in a particular language
     * @param language  the language in which the text string is formulated
     * @see             #getDescription()
     * @see             #putDescription(LanguageString)
     */
    public void putDescription(Language language, String text){
        this.description.put(language, LanguageString.NewInstance(text, language));
    }

    /**
     * Removes from the {@link MultilanguageText multilanguage text} used to describe
     * <i>this</i> individuals association the one {@link LanguageString language string}
     * with the given {@link Language language}.
     *
     * @param  language the language in which the language string to be removed
     *                  has been formulated
     * @see             #getDescription()
     */
    public void removeDescription(Language language){
        this.description.remove(language);
    }

  //*********************************** CLONE *****************************************/

    /**
     * Clones <i>this</i> external link. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> external link by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        try {
            ExternalLink result = (ExternalLink)super.clone();

            //description
            result.description = cloneLanguageString(getDescription());

            return result;
            //TODO do we need to clone URI?
            //no changes to: URI, size
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }

}
