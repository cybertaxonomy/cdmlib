/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.lsid.impl;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.cateproject.model.taxon.Taxon;
//import org.cateproject.model.taxon.TaxonBase;
//import org.cateproject.model.taxon.TaxonRelationship;
//import org.cateproject.service.lsid.LSIDMetadataMapper;
//import org.joda.time.format.DateTimeFormatter;
//import org.joda.time.format.ISODateTimeFormat;
//import org.tdwg.ontology.RDF;
//import org.tdwg.ontology.Relation;
//import org.tdwg.ontology.TaxonName;
//import org.tdwg.ontology.TaxonConcept.HasName;
//import org.tdwg.ontology.TaxonConcept.HasRelationship;

public class TaxonMetadataMapper {

//    implements LSIDMetadataMapper<TaxonBase> {
//	private static Log log = LogFactory.getLog(TaxonMetadataMapper.class);
//	private static DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime();
//	
//	private String host;
//	private String file;
//	private Integer port;
//	public static String PROTOCOL = "http";
//	
//	public void setHost(String host) {
//		this.host = host;
//	}
//	
//	public void setFile(String file) {
//		this.file = file;
//	}
//
//	public void setPort(String port) {
//		this.port = Integer.parseInt(port);
//	}
//	
//	public Object mapMetadata(TaxonBase input) {
//		RDF rdf = new RDF();
//		org.tdwg.ontology.TaxonConcept taxonConcept = new org.tdwg.ontology.TaxonConcept();
//		taxonConcept.setAbout(input.getLSID().getLsid());
//	
//		taxonConcept.getTitle().add(input.getTitleCache());
//		taxonConcept.getCreated().add(TaxonMetadataMapper.dateTimeFormatter.print(input.getCreated()));
//		
//		taxonConcept.getPublishedIn().add(input.getSec().getTitleCache());
//				
//		if(input.getName().getLSID() != null) {
//			TaxonName taxonName = new TaxonName();
//		    taxonName.setAbout(input.getName().getLSID().getLsid());
//		    taxonName.getNameComplete().add(input.getName().getTitleCache());
//			
//			HasName hasName = new HasName();
//			hasName.setTaxonName(taxonName);
//			taxonConcept.getHasName().add(hasName);
//		} else {
//			taxonConcept.getNameString().add(input.getName().getTitleCache());
//		}
//		
//		taxonConcept.getAccordingToString().add(input.getSec().getTitleCache());
//	    if(input instanceof Taxon) {
//	    	Taxon taxon = (Taxon)input;
//	    	for(TaxonRelationship taxonomicRelationship : taxon.getTaxonRelations()) {
//	    		HasRelationship.Relationship relationship = new HasRelationship.Relationship();
//	    		HasRelationship.Relationship.RelationshipCategory relationshipCategory = new HasRelationship.Relationship.RelationshipCategory();
//	    		relationshipCategory.setResource(taxonomicRelationship.getType().getURI());
//	    		relationship.getRelationshipCategory().add(relationshipCategory);
//	    		HasRelationship.Relationship.ToTaxon toTaxon = new HasRelationship.Relationship.ToTaxon();
//	    		toTaxon.setResource(taxonomicRelationship.getRelatedTo().getLSID().getLsid());
//	    		relationship.setToTaxon(toTaxon);
//	    		HasRelationship hasRelationship = new HasRelationship();
//	    		hasRelationship.setRelationship(relationship);
//
//	    		taxonConcept.getHasRelationship().add(hasRelationship);
//	    	}
//		}
//		
//		try {
//			Relation relation = new Relation();
//			URL url = new URL(TaxonMetadataMapper.PROTOCOL, host, port, file + input.getUuid().toString());
//			relation.setRdfResource(url.toExternalForm());
//			taxonConcept.getRelation().add(relation);
//		} catch (MalformedURLException e) {
//			log.error(e);
//		}
//
//		rdf.setTaxonConcept(taxonConcept);
//		
//		return rdf;
//	}
}
