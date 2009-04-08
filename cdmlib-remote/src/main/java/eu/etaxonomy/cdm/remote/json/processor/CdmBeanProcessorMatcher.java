// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor;

import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;

import net.sf.json.processors.JsonBeanProcessorMatcher;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.kohlbecker
 *
 */
public class CdmBeanProcessorMatcher extends JsonBeanProcessorMatcher {

	public static final Logger logger = Logger.getLogger(CdmBeanProcessorMatcher.class);
	
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
		if (Taxon.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(Taxon.class, set);
		}
		if (TaxonNameBase.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(TaxonNameBase.class, set);
		}
		if (TermBase.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(TermBase.class, set);
		}
		
		return DEFAULT.getMatch(target, set);
	}
	
	
}
