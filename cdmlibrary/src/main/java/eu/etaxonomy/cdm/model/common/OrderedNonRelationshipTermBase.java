package eu.etaxonomy.cdm.model.common;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public abstract class OrderedNonRelationshipTermBase extends OrderedTermBase {

	public OrderedNonRelationshipTermBase(String term, String label,
			TermVocabulary enumeration) {
		super(term, label, enumeration);
		// TODO Auto-generated constructor stub
	}

}
