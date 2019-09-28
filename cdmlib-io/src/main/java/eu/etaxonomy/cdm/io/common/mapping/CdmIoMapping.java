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

import eu.etaxonomy.cdm.io.common.mapping.out.ObjectChangeMapper;

/**
 * @author a.mueller
 * @since 27.07.2008
 */
public class CdmIoMapping {

	private static final Logger logger = Logger.getLogger(CdmIoMapping.class);

	//protected List<CdmAttributeMapperBase> mapperList = new ArrayList<>();
	protected List<CdmMapperBase> mapperList = new ArrayList<>();
	Set<String> sourceAttributes = new HashSet<>();
	Set<String> destinationAttributes = new HashSet<>();
	List<String> sourceAttributeList = new ArrayList<>();
	List<String> destinationAttributeList = new ArrayList<>();

	public void addMapper(CdmAttributeMapperBase mapper){
		if (mapper == null){
			return;
		}
		mapperList.add(mapper);
		if (mapper instanceof CdmSingleAttributeMapperBase){
			CdmSingleAttributeMapperBase singleMapper = (CdmSingleAttributeMapperBase)mapper;
			sourceAttributes.addAll(singleMapper.getSourceAttributes());
			sourceAttributeList.addAll(singleMapper.getSourceAttributeList());
			destinationAttributes.addAll(singleMapper.getDestinationAttributes());
			destinationAttributeList.addAll(singleMapper.getDestinationAttributeList());
		}else if (mapper instanceof MultipleAttributeMapperBase){
			MultipleAttributeMapperBase<?> multipleMapper = (MultipleAttributeMapperBase<?>)mapper;
			sourceAttributes.addAll(multipleMapper.getSourceAttributes());
			sourceAttributeList.addAll(multipleMapper.getSourceAttributes());
			destinationAttributes.addAll(multipleMapper.getDestinationAttributes());
			destinationAttributeList.addAll(multipleMapper.getDestinationAttributeList());
		}else if (mapper instanceof ObjectChangeMapper){
			ObjectChangeMapper changeMapper = (ObjectChangeMapper)mapper;
			sourceAttributes.addAll(changeMapper.getSourceAttributes());
			sourceAttributeList.addAll(changeMapper.getSourceAttributes());
			destinationAttributes.addAll(changeMapper.getDestinationAttributes());
			destinationAttributeList.addAll(changeMapper.getDestinationAttributeList());

		}else{
			logger.error("Unknown mapper type: " + mapper.getClass().getSimpleName());
			throw new IllegalArgumentException("Unknown mapper type: " + mapper.getClass().getSimpleName());
		}
	}

	public Set<String> getSourceAttributes(){
		Set<String> result = new HashSet<>();
		result.addAll(sourceAttributes);
		return result;
	}

	public Set<String> getSourceAttributesLowerCase(){
		Set<String> result = new HashSet<>();
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
		Set<String> result = new HashSet<>();
		result.addAll(destinationAttributes);
		return result;
	}

	public List<String> getDestinationAttributeList(){
		List<String> result = new ArrayList<>();
		result.addAll(destinationAttributeList);
		return result;
	}
}
