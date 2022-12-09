/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.compare.term.TermLanguageComparator;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.model.common.ExternallyManaged;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * A single enumeration must only contain DefinedTerm instances of one kind
 * (this means a subclass of DefinedTerm).
 * @author m.doering
 * @since 08-Nov-2007 13:06:23
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TermVocabulary", propOrder = {
    "termSourceUri",
    "terms",
    "externallyManaged"
})
@XmlRootElement(name = "TermVocabulary")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.term.TermVocabulary")
@Audited
public class TermVocabulary<T extends DefinedTermBase>
        extends TermCollection<T,TermNode> {

    private static final long serialVersionUID = 1925052321596648672L;
	private static final Logger logger = LogManager.getLogger();

	//The vocabulary source (e.g. ontology) defining the terms to be loaded when a database
	//is created for the first time.
	// Software can go and grap these terms incl. labels and description.
	// UUID needed? Further vocs can be setup through our own ontology.
	@XmlElement(name = "TermSourceURI")
	@Field(analyze = Analyze.NO)
    @FieldBridge(impl = UriBridge.class)
	@Type(type="uriUserType")
	private URI termSourceUri;

	@XmlElementWrapper(name = "Terms")
	@XmlElement(name = "Term")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="vocabulary", fetch=FetchType.LAZY, targetEntity = DefinedTermBase.class)
	@Type(type="DefinedTermBase")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	@IndexedEmbedded(depth = 2)
	protected Set<T> terms = newTermSet();

    private ExternallyManaged externallyManaged;

// ********************************* FACTORY METHODS *****************************************/

	public static TermVocabulary NewInstance(TermType type){
		return new TermVocabulary(type);
	}

	public static <T extends DefinedTermBase<T>> TermVocabulary<T> NewInstance(TermType type, Class<T> clazz){
		return new TermVocabulary<T>(type);
	}

	/**
	 * @deprecated use {@link #NewInstance(TermType, Class, String, String, String, URI)} instead
	 */
	@Deprecated
	public static TermVocabulary NewInstance(TermType type, String description, String label, String abbrev, URI termSourceUri){
		return new TermVocabulary(type, description, label, abbrev, termSourceUri, null);
	}

    public static <T extends DefinedTermBase<T>> TermVocabulary<T> NewInstance(TermType type, Class<T> clazz,
            String description, String label, String abbrev, URI termSourceUri){
        return new TermVocabulary<T>(type, description, label, abbrev, termSourceUri, null);
    }

    public static <T extends DefinedTermBase<T>> TermVocabulary<T> NewInstance(TermType type, Class<T> clazz,
            String description, String label, String abbrev, URI termSourceUri, Language language){
        return new TermVocabulary<T>(type, description, label, abbrev, termSourceUri, language);
    }

// ************************* CONSTRUCTOR *************************************************

    //for hibernate use only, *packet* private required by bytebuddy
	@Deprecated
	TermVocabulary() {
		super(TermType.Unknown);
	}

	protected TermVocabulary(TermType type) {
		super(type);
	}

	protected TermVocabulary(TermType type, String term, String label, String labelAbbrev, URI termSourceUri, Language language) {
		super(type, term, label, labelAbbrev, language);
		setTermSourceUri(termSourceUri);
	}

	protected Set<T> newTermSet(){
	    return new HashSet<>();
	}

// ******************* METHODS *************************************************/

	public T findTermByUuid(UUID uuid){
		for(T t : terms) {
			if(t.getUuid().equals(uuid)) {
				return t;
			}
		}
		return null;
	}


	public Set<T> getTerms() {
		return terms;
	}

	public void addTerm(T term) {
	    checkTermType(term);
		term.setVocabulary(this);
		this.terms.add(term);
	}
	public void removeTerm(T term) {
		this.terms.remove(term);
		term.setVocabulary(null);
	}

	public URI getTermSourceUri() {
		return termSourceUri;
	}
	public void setTermSourceUri(URI vocabularyUri) {
		this.termSourceUri = vocabularyUri;
	}

	@Deprecated //deprecated for now as only needed for property path handling; but may become generally public in future
    public Set<TermNode> getTermRelations() {
        return super.termRelations();
    }

    /**
     * Returns the first term found having the defined idInVocabulary.
     * If number of terms with given idInVoc > 1 the result is not deterministic.
     * @param idInVoc
     * @return the term with the given idInVoc
     */
    public T getTermByIdInvocabulary(String idInVoc) {
        for (T term : getTerms() ){
            if (CdmUtils.nullSafeEqual(idInVoc, term.getIdInVocabulary())){
                return term;
            }
        }
        return null;
    }

	public int size(){
		return terms.size();
	}

	/**
	 * Returns all terms of this vocabulary sorted by their representation defined by the given language.
	 * If such an representation does not exist, the representation of the default language is testing instead for ordering.
	 */
	public SortedSet<T> getTermsOrderedByLabels(Language language){
		TermLanguageComparator<T> comp = new TermLanguageComparator<>(Language.DEFAULT(), language);

		SortedSet<T> result = new TreeSet<>(comp);
		result.addAll(getTerms());
		return result;
	}

	public TermVocabulary<T> readCsvLine(List<String> csvLine) {
		return readCsvLine(csvLine, Language.CSV_LANGUAGE());
	}

	public TermVocabulary<T> readCsvLine(List<String> csvLine, Language lang) {
		this.setUuid(UUID.fromString(csvLine.get(0)));
        String uriStr = CdmUtils.Ne(csvLine.get(1));
        this.setUri(uriStr == null? null: URI.create(uriStr));
		String label = csvLine.get(2).trim();
		String description = csvLine.get(3);

		//see  https://dev.e-taxonomy.eu/redmine/issues/3550
		this.addRepresentation(Representation.NewInstance(description, label, null, lang) );

		TermType termType = TermType.getByKey(csvLine.get(4));
		if (termType == null){
			throw new IllegalArgumentException("TermType can not be mapped: " + csvLine.get(4));
		}
		this.setTermType(termType);

		return this;
	}

	/**
     * Throws {@link IllegalArgumentException} if the given
     * term has not the same term type as this term or if term type is null.
     * @param term
     */
    private void checkTermType(IHasTermType term) {
        IHasTermType.checkTermTypes(term, this);
    }

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> TermVocabulary. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> TermVocabulary.
	 * The terms of the original vocabulary are cloned
	 *
	 * @see eu.etaxonomy.cdm.model.term.TermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TermVocabulary<T> clone() {
		TermVocabulary<T> result;
		try {
			result = (TermVocabulary<T>) super.clone();

		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
		result.terms = new HashSet<T>();
		for (T term: this.terms){
			result.addTerm((T)term.clone());
		}

		return result;
	}
}
