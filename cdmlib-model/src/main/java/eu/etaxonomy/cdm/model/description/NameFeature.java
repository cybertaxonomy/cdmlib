package eu.etaxonomy.cdm.model.description;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public class NameFeature extends DefinedTermBase<NameFeature>{

	private static NameFeature PROTOLOGUE;
	private static NameFeature ADDITIONAL_PUBLICATION;
	
	
	private static final UUID uuidProtologue = UUID.fromString("7f1fd111-fc52-49f0-9e75-d0097f576b2d");
	private static final UUID uuidAdditionalPublication = UUID.fromString("cb2eab09-6d9d-4e43-8ad2-873f23400930");
	
/* ***************** CONSTRUCTOR AND FACTORY METHODS **********************************/
	

	/** 
	 * Class constructor: creates a new empty namefeature instance.
	 * 
	 * @see #NameFeature(String, String, String)
	 */
	public NameFeature() {
	}
	
	/** 
	 * Class constructor: creates a new namefeature instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new feature to be created 
	 * @param	label  		 the string identifying the new feature to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new feature to be created
	 * @see 				 #Feature()
	 */
	protected NameFeature(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/** 
	 * Creates a new empty namefeature instance.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static NameFeature NewInstance() {
		return new NameFeature();
	}
	
	/** 
	 * Creates a new namefeature instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new namefeature to be created 
	 * @param	label  		 the string identifying the new namefeature to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new namefeature to be created
	 * @see 				 #readCsvLine(List, Language)
	 * @see 				 #NewInstance()
	 */
	public static NameFeature NewInstance(String term, String label, String labelAbbrev){
		return new NameFeature(term, label, labelAbbrev);
	}

/* *************************************************************************************/
	
	@Override
	protected void setDefaultTerms(TermVocabulary<NameFeature> termVocabulary) {
		NameFeature.ADDITIONAL_PUBLICATION = termVocabulary.findTermByUuid(NameFeature.uuidAdditionalPublication);
		NameFeature.PROTOLOGUE = termVocabulary.findTermByUuid(NameFeature.uuidProtologue);
	}
	
	/**
	 * Returns the "protologue" feature. This feature can only be described
	 * with {@link TextData text data} reproducing the content of the protologue 
	 * (or some information about it) of the taxon name. 
	 * 
	 * 
	 */
	public static final NameFeature PROTOLOGUE(){
		return PROTOLOGUE;
	}

	/**
	 * Returns the "additional_publication" feature. This feature can only be
	 * described with {@link TextData text data} with information about a
	 * publication where a {@link TaxonNameBase taxon name} has also been published
	 * but which is not the {@link TaxonNameBase#getNomenclaturalReference() nomenclatural reference}.
	 *  
	 * 
	 */
	public static final NameFeature ADDITIONAL_PUBLICATION(){
		return ADDITIONAL_PUBLICATION;
	}

}
