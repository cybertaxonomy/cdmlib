package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;

public abstract class DescriptionBuilder<T extends DescriptionElementBase> {
	public abstract TextData build(T descriptionElement, List<Language> languages);
	
	
}
