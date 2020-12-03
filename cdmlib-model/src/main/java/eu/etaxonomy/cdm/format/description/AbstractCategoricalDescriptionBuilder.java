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
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * @author m.venin
 * @since 2010
 */
public abstract class AbstractCategoricalDescriptionBuilder
        extends DescriptionBuilder<CategoricalData>{

	@Override
    public TextData build(CategoricalData data, List<Language> languages) {
		   return doBuild(data.getStateData(), languages);
	}

	protected abstract TextData doBuild(List<StateData> stateDatas, List<Language> languages);

}
