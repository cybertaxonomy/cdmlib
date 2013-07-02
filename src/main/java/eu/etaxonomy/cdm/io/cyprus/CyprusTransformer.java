// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cyprus;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.MarkerType;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class CyprusTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CyprusTransformer.class);
	

	//feature
	public static final UUID redBookUuid =  UUID.fromString("df59d44a-ee5a-4c01-8637-127cc804842d");
	public static final UUID endemismUuid =  UUID.fromString("dd343c31-1916-4786-a530-536ea995dce4");
	
	//presenceTerm
	public static final UUID indigenousUuid = UUID.fromString("b325859b-504b-45e0-9ef0-d5c1602fcc0f");
	public static final UUID casualUuid = UUID.fromString("5e81353c-38a3-4ca6-b979-0d9abc93b877");
	public static final UUID nonInvasiveUuid = UUID.fromString("1b025e8b-901a-42e8-9739-119b410c6f03");
	public static final UUID invasiveUuid = UUID.fromString("faf2d271-868a-4bf7-b0b8-a1c5ab309de2");
	public static final UUID questionableUuid = UUID.fromString("4b48f675-a6cf-49f3-a5ba-77e2c2979eb3");

	public static final UUID indigenousDoubtfulUuid = UUID.fromString("17bc601f-53eb-4997-a4bc-c03ce5bfd1d3");
	public static final UUID casualDoubtfulUuid = UUID.fromString("73f75493-1185-4a3e-af1e-9a1f2e8dadb7");
	public static final UUID nonInvasiveDoubtfulUuid = UUID.fromString("11f56e2f-c16c-4b3d-a870-bb5d3b20e624");
	public static final UUID invasiveDoubtfulUuid = UUID.fromString("ac429d5f-e8ad-49ae-a41c-e4779b58b96a");
	public static final UUID questionableDoubtfulUuid = UUID.fromString("914e7393-1314-4632-bc45-5eff3dc1e424");

	public static final UUID cultivatedDoubtfulUuid = UUID.fromString("4f31bfc8-3058-4d83-aea5-3a1fe9773f9f");
	
	//Named Area - divisions 
	public static final UUID uuidCyprusDivisionsVocabulary = UUID.fromString("2119f610-1f93-4d87-af28-40aeefaca100");
	public static final UUID uuidCyprusDivisionsAreaLevel = UUID.fromString("ff52bbd9-f73d-4476-af39-f3991fa892bd");
	
	public static final UUID uuidDivision1 = UUID.fromString("ab17eee9-1abb-4ce9-a9a2-563f840cdbfc");
	public static final UUID uuidDivision2 = UUID.fromString("c3606165-efb7-4224-a168-63e009eb4aa5");
	public static final UUID uuidDivision3 = UUID.fromString("750d4e07-e34b-491f-a7b7-09723afdc960");
	public static final UUID uuidDivision4 = UUID.fromString("8a858922-e8e5-4791-ad53-906e50633ec7");
	public static final UUID uuidDivision5 = UUID.fromString("16057133-d541-4ebd-81d4-cb92265ec54c");
	public static final UUID uuidDivision6 = UUID.fromString("fbf21230-4a42-4f4c-9af8-5da52123c264");
	public static final UUID uuidDivision7 = UUID.fromString("d31dd96a-36ea-4428-871c-d8552a9565ca");
	public static final UUID uuidDivision8 = UUID.fromString("236ea447-c3ab-486d-9e06-cc5907861acc");
	
	

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getMarkerTypeByKey(java.lang.String)
	 */
	@Override
	public MarkerType getMarkerTypeByKey(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
//		}else if (key.equalsIgnoreCase("distribution")){return MarkerType.;
//		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.ECOLOGY();
		}else{
			return null;
		}
	}

	@Override
	public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
//		}else if (key.equalsIgnoreCase("IMPERFECTLY KNOWN SPECIES")){return uuidIncompleteTaxon;
		}else{
			return null;
		}

	}
	
	@Override
	public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("IN")){return indigenousUuid;
		}else if (key.equalsIgnoreCase("CA")){return casualUuid;
		}else if (key.equalsIgnoreCase("NN")){return nonInvasiveUuid;
		}else if (key.equalsIgnoreCase("NA")){return invasiveUuid;
		}else if (key.equalsIgnoreCase("Q")){return questionableUuid;
		}else if (key.equalsIgnoreCase("IN?")){return indigenousDoubtfulUuid;
		}else if (key.equalsIgnoreCase("CA?")){return casualDoubtfulUuid;
		}else if (key.equalsIgnoreCase("NN?")){return nonInvasiveDoubtfulUuid;
		}else if (key.equalsIgnoreCase("NA?")){return invasiveDoubtfulUuid;
		}else if (key.equalsIgnoreCase("Q?")){return questionableDoubtfulUuid;
		}else if (key.equalsIgnoreCase("CU?")){return cultivatedDoubtfulUuid;
		}else{
			return null;
		}

	}
	
	@Override
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("Red book")){return redBookUuid;
		}else if (key.equalsIgnoreCase("Endemism")){return endemismUuid;
		}else{
			return null;
		}

	}

	@Override
	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("1")){return uuidDivision1;
		}else if (key.equalsIgnoreCase("2")){return uuidDivision2;
		}else if (key.equalsIgnoreCase("3")){return uuidDivision3;
		}else if (key.equalsIgnoreCase("4")){return uuidDivision4;
		}else if (key.equalsIgnoreCase("5")){return uuidDivision5;
		}else if (key.equalsIgnoreCase("6")){return uuidDivision6;
		}else if (key.equalsIgnoreCase("7")){return uuidDivision7;
		}else if (key.equalsIgnoreCase("8")){return uuidDivision8;
		}else{
			return null;
		}	
	}
	
	
	
	
	
	
	
}
