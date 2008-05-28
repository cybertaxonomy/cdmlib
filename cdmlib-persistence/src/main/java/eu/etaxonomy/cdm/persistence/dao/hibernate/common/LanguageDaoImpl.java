/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.hibernate.Query;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageDao;

/**
 * 
 * @author a.kohlbecker
 * @version 1.0
 * @created 27.05.2008 12:02:26
 *
 */
public class LanguageDaoImpl extends DefinedTermDaoImpl implements ILanguageDao {

	   public Language getByIso(String iso639) {
		String isoStandart = "iso639_" + (iso639.length() - 1);
		Query query = getSession().createQuery("select lang from Language where lang." 
				+ isoStandart + " = :isoCode");
		query.setParameter("isoCode", iso639);
		return (Language) query.uniqueResult();
	}
}
