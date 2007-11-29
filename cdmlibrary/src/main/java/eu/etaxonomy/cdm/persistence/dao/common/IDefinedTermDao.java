package eu.etaxonomy.cdm.persistence.dao.common;

import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

public interface IDefinedTermDao extends ICdmEntityDao<DefinedTermBase>, ITitledDao<DefinedTermBase>{
}
