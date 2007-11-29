package eu.etaxonomy.cdm.model.common;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@MappedSuperclass
public abstract class RelationshipTermBase extends OrderedTermBase {
	static Logger logger = Logger.getLogger(RelationshipTermBase.class);
	
	private boolean symmetric;
	private boolean transitive;
	private Set<Representation> inverseRepresentations = new HashSet();
	
	public RelationshipTermBase() {
		super();
	}
	public RelationshipTermBase(String term, String label, boolean symmetric, boolean transitive) {
		super(term, label);
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
		if(getLabel(Language.DEFAULT())!=null){
			return this.getInverseRepresentation(Language.DEFAULT()).getLabel();
		}else{
			for (Representation r : inverseRepresentations){
				return r.getLabel();
			}			
		}
		return super.getUuid();
	}

	@Transient
	public String getInverseLabel(Language lang) {
		return this.getInverseRepresentation(lang).getLabel();
	}

	@Transient
	public String getInverseDescription() {
		return this.getInverseRepresentation(Language.DEFAULT()).getDescription();
	}

	@Transient
	public String getInverseDescription(Language lang) {
		return this.getInverseRepresentation(lang).getDescription();
	}
}
