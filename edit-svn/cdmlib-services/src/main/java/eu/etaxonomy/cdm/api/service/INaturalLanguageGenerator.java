package eu.etaxonomy.cdm.api.service;

import java.util.Set;
import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.DescriptionBase;



/**
 * Interface for Natural Language generation 
 * @author m.venin
 * @date 12.04.2010
 *
 */

public interface INaturalLanguageGenerator {
	
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree, TaxonDescription descriptions);
	
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description, Language language);
	
	public List<TextData> generatePreferredNaturalLanguageDescription(FeatureTree featureTree, TaxonDescription description, List<Language> languages);

	public TextData generateSingleTextData(FeatureTree featureTree, TaxonDescription description);
	
	public TextData generateSingleTextData(FeatureTree featureTree, TaxonDescription description, Language language);
	
	public TextData generatePreferredSingleTextData(FeatureTree featureTree, TaxonDescription description, List<Language> languages);
}
