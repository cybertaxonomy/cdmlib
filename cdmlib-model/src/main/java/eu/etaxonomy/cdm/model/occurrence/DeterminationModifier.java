package eu.etaxonomy.cdm.model.occurrence;

import javax.persistence.Entity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.description.Modifier;

/**
 * modifier for a determination.
 * can be cf. det. rev. conf. for example
 * @author m.doering
 *
 */
@Entity
public class DeterminationModifier extends Modifier {
	private static final Logger logger = Logger.getLogger(DeterminationModifier.class);

	/**
	 * Factory method
	 * @return
	 */
	public static DeterminationModifier NewInstance() {
		return new DeterminationModifier();
	}

	/**
	 * Factory method
	 * @return
	 */
	public static DeterminationModifier NewInstance(String term, String label, String labelAbbrev) {
		return new DeterminationModifier(term, label, labelAbbrev);
	}
	
	
	/**
	 * Constructor
	 */
	protected DeterminationModifier() {
		super();
	}

	
	/**
	 * Constructor
	 */
	protected DeterminationModifier(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

}
