/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.term;

import java.util.List;

import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringBaseDao;

/**
 * FIXME Candidate for removal. This class is only used in SDDExport and it is
 * used incorrectly. Export needs to be fixed and class removed.
 *
 * @deprecated this class will be removed
 */
@Deprecated
public interface IRepresentationDao extends ILanguageStringBaseDao<Representation> {

	public List<Representation> getAllRepresentations(Integer limit, Integer start);

}
