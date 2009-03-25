/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.CdmOneToManyMapper;


/**
 * @author a.mueller
 * @created 27.07.2008
 * @version 1.0
 */
public class CdmIoMapping {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmIoMapping.class);
	
	List<CdmAttributeMapperBase> mapperList = new ArrayList<CdmAttributeMapperBase>();
	Set<String> sourceAttributes = new HashSet<String>();
	Set<String> destinationAttributes = new HashSet<String>();
	List<String> sourceAttributeList = new ArrayList<String>();
	List<String> destinationAttributeList = new ArrayList<String>();
	
	
	public void addMapper(CdmAttributeMapperBase mapper){
		mapperList.add(mapper);
		if (mapper instanceof CdmSingleAttributeMapperBase){
			CdmSingleAttributeMapperBase singleMapper = (CdmSingleAttributeMapperBase)mapper;
			sourceAttributes.addAll(singleMapper.getSourceAttributes());
			sourceAttributeList.addAll(singleMapper.getSourceAttributeList());
			destinationAttributes.addAll(singleMapper.getDestinationAttributes());
			destinationAttributeList.addAll(singleMapper.getDestinationAttributeList());
		}else if (mapper instanceof CdmOneToManyMapper<?, ?,?>){
			CdmOneToManyMapper<?, ?,?> multipleMapper = (CdmOneToManyMapper<?, ?,?>)mapper;
			sourceAttributes.addAll(multipleMapper.getSourceAttributes());
			sourceAttributeList.addAll(multipleMapper.getSourceAttributes());
			destinationAttributes.addAll(multipleMapper.getDestinationAttributes());
			destinationAttributeList.addAll(multipleMapper.getDestinationAttributes());
		}else{
			logger.error("Unknown mapper type");
		}
	}
	
	public Set<String> getSourceAttributes(){
		Set<String> result = new HashSet<String>();
		result.addAll(sourceAttributes);
		return result;
	}
	
	public Set<String> getSourceAttributesLowerCase(){
		Set<String> result = new HashSet<String>();
		for(String attr : sourceAttributes){
			if (attr != null){
				result.add(attr.toLowerCase());
			}else{
				result.add(null);
			}
		}
		return result;
	}
	
	public Set<String> getDestinationAttributes(){
		Set<String> result = new HashSet<String>();
		result.addAll(destinationAttributes);
		return result;
	}
	
}
