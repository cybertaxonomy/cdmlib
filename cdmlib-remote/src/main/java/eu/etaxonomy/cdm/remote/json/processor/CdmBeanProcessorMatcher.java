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

import org.hamcrest.core.IsInstanceOf;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

import net.sf.json.processors.JsonBeanProcessorMatcher;

/**
 * @author a.kohlbecker
 *
 */
public class CdmBeanProcessorMatcher extends JsonBeanProcessorMatcher {

	/* (non-Javadoc)
	 * @see net.sf.json.processors.JsonBeanProcessorMatcher#getMatch(java.lang.Class, java.util.Set)
	 */
	@Override
	public Object getMatch(Class target, Set set) {
		if(target.getClass().isAssignableFrom(Taxon.class)){
			return DEFAULT.getMatch(Taxon.class, set);
		}
		if(target.getClass().isAssignableFrom(Taxon.class)){
			return DEFAULT.getMatch(TaxonBase.class, set);
		}
		return DEFAULT.getMatch(target, set);
	}
}
