/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common.init;

import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;

public interface ITermLoader {
	
	/**
	 * Loads the terms for the DefinedTermBase subclass defined by vocType.
	 * The terms will be loadded into the terms map and 
	 * @param <T>
	 * @param vocType
	 * @param terms
	 * @return
	 */
	public <T extends DefinedTermBase> TermVocabulary<T> loadTerms(VocabularyEnum vocType, Map<UUID,DefinedTermBase> terms);

	/**
	 * Unload all static terms. After calling this method all static methods returning terms in DefinedTermBase subclasses 
	 * will return null.
	 */
	public void unloadAllTerms();

	
}
