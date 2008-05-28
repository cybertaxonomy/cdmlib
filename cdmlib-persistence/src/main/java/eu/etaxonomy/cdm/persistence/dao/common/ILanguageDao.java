/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.common;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 27.05.2008 12:00:34
 *
 */
public interface ILanguageDao extends IDefinedTermDao {

	public Language getByIso(String iso639);
}
