/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import au.com.bytecode.opencsv.CSVWriter;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelationshipTermBase", propOrder = {
    "symmetric",
    "transitive",
    "inverseRepresentations"
})
@XmlSeeAlso({
	HybridRelationshipType.class,
	NameRelationshipType.class,
	SynonymRelationshipType.class,
	TaxonRelationshipType.class
})
@Entity
@Audited
public abstract class RelationshipTermBase<T extends RelationshipTermBase> extends OrderedTermBase<T> {
	private static final long serialVersionUID = 5497187985269083971L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RelationshipTermBase.class);

	@XmlElement(name = "Symmetrical")
	@Field(analyze = Analyze.NO)
	@Column(name="symmetrical") //to be compatible with PostGreSQL
	private boolean symmetric;

	@XmlElement(name = "Transitive")
	@Field(analyze = Analyze.NO)
	private boolean transitive;

	@XmlElementWrapper(name = "InverseRepresentations")
	@XmlElement(name = "Representation")
	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(
	        name="RelationshipTermBase_inverseRepresentation",
	        joinColumns = @JoinColumn(name="DefinedTermBase_id")
	)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	@IndexedEmbedded(depth = 2)
	private Set<Representation> inverseRepresentations = new HashSet<Representation>();

//******************** CONSTRUCTOR ************************/

    //for JAXB only, TODO needed?
    @Deprecated
    private RelationshipTermBase(){super();}


	protected RelationshipTermBase(TermType type) {
		super(type);
	}
	public RelationshipTermBase(TermType type, String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(type, term, label, labelAbbrev);
		setSymmetric(symmetric);
		setTransitive(transitive);
	}

//************************** METHODS ********************************

	public boolean isSymmetric() {
		return symmetric;
	}
	public void setSymmetric(boolean symmetric) {
		this.symmetric = symmetric;
	}

	public boolean isTransitive() {
		return transitive;
	}
	public void setTransitive(boolean transitive) {
		this.transitive = transitive;
	}

	public Set<Representation> getInverseRepresentations() {
		return inverseRepresentations;
	}

	public void addInverseRepresentation(Representation inverseRepresentation) {
		this.inverseRepresentations.add(inverseRepresentation);
	}
	public void removeInverseRepresentation(Representation inverseRepresentation) {
		this.inverseRepresentations.remove(inverseRepresentation);
	}
	public void addRepresentation(Representation representation, Representation inverseRepresentation) {
		this.addRepresentation(representation);
		this.addInverseRepresentation(inverseRepresentation);
	}

	public Representation getInverseRepresentation(Language lang) {
		Representation result = null;
		if (this.isSymmetric()){
			for (Representation repr : this.getRepresentations()){
				if (lang.equals(repr.getLanguage())){
					result = repr;
				}
			}
		}else{
			for (Representation repr : this.getInverseRepresentations()){
				if (lang.equals(repr.getLanguage())){
					result = repr;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the InverseRepresentation in the preferred language. Preferred languages
	 * are specified by the parameter languages, which receives a list of
	 * Language instances in the order of preference. If no representation in
	 * any preferred languages is found the method falls back to return the
	 * Representation in Language.DEFAULT() and if necessary further falls back
	 * to return the first element found if any.
	 *
	 * TODO think about this fall-back strategy &
	 * see also {@link TextData#getPreferredLanguageString(List)}
	 * see also {@link TermBase#getPreferredRepresentation(List)}
	 *
	 * @param languages
	 * @return
	 */
	public Representation getPreferredInverseRepresentation(List<Language> languages) {
		Representation repr = null;
		if(languages != null){
			for(Language language : languages) {
				repr = getInverseRepresentation(language);
				if(repr != null){
					return repr;
				}
			}
		}
		if(repr == null){
			repr = getInverseRepresentation(Language.DEFAULT());
		}
		if(repr == null){
			Iterator<Representation> it = getInverseRepresentations().iterator();
			if(it.hasNext()){
				repr = getInverseRepresentations().iterator().next();
			}
		}
		return repr;
	}

	/*
	 * Inverse representation convenience methods similar to TermBase.xxx
	 * @see eu.etaxonomy.cdm.model.common.TermBase#getLabel()
	 */
	@Transient
	public String getInverseLabel() {
		if(getInverseLabel(Language.DEFAULT())!=null){
			return this.getInverseRepresentation(Language.DEFAULT()).getLabel();
		}else{
			for (Representation r : inverseRepresentations){
				return r.getLabel();
			}
		}
		return super.getUuid().toString();
	}

	public String getInverseLabel(Language lang) {
		Representation r = this.getInverseRepresentation(lang);
		if(r==null){
			return null;
		}else{
			return r.getLabel();
		}
	}

	@Transient
	public String getInverseDescription() {
		return this.getInverseRepresentation(Language.DEFAULT()).getDescription();
	}

	public String getInverseDescription(Language lang) {
		return this.getInverseRepresentation(lang).getDescription();
	}

	@Override
	public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms, boolean abbrevAsId) {
		T newInstance = super.readCsvLine(termClass, csvLine, terms, abbrevAsId);

		String inverseText = csvLine.get(6).trim();
		String inverseLabel = csvLine.get(5).trim();
		String inverseLabelAbbrev = csvLine.get(7).trim();
		newInstance.addInverseRepresentation(new Representation(inverseText, inverseLabel, inverseLabelAbbrev, Language.CSV_LANGUAGE()) );
		newInstance.setSymmetric(Boolean.parseBoolean(csvLine.get(8)));
		newInstance.setTransitive(Boolean.parseBoolean(csvLine.get(9)));
		return newInstance;
	}

	@Override
	public void writeCsvLine(CSVWriter writer,T term) {
		String [] line = new String[8];
		line[0] = term.getUuid().toString();
		line[1] = term.getUri().toString();
		line[2] = term.getLabel();
		line[3] = term.getDescription();
		line[4] = term.getDescription();
		line[5] = term.getInverseLabel();
		line[6] = term.getInverseDescription();
		line[7] = String.valueOf(term.isSymmetric());
		line[8] = String.valueOf(term.isTransitive());
		writer.writeNext(line);
	}
	//*********************************** CLONE *********************************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		RelationshipTermBase<?> result = (RelationshipTermBase<?>)super.clone();

		result.inverseRepresentations = new HashSet<Representation>();
		for (Representation rep: this.inverseRepresentations){
			result.addInverseRepresentation((Representation)rep.clone());
		}

		//no changes to: symmetric, transitiv
		return result;
	}


}
