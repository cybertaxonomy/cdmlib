package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.LanguageStringBase;
import eu.etaxonomy.cdm.model.common.Representation;

public interface IRepresentationDao extends ILanguageStringBaseDao<Representation> {

	public List<Representation> getAllRepresentations(Integer limit, Integer start);

}
