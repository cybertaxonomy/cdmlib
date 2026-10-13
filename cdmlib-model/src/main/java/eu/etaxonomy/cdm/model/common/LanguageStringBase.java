/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Length;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.type.SqlTypes;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;

import eu.etaxonomy.cdm.hibernate.search.StripHtmlBridge;
import eu.etaxonomy.cdm.jaxb.FormattedTextAdapter;

/**
 * @author a.mueller
 * @since 23.04.2026
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
public abstract class LanguageStringBase
        extends VersionableEntity
        implements ILanguageString {

    private static final long serialVersionUID = -1892526642162438277L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name = "Text")
    @XmlJavaTypeAdapter(FormattedTextAdapter.class)
    @FullTextField(valueBridge = @ValueBridgeRef(type=StripHtmlBridge.class))
    // Workaround for Hibernate 6.1 / Envers (HHH-16253): Envers keeps length but drops LOB on _AUD
    // (falls back to varchar(255)). LONG32 exceeds dialect max VARCHAR → expected type upgrades to CLOB.
    // Remove @JdbcTypeCode / reconsider length when on a Hibernate version that preserves CLOB on audit tables.
    @Column(length = Length.LONG32)
    @Lob
    @JdbcTypeCode(SqlTypes.CLOB)
    protected String text;

    @XmlElement(name = "Language")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.EAGER)
    @IndexedEmbedded(includeDepth=1)
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
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
    public LanguageStringBase clone() throws CloneNotSupportedException{
        LanguageStringBase result = (LanguageStringBase) super.clone();
        //no changes to text and language
        //result.setText(this.text);
        //result.setLanguage(this.language);
        return result;
    }
}
