/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.term;

import java.util.List;

import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.common.IVersionableDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.babadshanjan
 * @since 09.09.2008
 */
public interface ITermNodeDao extends IVersionableDao<TermNode> {

	public List<TermNode> list();

    /**
     * Loads a list of term nodes depending on the term type including included term types
     * @param termType the term type
     * @return
     */
    public List<TermNode> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths);

}
