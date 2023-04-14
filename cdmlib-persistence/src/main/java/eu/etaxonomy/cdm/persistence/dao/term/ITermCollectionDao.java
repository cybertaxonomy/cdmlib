/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.term;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.TermSearchField;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermGraphBase;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

/**
 * @author a.mueller
 * @date 12.04.2023
 */
public interface ITermCollectionDao extends IIdentifiableDao<TermCollection> {

    /**
     * Returns a set of terms available in the given term graph by a label pattern
     */
    public <TERM extends DefinedTermBase> List<TERM> listTerms(Class<TERM> type, List<TermGraphBase> graphs,
            Integer limit, String pattern, TermSearchField labelType, Language lang);


}
