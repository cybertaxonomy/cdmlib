/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.List;
import java.util.UUID;

/**
 * @author a.mueller
 * @date 23.05.2011
 *
 */
public class MatchingTaxonConfigurator {

	public static MatchingTaxonConfigurator NewInstance(){
		return new MatchingTaxonConfigurator();
	}

	private String taxonNameTitle;

	private UUID secUuid;

	private UUID classificationUuid;

	private boolean includeSynonyms = true;

	private boolean onlyMatchingSecUuid = false;

	private boolean onlyMatchingClassificationUuid = false;

	private List<String> propertyPath = null;

	/**
	 * @return the taxonName
	 */
	public String getTaxonNameTitle() {
		return taxonNameTitle;
	}

	/**
	 * @param taxonName the taxonName to set
	 */
	public void setTaxonNameTitle(String taxonNameTitle) {
		this.taxonNameTitle = taxonNameTitle;
	}

	/**
	 * @return the secUuid
	 */
	public UUID getSecUuid() {
		return secUuid;
	}

	/**
	 * @param secUuid the secUuid to set
	 */
	public void setSecUuid(UUID secUuid) {
		this.secUuid = secUuid;
	}

	/**
	 * @return the classificationUuid
	 */
	public UUID getClassificationUuid() {
		return classificationUuid;
	}

	/**
	 * @param classificationUuid the classificationUuid to set
	 */
	public void setClassificationUuid(UUID classificationUuid) {
		this.classificationUuid = classificationUuid;
	}

	/**
	 * @return the includeSynonyms
	 */
	public boolean isIncludeSynonyms() {
		return includeSynonyms;
	}

	/**
	 * Default is <code>true</code>.
	 * @param includeSynonyms the includeSynonyms to set
	 */
	public void setIncludeSynonyms(boolean includeSynonyms) {
		this.includeSynonyms = includeSynonyms;
	}

	/**
	 * @return the onlyMatchingSecUuid
	 */
	public boolean isOnlyMatchingSecUuid() {
		return onlyMatchingSecUuid;
	}

	/**
	 * @param onlyMatchingSecUuid the onlyMatchingSecUuid to set
	 */
	public void setOnlyMatchingSecUuid(boolean onlyMatchingSecUuid) {
		this.onlyMatchingSecUuid = onlyMatchingSecUuid;
	}

	/**
	 * @return the onlyMatchingClassificationUuid
	 */
	public boolean isOnlyMatchingClassificationUuid() {
		return onlyMatchingClassificationUuid;
	}

	/**
	 * @param onlyMatchingClassificationUuid the onlyMatchingClassificationUuid to set
	 */
	public void setOnlyMatchingClassificationUuid(
			boolean onlyMatchingClassificationUuid) {
		this.onlyMatchingClassificationUuid = onlyMatchingClassificationUuid;
	}

    /**
     * @return the propertyPath
     */
    public List<String> getPropertyPath() {
        return propertyPath;
    }

    /**
     * @param propertyPath the propertyPath to set
     */
    public void setPropertyPath(List<String> propertyPath) {
        this.propertyPath = propertyPath;
    }


}
