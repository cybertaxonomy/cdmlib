package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.term.TermTree;



/**
 * Interface for Natural Language generation 
 * @author m.venin
 * @since 12.04.2010
 *
 */

public interface INaturalLanguageGenerator {
	
	public List<TextData> generateNaturalLanguageDescription(TermTree featureTree, TaxonDescription descriptions);
	
	public List<TextData> generateNaturalLanguageDescription(TermTree featureTree,TaxonDescription description, Language language);
	
	public List<TextData> generatePreferredNaturalLanguageDescription(TermTree featureTree, TaxonDescription description, List<Language> languages);

	public TextData generateSingleTextData(TermTree featureTree, TaxonDescription description);
	
	public TextData generateSingleTextData(TermTree featureTree, TaxonDescription description, Language language);
	
	public TextData generatePreferredSingleTextData(TermTree featureTree, TaxonDescription description, List<Language> languages);
}
