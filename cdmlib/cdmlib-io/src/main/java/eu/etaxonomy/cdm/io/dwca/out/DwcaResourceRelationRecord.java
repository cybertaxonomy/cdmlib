// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.common.Annotation;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaResourceRelationRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaResourceRelationRecord.class);
	
	private DwcaId resourceRelationshipId;
	private UUID relatedResourceId;
	private String relationshipOfResource;
	private String relationshipAccordingTo;
	private String relationshipEstablishedDate;
	private Set<Annotation> relationshipRemarks;
	private String scientificName;
	
	
	public DwcaResourceRelationRecord(DwcaMetaDataRecord metaDataRecord, DwcaTaxExportConfigurator config){
		super(metaDataRecord, config);
		resourceRelationshipId = new DwcaId(config);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.dwca.out.DwcaRecordBase#registerKnownFields()
	 */
	protected void registerKnownFields(){
		try {
			addKnownField("resourceRelationshipID", "http://rs.tdwg.org/dwc/terms/resourceRelationshipID");
			addKnownField("relatedResourceID", "http://rs.tdwg.org/dwc/terms/relatedResourceID");
			addKnownField("relationshipOfResource", "http://rs.tdwg.org/dwc/terms/relationshipOfResource");
			addKnownField("relationshipAccordingTo", "http://rs.tdwg.org/dwc/terms/relationshipAccordingTo");
			addKnownField("relationshipEstablishedDate", "http://rs.tdwg.org/dwc/terms/relationshipEstablishedDate");
			addKnownField("relationshipRemarks", "http://rs.tdwg.org/dwc/terms/relationshipRemarks");
			addKnownField("scientificName", "http://rs.tdwg.org/dwc/terms/scientificName");

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
//	@Override
//	public List<String> getHeaderList() {
//		String[] result = new String[]{"coreid", 
//				"resourceRelationshipId",
//				"relatedResourceId",
//				"relationshipOfResource", 
//				"relationshipAccordingTo", 
//				"relationshipEstablishedDate", 
//				"relationshipRemarks",
//				"scientificName"
//		};
//		return Arrays.asList(result);
//	}
	
	public void write(PrintWriter writer) {
		printId(getUuid(), writer, IS_FIRST, "coreid");
		print(getResourceRelationshipId(), writer, IS_NOT_FIRST, TermUri.DWC_RESOURCE_RELATIONSHIP_ID);
		print(relatedResourceId, writer, IS_NOT_FIRST, TermUri.DWC_RELATED_RESOURCE_ID);
		print(relationshipOfResource, writer, IS_NOT_FIRST, TermUri.DWC_RELATIONSHIP_OF_RESOURCE);
		print(relationshipAccordingTo, writer, IS_NOT_FIRST, TermUri.DWC_RELATIONSHIP_ACCORDING_TO);
		print(relationshipEstablishedDate, writer, IS_NOT_FIRST, TermUri.DWC_RELATIONSHIP_ESTABLISHED_DATE);
		printNotes(relationshipRemarks, writer, IS_NOT_FIRST, TermUri.DWC_RELATIONSHIP_REMARKS);
		print(scientificName, writer, IS_NOT_FIRST, TermUri.DWC_SCIENTIFIC_NAME);
		writer.println();
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getResourceRelationshipId() {
		return resourceRelationshipId.getId();
	}

	public void setResourceRelationshipId(Integer resourceRelationshipId) {
		this.resourceRelationshipId.setId(resourceRelationshipId);
	}
	public void setResourceRelationshipId(UUID resourceRelationshipId) {
		this.resourceRelationshipId.setId(resourceRelationshipId);
	}

	public UUID getRelatedResourceId() {
		return relatedResourceId;
	}

	public void setRelatedResourceId(UUID relatedResourceId) {
		this.relatedResourceId = relatedResourceId;
	}

	public String getRelationshipOfResource() {
		return relationshipOfResource;
	}

	public void setRelationshipOfResource(String relationshipOfResource) {
		this.relationshipOfResource = relationshipOfResource;
	}

	public String getRelationshipAccordingTo() {
		return relationshipAccordingTo;
	}

	public void setRelationshipAccordingTo(String relationshipAccordingTo) {
		this.relationshipAccordingTo = relationshipAccordingTo;
	}

	public String getRelatioshipEstablishedDate() {
		return relationshipEstablishedDate;
	}

	public void setRelatioshipEstablishedDate(String relatioshipEstablishedDate) {
		this.relationshipEstablishedDate = relatioshipEstablishedDate;
	}

	public Set<Annotation> getRelationshipRemarks() {
		return relationshipRemarks;
	}

	public void setRelationshipRemarks(Set<Annotation> set) {
		this.relationshipRemarks = set;
	}

}
