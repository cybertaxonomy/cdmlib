/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface ITermService extends IService<DefinedTermBase> {

	public abstract DefinedTermBase getTermByUri(String uri);

	public abstract DefinedTermBase getTermByUuid(UUID uuid);

	public abstract TermVocabulary getVocabulary(UUID vocabularyUuid);
	
	public abstract Set<TermVocabulary> listVocabularies(Class termClass);

}
