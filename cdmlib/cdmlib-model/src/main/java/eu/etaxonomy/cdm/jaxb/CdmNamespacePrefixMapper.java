/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.jaxb;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class CdmNamespacePrefixMapper extends NamespacePrefixMapper {

	private static final String CDM_PREFIX = "";
	private static final String AGENT_PREFIX = "agent";
	private static final String COMMON_PREFIX = "common";
	private static final String DESCRIPTION_PREFIX = "description";
	private static final String LOCATION_PREFIX = "location";
	private static final String MEDIA_PREFIX = "media";
	private static final String MOLECULAR_PREFIX = "molecular";
	private static final String NAME_PREFIX = "name";
	private static final String OCCURRENCE_PREFIX = "occurrence";
	private static final String REFERENCE_PREFIX = "reference";
	private static final String TAXON_PREFIX = "taxon";
		
	public static final String CDM_NAMESPACE = "http://etaxonomy.eu/cdm/model/1.0";
	public static final String AGENT_NAMESPACE = "http://etaxonomy.eu/cdm/model/agent/1.0";
	public static final String COMMON_NAMESPACE = "http://etaxonomy.eu/cdm/model/common/1.0";
	public static final String DESCRIPTION_NAMESPACE = "http://etaxonomy.eu/cdm/model/description/1.0";
	public static final String LOCATION_NAMESPACE = "http://etaxonomy.eu/cdm/model/location/1.0";
	public static final String MEDIA_NAMESPACE = "http://etaxonomy.eu/cdm/model/media/1.0";
	public static final String MOLECULAR_NAMESPACE = "http://etaxonomy.eu/cdm/model/molecular/1.0";
	public static final String NAME_NAMESPACE = "http://etaxonomy.eu/cdm/model/name/1.0";
	public static final String OCCURRENCE_NAMESPACE = "http://etaxonomy.eu/cdm/model/occurrence/1.0";
	public static final String REFERENCE_NAMESPACE = "http://etaxonomy.eu/cdm/model/reference/1.0";
	public static final String TAXON_NAMESPACE = "http://etaxonomy.eu/cdm/model/taxon/1.0";
	
	public String getPreferredPrefix(String namespaceURI, String suggestion, boolean requirePrefix) {
		String result = suggestion;
		
		/**
		 * Returning empty namespace prefix works if a SAXResult is used, but not
		 * a DOMResult - see http://forums.java.net/jive/thread.jspa?messageID=217155
		 * This has not been resolved yet, as far as I can tell
		 */
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.CDM_NAMESPACE)) {
			return CdmNamespacePrefixMapper.CDM_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.AGENT_NAMESPACE)) {
			return CdmNamespacePrefixMapper.AGENT_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.COMMON_NAMESPACE)) {
			return CdmNamespacePrefixMapper.COMMON_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.DESCRIPTION_NAMESPACE)) {
			return CdmNamespacePrefixMapper.DESCRIPTION_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.LOCATION_NAMESPACE)) {
			return CdmNamespacePrefixMapper.LOCATION_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.MEDIA_NAMESPACE)) {
			return CdmNamespacePrefixMapper.MEDIA_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.MOLECULAR_NAMESPACE)){
			return CdmNamespacePrefixMapper.MOLECULAR_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.NAME_NAMESPACE)) {
			return CdmNamespacePrefixMapper.NAME_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.OCCURRENCE_NAMESPACE)) {
			return CdmNamespacePrefixMapper.OCCURRENCE_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.REFERENCE_NAMESPACE)) {
			return CdmNamespacePrefixMapper.REFERENCE_PREFIX;
		}
		
		if(namespaceURI.equals(CdmNamespacePrefixMapper.TAXON_NAMESPACE)) {
			return CdmNamespacePrefixMapper.TAXON_PREFIX;
		}
		
		return result;
	}

}
