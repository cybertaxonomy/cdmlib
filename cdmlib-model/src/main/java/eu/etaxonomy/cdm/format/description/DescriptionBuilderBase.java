/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * Abstract class that defines the basic element for constructing natural language descriptions.
 *
 * @author m.venin
 */
public abstract class DescriptionBuilderBase<T extends DescriptionElementBase> {

	protected String separator = ",";// the basic separator, used for example to separate states when building a description
	// of a CategoricalData
	private int option = 0; // option used to return either the text, the label or the abbreviation of a Representation.
	// By default a builder returns the label


	/**
	 * Sets the builder to return the abbreviation contained in the Representation element of an object
	 */
	public void returnAbbreviatedLabels() {
		option=1;
	}

	/**
	 * Sets the builder to return the text contained in the Representation element of an object
	 */
	public void returnTexts() {
		option=2;
	}

	/**
	 * Sets the builder to return the label contained in the Representation element of an object
	 */
	public void returnLabels() {
		option=0;
	}

	public void setSeparator(String newSeparator) {
		separator = newSeparator;
	}

	public String getSeparator() {
		return separator;
	}

	/**
	 * Returns the TextData element with the description of the according DescriptionElement
	 */
	public abstract TextData build(T descriptionElement, List<Language> languages);

	/**
	 * Returns either the text, label or abbreviation of a Representation.
	 */
	protected String getRightText(Representation representation){
		String result;
		if (option==1){
			result = representation.getAbbreviatedLabel();
			if (result != null) {
                return result;
            }
		}
		else if (option==2){
			result = representation.getText();
			if (result != null) {
                return result;
            }
		}
		return representation.getLabel();
	}

	/**
	 * Returns a TextData with the name of the feature.
	 */
	public TextData buildTextDataFeature(Feature feature, List<Language> languages){
		return TextData.NewInstance(getRightText(feature.getPreferredRepresentation(languages)),languages.get(0),null);
	}
}