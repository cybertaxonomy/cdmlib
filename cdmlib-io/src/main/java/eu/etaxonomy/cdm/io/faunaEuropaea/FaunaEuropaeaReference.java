/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.util.UUID;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.babadshanjan
 * @created 13.09.2009
 * @version 1.0
 */
public class FaunaEuropaeaReference {
	
	private UUID taxonUuid;
	//private ReferenceBase cdmReference;
//	private int taxonId;
	private int referenceId;
	private String referenceAuthor;
	private String referenceYear;
	private String referenceTitle;
	private String referenceSource;
	private String page;
	
	
	/**
	 * @return the page
	 */
	public String getPage() {
		return page;
	}
	/**
	 * @param page the page to set
	 */
	public void setPage(String page) {
		this.page = page;
	}
	/**
	 * @return the taxonUuid
	 */
	public UUID getTaxonUuid() {
		return taxonUuid;
	}
	/**
	 * @param taxonUuid the taxonUuid to set
	 */
	public void setTaxonUuid(UUID taxonUuid) {
		this.taxonUuid = taxonUuid;
	}
	/**
	 * @return the referenceId
	 */
	public int getReferenceId() {
		return referenceId;
	}
	/**
	 * @param referenceId the referenceId to set
	 */
	public void setReferenceId(int referenceId) {
		this.referenceId = referenceId;
	}
	/**
	 * @return the referenceYear
	 */
	public String getReferenceYear() {
		return referenceYear;
	}
	/**
	 * @param referenceYear the referenceYear to set
	 */
	public void setReferenceYear(String referenceYear) {
		this.referenceYear = referenceYear;
	}
	/**
	 * @return the referenceTitle
	 */
	public String getReferenceTitle() {
		return referenceTitle;
	}
	/**
	 * @param referenceTitle the referenceTitle to set
	 */
	public void setReferenceTitle(String referenceTitle) {
		this.referenceTitle = referenceTitle;
	}
	/**
	 * @return the referenceSource
	 */
	public String getReferenceSource() {
		return referenceSource;
	}
	/**
	 * @param referenceSource the referenceSource to set
	 */
	public void setReferenceSource(String referenceSource) {
		this.referenceSource = referenceSource;
	}
	/**
	 * @return the referenceAuthor
	 */
	public String getReferenceAuthor() {
		return referenceAuthor;
	}
	/**
	 * @param referenceAuthor the referenceAuthor to set
	 */
	public void setReferenceAuthor(String referenceAuthor) {
		this.referenceAuthor = referenceAuthor;
	}
	/**
	 * @return the cdmReference
	 */
	/*public ReferenceBase getCdmReference() {
		return cdmReference;
	}*/
	/**
	 * @param cdmReference the cdmReference to set
	 */
	/*public void setCdmReference(ReferenceBase cdmReference) {
		this.cdmReference = cdmReference;
	}*/

}
