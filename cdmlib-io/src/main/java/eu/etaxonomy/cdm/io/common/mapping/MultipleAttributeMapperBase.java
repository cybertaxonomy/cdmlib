// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase;


/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public abstract class MultipleAttributeMapperBase<SINGLE_MAPPER extends CdmSingleAttributeMapperBase> extends CdmAttributeMapperBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MultipleAttributeMapperBase.class);

	protected List<SINGLE_MAPPER> singleMappers = new ArrayList<SINGLE_MAPPER>();

	
	/**
	 * 
	 */
	public MultipleAttributeMapperBase() {
		singleMappers = new ArrayList<SINGLE_MAPPER>();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase#getDestinationAttributeList()
	 */
	@Override
	public List<String> getDestinationAttributeList() {
		List<String> result = new ArrayList<String>();
		for (SINGLE_MAPPER singleMapper : singleMappers){
			result.add(singleMapper.getDestinationAttribute());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase#getDestinationAttributes()
	 */
	@Override
	public Set<String> getDestinationAttributes() {
		Set<String> result = new HashSet<String>();
		result.addAll(getDestinationAttributeList());
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase#getSourceAttributeList()
	 */
	@Override
	public List<String> getSourceAttributeList() {
		List<String> result = new ArrayList<String>();
		for (SINGLE_MAPPER singleMapper : singleMappers){
			result.add(singleMapper.getSourceAttribute());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmAttributeMapperBase#getSourceAttributes()
	 */
	@Override
	public Set<String> getSourceAttributes() {
		Set<String> result = new HashSet<String>();
		result.addAll(getSourceAttributeList());
		return result;
	}
}
