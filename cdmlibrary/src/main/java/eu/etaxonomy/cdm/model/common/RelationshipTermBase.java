package eu.etaxonomy.cdm.model.common;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class RelationshipTermBase extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(RelationshipTermBase.class);

	public RelationshipTermBase() {
		super();
	}

	private boolean symmetric;
	private boolean transitive;
	private Set<Representation> inverseRepresentations;
	
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
	public Set<Representation> getInverseRepresentations() {
		return inverseRepresentations;
	}
	protected void setInverseRepresentations(
			Set<Representation> inverseRepresentations) {
		this.inverseRepresentations = inverseRepresentations;
	}
	public void addRepresentation(Representation representation) {
		this.inverseRepresentations.add(representation);
	}
	public void removeRepresentation(Representation representation) {
		this.inverseRepresentations.remove(representation);
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
}
