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

public class TDWGNamespacePrefixMapper extends NamespacePrefixMapper {

	private static final String RDF_PREFIX = "rdf";
	private static final String DC_PREFIX = "dc";
	private static final String DCTERMS_PREFIX = "dcterms";
	private static final String OWL_PREFIX = "owl";
	private static final String TAXONNAME_PREFIX = "tn";
	private static final String TAXONCONCEPT_PREFIX = "tc";
	private static final String COMMON_PREFIX = "tcom";
	private static final String PERSON_PREFIX = "tp";
	private static final String TEAM_PREFIX = "tt";
	private static final String PUBLICATIONCITATION_PREFIX = "tpc";
		
	public static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
	public static final String DCTERMS_NAMESPACE = "http://purl.org/dc/terms/";
	public static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
	public static final String TAXONNAME_NAMESPACE = "http://rs.tdwg.org/ontology/voc/TaxonName#";
	public static final String TAXONCONCEPT_NAMESPACE = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
	public static final String COMMON_NAMESPACE = "http://rs.tdwg.org/ontology/voc/Common#";
	public static final String PERSON_NAMESPACE = "http://rs.tdwg.org/ontology/voc/Person#";
	public static final String TEAM_NAMESPACE = "http://rs.tdwg.org/ontology/voc/Team#";
	public static final String PUBLICATIONCITATION_NAMESPACE = "http://rs.tdwg.org/ontology/voc/PublicationCitation#";;
	
	public String getPreferredPrefix(String namespaceURI, String suggestion, boolean requirePrefix) {
		String result = suggestion;
		
		/**
		 * Returning empty namespace prefix works if a SAXResult is used, but not
		 * a DOMResult - see http://forums.java.net/jive/thread.jspa?messageID=217155
		 * This has not been resolved yet, as far as I can tell
		 */
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.RDF_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.RDF_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.DC_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.DC_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.DCTERMS_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.DCTERMS_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.OWL_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.OWL_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.TAXONNAME_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.TAXONNAME_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.TEAM_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.TEAM_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.TAXONCONCEPT_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.TAXONCONCEPT_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.COMMON_NAMESPACE)){
			return TDWGNamespacePrefixMapper.COMMON_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.PERSON_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.PERSON_PREFIX;
		}
		
		if(namespaceURI.equals(TDWGNamespacePrefixMapper.PUBLICATIONCITATION_NAMESPACE)) {
			return TDWGNamespacePrefixMapper.PUBLICATIONCITATION_PREFIX;
		}
		
		return result;
	}

}
