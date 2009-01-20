package eu.etaxonomy.cdm.model.occurrence;

import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Modifier;

/**
 * modifier for a determination.
 * can be cf. det. rev. conf. for example
 * @author m.doering
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeterminationModifier")
@XmlRootElement(name = "DeterminationModifier")
@Entity
//@Audited
public class DeterminationModifier extends Modifier {
	private static final Logger logger = Logger.getLogger(DeterminationModifier.class);

	private static final UUID uuidUnknown = UUID.fromString("00000000-0000-0000-0000-000000000000");

	
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
	public DeterminationModifier() {
		super();
	}

	
	/**
	 * Constructor
	 */
	protected DeterminationModifier(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}
	
	@Override
	protected void setDefaultTerms(TermVocabulary<Modifier> termVocabulary) {}
}
