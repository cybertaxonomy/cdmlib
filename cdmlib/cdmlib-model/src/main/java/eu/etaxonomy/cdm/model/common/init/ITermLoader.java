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
import java.util.Set;
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

	/**
	 * Loads the {@link UUID}s of all vocabularies and all related terms into the given uuidMap.
	 * Where the key of the map is the vocabulary uuid and the values are the related term uuids.
	 * @param vocType vocabulary type
	 * @param uuidMap the resulting uuid map with the key as vocabulary uuid and the value as set of term uuids
	 *        belonging to the given vocabulary
	 */
	public UUID loadUuids(VocabularyEnum vocType, Map<UUID, Set<UUID>> uuidMap);

	/**
	 * Loads all terms for the given vocabulary and with the given term uuids by using the given term loader.
	 * @param vocType VocabularyEnum
	 * @param voc vocabulary
	 * @param missingTerms Set of UUIDs for terms to be loaded.
	 * @return
	 */
	public <T extends DefinedTermBase> Set<T> loadSingleTerms(VocabularyEnum vocType, TermVocabulary<T> voc, 
				Set<UUID> missingTerms);

	

	
}
