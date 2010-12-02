package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;

public abstract class DescriptionBuilder<T extends DescriptionElementBase> {
	
	protected String separator = ",";
	private int option = 0;
	
	public void returnAbbreviatedLabels() {
		option=1;
	}
	
	public void returnTexts() {
		option=2;
	}
	
	public void returnLabels() {
		option=0;
	}
	
	public void setSeparator(String newSeparator) {
		separator = newSeparator;
	}
	
	public String getSeparator() {
		return separator;
	}
	
	public abstract TextData build(T descriptionElement, List<Language> languages);
	
	protected String getRightText(Representation representation){
		String result;
		if (option==1){
			result = representation.getAbbreviatedLabel();
			if (result != null) return result;
		}
		else if (option==2){
			result = representation.getText();
			if (result != null) return result;
		}
		return representation.getLabel();
	}
	
	public TextData buildTextDataFeature(Feature feature, List<Language> languages){
		return TextData.NewInstance(getRightText(feature.getPreferredRepresentation(languages)),languages.get(0),null);
	}
	
}