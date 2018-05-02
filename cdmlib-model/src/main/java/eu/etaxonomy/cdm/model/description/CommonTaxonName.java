/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class represents common or vernacular names for {@link Taxon taxa}.
 * Only {@link TaxonDescription taxon descriptions} may contain common names.
 * Common names vary not only according to the {@link Language language} but also sometimes
 * according to {@link TaxonDescription#getGeoScopes() geospatial areas}. Furthermore there might be several
 * distinct common names in one language and in the same geospatial area to
 * designate the same taxon. Therefore using a {@link MultilanguageText multilanguage text}
 * would not have been adequate.
 *
 * @author m.doering
 * @version 1.0
 * @since 08-Nov-2007 13:06:17
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommonTaxonName", propOrder = {
    "name",
    "language",
    "area"
})
@XmlRootElement(name = "CommonTaxonName")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class CommonTaxonName extends DescriptionElementBase implements Cloneable {
    private static final long serialVersionUID = 2643808051976643339L;
    private static final Logger logger = Logger.getLogger(CommonTaxonName.class);

    @XmlElement(name = "Name")
    @Field(store=Store.YES)
    private String name;

    @XmlElement(name = "Language")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded(depth=1)
    private Language language;

    @XmlElement(name = "Area")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded(depth=1) // FIXME do we need a special field bridge for this type?
    private NamedArea area;


    /**
     * Creates a common name instance with the given name string and the given
     * {@link Language language}. The corresponding {@link Feature feature} is set to
     * {@link Feature#COMMON_NAME() COMMON_NAME}.
     *
     * @param name		the name string
     * @param language	the language of the name string
     */
    public static CommonTaxonName NewInstance(String name, Language language){
        return NewInstance(name, language, null);
    }

    /**
     * Creates a common name instance with the given name string and the given
     * {@link Language language}. The corresponding {@link Feature feature} is set to
     * {@link Feature#COMMON_NAME() COMMON_NAME}.
     *
     * @param name		the name string
     * @param language	the language of the name string
     * @param area		the area where this common name is used
     */
    public static CommonTaxonName NewInstance(String name, Language language, NamedArea area){
        CommonTaxonName result = new CommonTaxonName();
        result.setName(name);
        result.setLanguage(language);
        result.setArea(area);
        return result;
    }


// *************************** CONSTRUCTOR *************************************/

    /**
     * Class constructor: creates a new empty common name instance.
     * The corresponding {@link Feature feature} is set to {@link Feature#COMMON_NAME() COMMON_NAME}.
     */
    protected CommonTaxonName(){
        super(Feature.COMMON_NAME());
    }

// *************************** METHODS *****************************************/

    /**
     * @deprecated Deprecated because {@link Feature feature} should always be {@link Feature#COMMON_NAME() COMMON_NAME}
     * for all common name instances.
    */
    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#setFeature(eu.etaxonomy.cdm.model.description.Feature)
     */
    @Override
    @Deprecated
    public void setFeature(Feature feature) {
        super.setFeature(feature);
    }

    /**
     * Returns the {@link Language language} used for <i>this</i> common name.
     */
    public Language getLanguage(){
        return this.language;
    }
    /**
     * @see	#getLanguage()
     */
    public void setLanguage(Language language){
        this.language = language;
    }

    /**
     * Returns the name string of <i>this</i> common name.
     */
    public String getName(){
        return this.name;
    }

    /**
     * @see	#getName()
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * The area where the name is used
     * @return
     */
    public NamedArea getArea() {
        return area;
    }

    /**
     * @see #getArea()
     * @param area
     */
    public void setArea(NamedArea area) {
        this.area = area;
    }


//*********************************** CLONE *****************************************/

    /**
     * Clones <i>this</i> common name. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> common name by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        try {
            CommonTaxonName result = (CommonTaxonName)super.clone();
            return result;
            //no changes to name, language, area
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }

//*********************************** toString *****************************************/

    @Override
    public String toString(){
        if (StringUtils.isNotBlank(name)){
            return name;
        }else{
            return super.toString();
        }
    }
}
