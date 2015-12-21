package eu.etaxonomy.cdm.model.description;

import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name="NaturalLanguageTerm")
@XmlRootElement(name = "NaturalLanguageTerm")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class NaturalLanguageTerm extends DefinedTermBase<NaturalLanguageTerm> {
	private static final long serialVersionUID = 6754598791831848705L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NaturalLanguageTerm.class);

	protected static Map<UUID, NaturalLanguageTerm> termMap = null;


	private static NaturalLanguageTerm FROM = new NaturalLanguageTerm();
	private static NaturalLanguageTerm TO = new NaturalLanguageTerm();
	private static NaturalLanguageTerm UP_TO = new NaturalLanguageTerm();
	private static NaturalLanguageTerm MOST_FREQUENTLY = new NaturalLanguageTerm();
	private static NaturalLanguageTerm ON_AVERAGE = new NaturalLanguageTerm();
	private static NaturalLanguageTerm MORE_OR_LESS = new NaturalLanguageTerm();

	private static final UUID uuidTo = UUID.fromString("9087cdcd-8b08-4082-a1de-34c9ba9fb494");


	public static  NaturalLanguageTerm NewInstance(String term, String label, String labelAbbrev) {
		return new NaturalLanguageTerm(term, label, labelAbbrev);
	}


//********************************** Constructor *******************************************************************/

	//for hibernate use only
	@Deprecated
	protected NaturalLanguageTerm() {
		super(TermType.NaturalLanguageTerm);
	}

	private NaturalLanguageTerm(String term, String label, String labelAbbrev) {
		super(TermType.NaturalLanguageTerm, term, label, labelAbbrev);
	}

//********************************** Methods *******************************************************************/


	@Override
	protected void setDefaultTerms(
			TermVocabulary<NaturalLanguageTerm> termVocabulary) {
		//NaturalLanguageTerm.TO = termVocabulary.findTermByUuid(NaturalLanguageTerm.uuidTo);
		//NaturalLanguageTerm.TO.setLabel("eip");

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}


	@XmlElement(name = "KindOf", namespace = "http://etaxonomy.eu/cdm/model/common/1.0")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Override
	public NaturalLanguageTerm getKindOf(){
		return super.getKindOf();
	}

	@Override
    public void setKindOf(NaturalLanguageTerm kindOf){
		super.setKindOf(kindOf);
	}

	/**
	 * Returns the "from" term.
	 */
	public static final NaturalLanguageTerm FROM(){
		NaturalLanguageTerm nlt = FROM;
		Representation representation = Representation.NewInstance("", "from", "", Language.ENGLISH());
		Representation representation2 = Representation.NewInstance("", "de", "", Language.FRENCH());
		Representation representation3 = Representation.NewInstance("", "von", "", Language.GERMAN());
		nlt.addRepresentation(representation);
		nlt.addRepresentation(representation2);
		nlt.addRepresentation(representation3);
		return nlt;
	}

	/**
	 * Returns the "to" term.
	 */
	public static final NaturalLanguageTerm TO(){
		NaturalLanguageTerm nlt = TO;
		Representation representation = Representation.NewInstance("", "to", "", Language.ENGLISH());
		Representation representation2 = Representation.NewInstance("", "à", "", Language.FRENCH());
		nlt.addRepresentation(representation);
		nlt.addRepresentation(representation2);
		return nlt;
	}

	public static final NaturalLanguageTerm UP_TO(){
		NaturalLanguageTerm nlt = UP_TO;
		Representation representation = Representation.NewInstance("", "up to", "", Language.ENGLISH());
		Representation representation2 = Representation.NewInstance("", "jusqu'à", "", Language.FRENCH());
		nlt.addRepresentation(representation);
		nlt.addRepresentation(representation2);
		return nlt;
	}

	public static final NaturalLanguageTerm MOST_FREQUENTLY(){
		NaturalLanguageTerm nlt = MOST_FREQUENTLY;
		Representation representation = Representation.NewInstance("", "most frequently", "", Language.ENGLISH());
		Representation representation2 = Representation.NewInstance("", "plus fréquemment", "", Language.FRENCH());
		nlt.addRepresentation(representation);
		nlt.addRepresentation(representation2);
		return nlt;
	}

	public static final NaturalLanguageTerm ON_AVERAGE(){
		NaturalLanguageTerm nlt = ON_AVERAGE;
		Representation representation = Representation.NewInstance("", "on average", "", Language.ENGLISH());
		Representation representation2 = Representation.NewInstance("", "en moyenne", "", Language.FRENCH());
		nlt.addRepresentation(representation);
		nlt.addRepresentation(representation2);
		return nlt;
	}

	public static final NaturalLanguageTerm MORE_OR_LESS(){
		NaturalLanguageTerm nlt = MORE_OR_LESS;
		Representation representation = Representation.NewInstance("", "+/-", "", Language.ENGLISH());
		nlt.addRepresentation(representation);
		return nlt;
	}
}
