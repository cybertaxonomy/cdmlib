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
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Annotation;

/**
 * @author a.mueller
 * @date 20.04.2011
 *
 */
public class DwcaResourceRelationRecord extends DwcaRecordBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaResourceRelationRecord.class);
	
	private Integer coreid;
	private String resourceRelationshipId;
	private String relatedResourceId;
	private String relationshipOfResource;
	private String relationshipAccordingTo;
	private String relatioshipEstablishedDate;
	private Set<Annotation> relationshipRemarks;
	private String scientificName;
	
	
	public void write(PrintWriter writer) {
		print(coreid, writer, IS_FIRST);
		print(resourceRelationshipId, writer, IS_NOT_FIRST);
		print(relatedResourceId, writer, IS_NOT_FIRST);
		print(relationshipOfResource, writer, IS_NOT_FIRST);
		print(relationshipAccordingTo, writer, IS_NOT_FIRST);
		print(relatioshipEstablishedDate, writer, IS_NOT_FIRST);
		printNotes(relationshipRemarks, writer, IS_NOT_FIRST);
		print(scientificName, writer, IS_NOT_FIRST);
		writer.println();
	}

	public Integer getCoreid() {
		return coreid;
	}

	public void setCoreid(Integer coreid) {
		this.coreid = coreid;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getResourceRelationshipId() {
		return resourceRelationshipId;
	}

	public void setResourceRelationshipId(String resourceRelationshipId) {
		this.resourceRelationshipId = resourceRelationshipId;
	}

	public String getRelatedResourceId() {
		return relatedResourceId;
	}

	public void setRelatedResourceId(String relatedResourceId) {
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
		return relatioshipEstablishedDate;
	}

	public void setRelatioshipEstablishedDate(String relatioshipEstablishedDate) {
		this.relatioshipEstablishedDate = relatioshipEstablishedDate;
	}

	public Set<Annotation> getRelationshipRemarks() {
		return relationshipRemarks;
	}

	public void setRelationshipRemarks(Set<Annotation> set) {
		this.relationshipRemarks = set;
	}

}
