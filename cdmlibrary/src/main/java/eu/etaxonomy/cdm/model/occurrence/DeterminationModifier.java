package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Modifier;

@Entity
public class DeterminationModifier extends Modifier {
	static Logger logger = Logger.getLogger(DeterminationModifier.class);

	public DeterminationModifier() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DeterminationModifier(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

}
