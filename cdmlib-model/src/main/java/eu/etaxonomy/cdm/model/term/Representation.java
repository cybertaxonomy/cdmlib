/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Store;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageStringBase;

/**
 * workaround for enumerations
 * @author m.doering
 * @since 08-Nov-2007 13:06:49
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Representation", propOrder = {
    "label",
    "plural",
    "abbreviatedLabel"
})
@XmlRootElement(name = "Representation")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.Representation")
@Audited
public class Representation extends LanguageStringBase {

    private static final long serialVersionUID = -4202420199587324532L;
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name = "Label")
    @Field(store=Store.YES)
    private String label;

    @XmlElement(name = "AbbreviatedLabel")
    @Field(store=Store.YES)
    private String abbreviatedLabel;

    //#8142
    @XmlElement(name = "Plural")
    @Field(store=Store.YES)
    private String plural;

//********************************************* FACTORY ************************/

    public static Representation NewInstance(String description, String label, String abbreviatedLabel, Language lang){
        return new Representation(description, label, abbreviatedLabel, lang);
    }

    //for hibernate use only, *packet* private required by bytebuddy
    @Deprecated
    Representation() {}

    /**
     * text represents an explanation/declaration ('The name is illegitimate according to ICBN'); label a string identifier ('illegitimate name');
     * abbreviatedLabel a shortened string for the label ('nom. illeg.')
     */
    public Representation(String description, String label, String abbreviatedLabel, Language language) {
        super(description, language);
        this.label = label;
        this.abbreviatedLabel = abbreviatedLabel;
    }


    public String getLabel(){
        return this.label;
    }
    public void setLabel(String label){
        this.label = label;
    }

    public String getAbbreviatedLabel(){
        return this.abbreviatedLabel;
    }
    public void setAbbreviatedLabel(String abbreviatedLabel){
        this.abbreviatedLabel = abbreviatedLabel;
    }

    public String getPlural() {
        return plural;
    }
    public void setPlural(String plural) {
        this.plural = plural;
    }

    /**
     * Returns the description of this representation
     * see {@link #getText()}
     * @return
     */
    @Transient
    public String getDescription(){
        return getText();
    }

    protected void setDescription(String text) {
        super.setText(text);
    }


    /*
     * Overrides super.getText() only to document that here the Text attribute
     * should be used for a larger description of the label.
     */
    /**
     * Returns the description of this representation.
     * @see #getDescription()
     */
    @Transient
    @Override
    public String getText(){
        return super.getText();
    }


    @Override
    public String toString(){
        // we dont need the language returned too, do we?
        return getLabel();
//		if(getLanguage()==null || getLanguage().getLabel()==null){
//			return getLabel();
//		}else{
//			return getLabel()+"("+ getLanguage().getLabel()+")";
//		}
    }

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> Representation. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> Representation by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.common.LanguageStringBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Representation clone() {
        try{
            Representation result = (Representation) super.clone();
            //no changes to abbreviatedLabel and label
            return result;
        }catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }

    }
}
