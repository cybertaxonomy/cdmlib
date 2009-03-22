package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

import au.com.bytecode.opencsv.CSVWriter;

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
	
	@XmlElement(name = "Symmetric")
	private boolean symmetric;
	
	@XmlElement(name = "Transitive")
	private boolean transitive;
	
	@XmlElementWrapper(name = "InverseRepresentations")
	@XmlElement(name = "InverseRepresentation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
		@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name="RelationshipTermBase_inverseRepresentation")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	private Set<Representation> inverseRepresentations = new HashSet<Representation>();
	
	public RelationshipTermBase() {
		super();
	}
	public RelationshipTermBase(String term, String label, String labelAbbrev, boolean symmetric, boolean transitive) {
		super(term, label, labelAbbrev);
		setSymmetric(symmetric);
		setTransitive(transitive);		
	}

	
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
				if (repr.getLanguage() == lang){
					result = repr;
				}
			}
		}else{
			for (Representation repr : this.getInverseRepresentations()){
				if (repr.getLanguage() == lang){
					result = repr;
				}
			}
		}
		return result;
	}
	
	/*
	 * Inverse representation convenience methods similar to TermBase.xxx 
	 * @see eu.etaxonomy.cdm.model.common.TermBase#getLabel()
	 */
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

	public String getInverseDescription() {
		return this.getInverseRepresentation(Language.DEFAULT()).getDescription();
	}

	public String getInverseDescription(Language lang) {
		return this.getInverseRepresentation(lang).getDescription();
	}
	
	@Override
	public T readCsvLine(Class<T> termClass, List<String> csvLine, Map<UUID,DefinedTermBase> terms) {
		T newInstance = super.readCsvLine(termClass, csvLine, terms);

		String inverseText = csvLine.get(5).trim();
		String inverseLabel = csvLine.get(4).trim();
		String inverseLabelAbbrev = null;
		newInstance.addInverseRepresentation(new Representation(inverseText, inverseLabel, inverseLabelAbbrev, Language.ENGLISH()) );
		newInstance.setSymmetric(Boolean.parseBoolean(csvLine.get(6)));
		newInstance.setTransitive(Boolean.parseBoolean(csvLine.get(7)));
		return newInstance;
	}
	
	@Override
	public void writeCsvLine(CSVWriter writer,T term) {
		String [] line = new String[8];
		line[0] = term.getUuid().toString();
		line[1] = term.getUri();
		line[2] = term.getLabel();
		line[3] = term.getDescription();
		line[4] = term.getInverseLabel();
		line[5] = term.getInverseDescription();
		line[6] = String.valueOf(term.isSymmetric());
		line[7] = String.valueOf(term.isTransitive());
		writer.writeNext(line);
	}
	
}
