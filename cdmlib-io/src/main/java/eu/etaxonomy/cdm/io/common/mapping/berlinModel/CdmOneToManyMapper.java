/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.berlinModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.CdmSingleAttributeMapperBase;
import eu.etaxonomy.cdm.io.common.mapping.MultipleAttributeMapperBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 20.03.2008
 * @version 1.0
 */
public class CdmOneToManyMapper<ONE extends CdmBase, MANY extends CdmBase, SINGLE_MAPPER extends CdmSingleAttributeMapperBase> extends MultipleAttributeMapperBase<SINGLE_MAPPER> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdmOneToManyMapper.class);

	private Class<MANY> manyClass;
	private Class<ONE> oneClass;
	private String singleAttributeName;

	public CdmOneToManyMapper(Class<ONE> oneClass, Class<MANY> manyClass, String singleAttributeName, SINGLE_MAPPER[] singleAttributesMappers) {
		if (singleAttributesMappers == null){
			throw new NullPointerException("singleAttributesMapper and cdmAttributeStrings must not be null");
		}
		for (SINGLE_MAPPER singleMapper : singleAttributesMappers){
			singleMappers.add(singleMapper);
		}
		this.manyClass = manyClass;
		this.oneClass = oneClass;
		this.singleAttributeName = singleAttributeName;
	}
	
//	@Override
//	public Set<String> getSourceAttributes(){
//		Set<String> result = new HashSet<String>();
//		result.addAll(getSourceAttributeList());
//		return result;
//	}
	
	@Override
	public List<String> getSourceAttributeList(){
		List<String> result = new ArrayList<String>();
		for (SINGLE_MAPPER singleMapper : singleMappers){
			result.add(singleMapper.getSourceAttribute());
		}
		return result;
	}
	
//	@Override
//	public Set<String> getDestinationAttributes(){
//		Set<String> result = new HashSet<String>();
//		result.addAll(getDestinationAttributeList());
//		return result;
//	}
	
	@Override
	public List<String> getDestinationAttributeList(){
		List<String> result = new ArrayList<String>();
		for (SINGLE_MAPPER singleMapper : singleMappers){
			result.add(singleMapper.getDestinationAttribute());
		}
		return result;
	}

	
	public Class<MANY> getManyClass(){
		return manyClass;
	}
	
	public Class<ONE> getOneClass(){
		return oneClass;
	}
	
	public String getDestinationAttribute(String sourceAttribute){
		if (sourceAttribute == null){
			return null;
		}
		for (SINGLE_MAPPER singleMapper : singleMappers){
			if (sourceAttribute.equals(singleMapper.getSourceAttribute())){
				return singleMapper.getDestinationAttribute();
			}
		}
		return null;
	}
	
	public List<SINGLE_MAPPER> getSingleMappers(){
		return singleMappers;
	}

	/**
	 * @return the singleAttributeName
	 */
	public String getSingleAttributeName() {
		return singleAttributeName;
	}
	
}
