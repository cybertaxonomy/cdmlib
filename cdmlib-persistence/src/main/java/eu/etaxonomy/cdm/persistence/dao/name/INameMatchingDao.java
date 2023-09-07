/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.name;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dto.NameMatchingParts;

/**
 * @author andreabee90
 *
 */

public interface INameMatchingDao extends IIdentifiableDao<TaxonName> {

	/**
	 * Supports using wildcards in the query parameters.
	 * If a name part passed to the method contains the
	 * asterisk character ('*') it will be translated into '%' the related field is search with a LIKE clause.
	 * <p>
	 * A query parameter which is passed as <code>NULL</code> value will be ignored.
	 * A parameter passed as {@link Optional} object containing a <code>NULL</code> value will be used
	 * to filter select taxon names where the according field is <code>null</code>.
	 *
	 * @param genusOrUninomial
	 * @param infraGenericEpithet
	 * @param specificEpithet
	 * @param infraSpecificEpithet
	 * @param rank
	 *     Only name having the specified rank are taken into account.
	 * @param excludedNamesUuids
     *     Names to be excluded from the result set
	 * @return
	 */
    public List<NameMatchingParts> findNameMatchingParts(Map<String, Integer> map);
}
