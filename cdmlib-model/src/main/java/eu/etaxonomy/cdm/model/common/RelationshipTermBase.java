package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import au.com.bytecode.opencsv.CSVWriter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RelationshipTermBase", propOrder = {
    "symmetric",
    "transitive",
    "inverseRepresentations"
})
@XmlRootElement(name = "RelationshipTermBase")
@MappedSuperclass
public abstract class RelationshipTermBase<T extends RelationshipTermBase> extends OrderedTermBase<T> {
	
	static Logger logger = Logger.getLogger(RelationshipTermBase.class);
	
	@XmlElement(name = "Symmetric")
	private boolean symmetric;
	
	@XmlElement(name = "Transitive")
	private boolean transitive;
	
	@XmlElementWrapper(name = "InverseRepresentations")
	@XmlElement(name = "InverseRepresentation")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<Representation> inverseRepresentations = new HashSet();
	
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
	
	
	@OneToMany
	@JoinTable(name="RelationshipTermBase_inverseRepresentation")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<Representation> getInverseRepresentations() {
		return inverseRepresentations;
	}
	protected void setInverseRepresentations(
			Set<Representation> inverseRepresentations) {
		this.inverseRepresentations = inverseRepresentations;
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
	
	@Transient
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

	@Transient
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

	@Transient
	public String getInverseDescription(Language lang) {
		return this.getInverseRepresentation(lang).getDescription();
	}
	
	@Override
	public ILoadableTerm readCsvLine(List csvLine) {
		RelationshipTermBase result;
		// read UUID, URI, english label+description
		List<String> csvLineString = csvLine;
		result = (RelationshipTermBase)super.readCsvLine(csvLineString);
		// inverse label + 2 booleans
		String inverseText = csvLineString.get(5).trim();
		String inverseLabel = csvLineString.get(4).trim();
		String inverseLabelAbbrev = null;
		result.addInverseRepresentation(new Representation(inverseText, inverseLabel, inverseLabelAbbrev, Language.ENGLISH()) );
		result.setSymmetric(Boolean.parseBoolean(csvLineString.get(6)));
		result.setTransitive(Boolean.parseBoolean(csvLineString.get(7)));
		return result;
	}
	
	@Override
	public void writeCsvLine(CSVWriter writer) {
		String [] line = new String[8];
		line[0] = getUuid().toString();
		line[1] = getUri();
		line[2] = getLabel();
		line[3] = getDescription();
		line[4] = getInverseLabel();
		line[5] = getInverseDescription();
		line[6] = String.valueOf(this.isSymmetric());
		line[7] = String.valueOf(this.isTransitive());
		writer.writeNext(line);
	}
	
}
