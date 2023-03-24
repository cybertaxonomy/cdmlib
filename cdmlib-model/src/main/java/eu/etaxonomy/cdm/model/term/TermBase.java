/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.term.TermDefaultCacheStrategy;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermBase", propOrder = {
    "uri",
    "termType",
    "representations"
})
@XmlSeeAlso({
    DefinedTermBase.class,
    TermVocabulary.class
})
@MappedSuperclass
@Audited
public abstract class TermBase
            extends IdentifiableEntity<IIdentifiableEntityCacheStrategy<TermBase>>
            implements IHasTermType {

    private static final long serialVersionUID = 1471561531632115822L;
    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name = "URI")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = UriBridge.class)
    @Type(type="uriUserType")
    private URI uri;

	/**
	 * The {@link TermType type} of this term. Needs to be the same type in a {@link DefinedTermBase defined term}
	 * and in it's {@link TermVocabulary vocabulary}.
	 */
	@XmlAttribute(name ="TermType")
	@Column(name="termType")
	@NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.term.TermType")}
    )
	@Audited
	private TermType termType;

    @XmlElementWrapper(name = "Representations")
    @XmlElement(name = "Representation")
    @OneToMany(fetch=FetchType.EAGER, orphanRemoval=true)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
    // @IndexedEmbedded no need for embedding since we are using the DefinedTermBaseClassBridge
    private Set<Representation> representations = new HashSet<>();

//******************* CONSTRUCTOR *************************************/

    //for hibernate (and JAXB?) use only, *packet* private required by bytebuddy
    TermBase(){}

    protected TermBase(TermType type){
        if (type == null){
        	throw new IllegalArgumentException("TermType must not be null");
        }else{
        	this.termType = type;
        }
    }

    protected TermBase(TermType type, String term, String label, String labelAbbrev, Language lang) {
        this(type);
        if (lang == null) {
        	lang = Language.DEFAULT();
        }
        this.addRepresentation(new Representation(term, label, labelAbbrev, lang) );
    }

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = TermDefaultCacheStrategy.NewInstance(TermBase.class);
    }

//******************** GETTER /SETTER ********************************/

	@Override
    public TermType getTermType() {
		return termType;
	}
	@Deprecated //the term type should never be changed, might be removed in future
	public void setTermType(TermType termType) {
		this.termType = termType;
	}


    public Set<Representation> getRepresentations() {
        return this.representations;
    }

    public void addRepresentation(Representation representation) {
//        this.representations.add(representation);
        this.addToSetWithChangeEvent(this.representations, representation, "representations");
    }

    public void removeRepresentation(Representation representation) {
//        this.representations.remove(representation);
        this.removeFromSetWithChangeEvent(this.representations, representation, "representations");
    }

    public Representation getRepresentation(Language lang) {
        for (Representation repr : representations){
            Language reprLanguage = repr.getLanguage();
            if (reprLanguage != null && reprLanguage.equals(lang)){
                return repr;
            }
        }
        return null;
    }

    /**
     * @see #getPreferredRepresentation(Language)
     */
    public Representation getPreferredRepresentation(Language language) {
        Representation repr = getRepresentation(language);
        if(repr == null){
            repr = getRepresentation(Language.DEFAULT());
        }
        if(repr == null){
            repr = getRepresentations().isEmpty() ? null : getRepresentations().iterator().next();
        }
        return repr;
    }

    /**
     * Returns the Representation in the preferred language. Preferred languages
     * are specified by the parameter languages, which receives a list of
     * Language instances in the order of preference. If no representation in
     * any preferred languages is found the method falls back to return the
     * Representation in Language.DEFAULT() and if necessary further falls back
     * to return the first element found if any.
     *
     * TODO think about this fall-back strategy &
     * see also {@link TextData#getPreferredLanguageString(List)}
     *
     * @param languages
     * @return
     */
    public Representation getPreferredRepresentation(List<Language> languages) {
        Representation repr = null;
        if(languages != null){
            for(Language language : languages) {
                repr = getRepresentation(language);
                if(repr != null){
                    return repr;
                }
            }
        }
        repr = getRepresentation(Language.DEFAULT());
        if(repr == null){
            Iterator<Representation> it = getRepresentations().iterator();
            if(it.hasNext()){
                repr = getRepresentations().iterator().next();
            }
        }
        return repr;
    }

    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Transient
    public String getLabel() {
        if(isNotBlank(getLabel(Language.DEFAULT()))){
            return getLabel(Language.DEFAULT());
        }else{
            for (Representation r : representations){
                if (isNotBlank(r.getLabel())){
                    return r.getLabel();
                }
            }
            if (representations.size()>0){
                return null;
            }else{
            	if (this.getTitleCache() != null) {
            		return this.getTitleCache();
            	}
                return super.getUuid().toString();
            }
        }
    }

    public String getLabel(Language lang) {
        Representation repr = this.getRepresentation(lang);
        return (repr == null) ? null : repr.getLabel();
    }

    public void setLabel(String label){
        Language lang = Language.DEFAULT();
        setLabel(label, lang);
    }

    public void setLabel(String label, Language language){
        if (language != null){
            Representation repr = getRepresentation(language);
            if (repr != null){
                repr.setLabel(label);
            }else{
                repr = Representation.NewInstance(null, label, null, language);
                this.addRepresentation(repr);
            }
            this.titleCache = null; //force titleCache refresh
        }
    }

    @Transient
    public String getDescription() {
        return this.getDescription(Language.DEFAULT());
    }

    public String getDescription(Language lang) {
        Representation repr = this.getRepresentation(lang);
        return (repr == null) ? null :repr.getDescription();
    }


    @Override
    public String toString() {
        //TODO eliminate nasty LazyInitializationException logging
        try {
            return super.toString();
        } catch (LazyInitializationException e) {
            return super.toString()+" "+this.getUuid();
        }
    }

//*********************** CLONE ********************************************************/

    /**
     * Clones <i>this</i> TermBase. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> TermBase by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public TermBase clone()throws CloneNotSupportedException {

        TermBase result = (TermBase) super.clone();

        result.representations = new HashSet<Representation>();
        for (Representation rep : this.representations){
            result.representations.add(rep.clone());
        }

        return result;
    }
}
