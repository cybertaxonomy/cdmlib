/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.babadshanjan
 * @since 12.09.2008
 */
public interface ILanguageStringDao extends ILanguageStringBaseDao<LanguageString> {

	public List<LanguageString> getAllLanguageStrings(Integer limit, Integer start);

}
