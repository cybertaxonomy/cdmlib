// $Id$
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

import net.sf.json.processors.JsonBeanProcessorMatcher;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

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
		if (Taxon.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(Taxon.class, set);
		}
		if (TaxonNameBase.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(TaxonNameBase.class, set);
		}
		if (TermBase.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(TermBase.class, set);
		}
//		if (NameRelationship.class.isAssignableFrom(target)) {
//			return DEFAULT.getMatch(NameRelationship.class, set);
//		}
//		if (TaxonRelationship.class.isAssignableFrom(target)) {
//			return DEFAULT.getMatch(TaxonRelationship.class, set);
//		}
		if (Media.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(Media.class, set);
		}
		if (ReferenceBase.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(ReferenceBase.class, set);
		}
		if (TypeDesignationBase.class.isAssignableFrom(target)) {
			return DEFAULT.getMatch(TypeDesignationBase.class, set);
		}

		return DEFAULT.getMatch(target, set);
	}
	
	
}
