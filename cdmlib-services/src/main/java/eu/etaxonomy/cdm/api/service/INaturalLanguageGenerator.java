package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.TextData;



/**
 * Interface for Natural Language generation
 * @author m.venin
 * @since 12.04.2010
 *
 */

public interface INaturalLanguageGenerator {

	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree, DescriptionBase descriptions);

	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,DescriptionBase description, Language language);

	public List<TextData> generatePreferredNaturalLanguageDescription(FeatureTree featureTree, DescriptionBase description, List<Language> languages);

	public TextData generateSingleTextData(FeatureTree featureTree, DescriptionBase description);

	public TextData generateSingleTextData(FeatureTree featureTree, DescriptionBase description, Language language);

	public TextData generatePreferredSingleTextData(FeatureTree featureTree, DescriptionBase description, List<Language> languages);
}
