/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.persistence.dao.common.ILanguageStringDao;

/**
 * @author a.babadshanjan
 * @since 12.09.2008
 */
@Repository
public class LanguageStringDaoImpl
        extends AnnotatableDaoBaseImpl<LanguageString>
        implements ILanguageStringDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	public LanguageStringDaoImpl() {
		super(LanguageString.class);
	}
}
