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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapper base class mapping a single source attribute to a single destination attribute.
 *
 * @author a.mueller
 * @since 05.08.2008
 */
public abstract class CdmSingleAttributeMapperBase extends CdmAttributeMapperBase{

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private String sourceValue;
	private String destinationValue;
	protected Object defaultValue;

	protected CdmSingleAttributeMapperBase(String sourceAttributString, String destinationAttributeString){
		this(sourceAttributString, destinationAttributeString, null);
	}

	protected CdmSingleAttributeMapperBase(String sourceAttributString, String destinationAttributeString, Object defaultValue){
		this.sourceValue = sourceAttributString;
		this.destinationValue = destinationAttributeString;
		this.defaultValue = defaultValue;
	}

	public String getSourceAttribute(){
		return sourceValue;
	}

	public String getDestinationAttribute(){
		return destinationValue;
	}

	@Override
	public Set<String> getSourceAttributes(){
		Set<String>  result = new HashSet<>();
		result.add(sourceValue);
		return result;
	}

	@Override
	public Set<String> getDestinationAttributes(){
		Set<String> result = new HashSet<>();
		if(destinationValue != null){
			result.add(destinationValue);
		}
		return result;
	}

	@Override
	public List<String> getSourceAttributeList(){
		List<String> result = new ArrayList<>();
		result.add(sourceValue);
		return result;
	}

	@Override
	public List<String> getDestinationAttributeList(){
		List<String> result = new ArrayList<>();
		if(destinationValue != null){
			result.add(destinationValue);
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
    public abstract Class getTypeClass();
}
