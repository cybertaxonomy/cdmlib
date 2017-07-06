/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.matcher;

import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import net.sf.json.processors.JsonBeanProcessorMatcher;

/**
 * can handle HibernateProxys
 *
 * @author a.kohlbecker
 *
 */
public class CdmProxyBeanProcessorMatcher extends JsonBeanProcessorMatcher {

    public static final Logger logger = Logger.getLogger(CdmProxyBeanProcessorMatcher.class);

    /* (non-Javadoc)
     * @see net.sf.json.processors.JsonBeanProcessorMatcher#getMatch(java.lang.Class, java.util.Set)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getMatch(Class target, Set set) {


        if (HibernateProxy.class.isAssignableFrom(target)) {
            if(logger.isDebugEnabled()){
                logger.debug("Found HibernateProxy object of class " + target.getClass() + " returning " + HibernateProxy.class);
            }
            return HibernateProxy.class;
        }


        // TODO implement generically by making use of CdmBaseType
        if (TaxonBase.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(TaxonBase.class, set);
        }
        if (TaxonName.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(TaxonName.class, set);
        }
        if (TermBase.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(TermBase.class, set);
        }
        if (TeamOrPersonBase.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(TeamOrPersonBase.class, set);
        }
        if (Media.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(Media.class, set);
        }
        if (Reference.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(Reference.class, set);
        }
        if (TypeDesignationBase.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(TypeDesignationBase.class, set);
        }
        if (DescriptionElementBase.class.isAssignableFrom(target)) {
            return DEFAULT.getMatch(DescriptionElementBase.class, set);
        }

        return DEFAULT.getMatch(target, set);
    }


}
