/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.hibernate.search.StripHtmlBridge;
import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;

/**
 * @author a.mueller
 * @version 1.0
 * @created 25.04.2008
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LanguageStringBase", propOrder = {
    "text",
    "language"
})
@XmlSeeAlso({
    LanguageString.class
})
@MappedSuperclass
@Audited
public abstract class LanguageStringBase extends AnnotatableEntity{
    private static final long serialVersionUID = -1892526642162438277L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LanguageStringBase.class);

    @XmlElement(name = "Text")
    @XmlJavaTypeAdapter(FormattedTextAdapter.class)
    @Column(length=65536)
    @Field
    @FieldBridge(impl=StripHtmlBridge.class)
    @Lob
    protected String text;

    @XmlElement(name = "Language")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.EAGER)
//    @Cascade({CascadeType.MERGE})  remove cascade #5755
    @IndexedEmbedded(depth=1)
    protected Language language;

    protected LanguageStringBase() {
        super();
    }

    protected LanguageStringBase(String text, Language language) {
        super();
        this.setLanguage(language);
        this.setText(text);

    }

    public Language getLanguage(){
        return this.language;
    }
    public void setLanguage(Language language){
        this.language = language;
    }

    public String getText(){
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Transient
    public String getLanguageLabel(){
        if (language != null){
            return this.language.getRepresentation(Language.DEFAULT()).getLabel();
        }else{
            return null;
        }
    }

    public String getLanguageLabel(Language lang){
        if (language != null){
            return this.language.getRepresentation(lang).getLabel();
        }else{
            return null;
        }
    }

// ****************** CLONE ************************************/

    @Override
    public Object clone() throws CloneNotSupportedException{
        LanguageStringBase result = (LanguageStringBase) super.clone();
        //no changes to text and language
        //result.setText(this.text);
        //result.setLanguage(this.language);
        return result;
    }
}
