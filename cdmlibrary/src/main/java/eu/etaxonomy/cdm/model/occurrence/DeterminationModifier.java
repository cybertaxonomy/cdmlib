package eu.etaxonomy.cdm.model.occurrence;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Enumeration;
import eu.etaxonomy.cdm.model.description.Modifier;

public class DeterminationModifier extends Modifier {
	static Logger logger = Logger.getLogger(DeterminationModifier.class);

	public DeterminationModifier(String term, String label,
			Enumeration enumeration) {
		super(term, label, enumeration);
		// TODO Auto-generated constructor stub
	}

}
