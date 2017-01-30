/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.name;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

public class ZooNameNoMarkerCacheStrategy extends ZooNameDefaultCacheStrategy {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ZooNameNoMarkerCacheStrategy.class);
	private static final long serialVersionUID = 2821727191810867550L;

	final static UUID uuid = UUID.fromString("8ffa5f04-0303-4875-be44-dac5ff95b874");


	@Override
	public UUID getUuid(){
		return uuid;
	}


	/**
	 * Factory method
	 * @return
	 */
	public static ZooNameNoMarkerCacheStrategy NewInstance(){
		return new ZooNameNoMarkerCacheStrategy();
	}

	/**
	 * Constructor
	 */
	private ZooNameNoMarkerCacheStrategy(){
		super();
	}


	@Override
	protected List<TaggedText> getInfraSpeciesTaggedNameCache(ZoologicalName nonViralName){
		boolean includeMarker = false;
		return getInfraSpeciesTaggedNameCache(nonViralName, includeMarker);
	}


	@Override
	protected void addInfraGenericPart(INonViralName name, List<TaggedText> tags, String infraGenericMarker, String infraGenEpi) {
		//add epitheton
		if (StringUtils.isNotBlank(infraGenEpi)){
	        tags.add(new TaggedText(TagEnum.name, "(" + infraGenEpi + ")"));
        }
	}


}
