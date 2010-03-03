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


/**
 * A base class for all mappers (import + export) which map one attribute in the source to one
 * attribute in the destination
 * 
 * @author a.mueller
 * @created 05.08.2008
 * @version 1.0
 */
public abstract class CdmSingleAttributeMapperBase extends CdmAttributeMapperBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmSingleAttributeMapperBase.class);
	
	private String sourceAttribute;
	private String destinationAttribute;
	protected Object defaultValue;

	protected CdmSingleAttributeMapperBase(String sourceAttributeString, String destinationAttributeString){
		this(sourceAttributeString,destinationAttributeString, null);
	}

	protected CdmSingleAttributeMapperBase(String sourceAttributString, String destinationAttributeString, Object defaultValue){
		this.sourceAttribute = sourceAttributString;
		this.destinationAttribute = destinationAttributeString;
		this.defaultValue = defaultValue;
	}

	
	public String getSourceAttribute(){
		return sourceAttribute;
	}
	
	public String getDestinationAttribute(){
		return destinationAttribute;
	}
	
	@Override
	public Set<String> getSourceAttributes(){
		Set<String>  result = new HashSet<String>();
		result.add(sourceAttribute);
		return result;
	}

	@Override
	public Set<String>  getDestinationAttributes(){
		Set<String>  result = new HashSet<String>();
		result.add(destinationAttribute);
		return result;
	}
	
	
	@Override
	public List<String> getSourceAttributeList(){
		List<String>  result = new ArrayList<String>();
		result.add(sourceAttribute);
		return result;
	}

	@Override
	public List<String>  getDestinationAttributeList(){
		List<String>  result = new ArrayList<String>();
		result.add(destinationAttribute);
		return result;
	}
	
	/**
	 * Returns the type of the cdm attribute
	 * @return
	 */
	public abstract Class getTypeClass();
}