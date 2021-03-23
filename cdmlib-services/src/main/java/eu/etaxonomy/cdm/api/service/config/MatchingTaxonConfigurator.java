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
 * @since 23.05.2011
 *
 */
public class MatchingTaxonConfigurator {

	public static MatchingTaxonConfigurator NewInstance(){
		return new MatchingTaxonConfigurator();
	}

    private boolean includeUnpublished = false;

	private String taxonNameTitle;

	private UUID secUuid;

	private UUID classificationUuid;

	private boolean includeSynonyms = true;

	private boolean onlyMatchingSecUuid = false;

	private boolean onlyMatchingClassificationUuid = false;

	private List<String> propertyPath = null;


	public String getTaxonNameTitle() {
		return taxonNameTitle;
	}
	public void setTaxonNameTitle(String taxonNameTitle) {
		this.taxonNameTitle = taxonNameTitle;
	}


	public UUID getSecUuid() {
		return secUuid;
	}
	public void setSecUuid(UUID secUuid) {
		this.secUuid = secUuid;
	}


	public UUID getClassificationUuid() {
		return classificationUuid;
	}
	public void setClassificationUuid(UUID classificationUuid) {
		this.classificationUuid = classificationUuid;
	}

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

	public boolean isOnlyMatchingSecUuid() {
		return onlyMatchingSecUuid;
	}
	public void setOnlyMatchingSecUuid(boolean onlyMatchingSecUuid) {
		this.onlyMatchingSecUuid = onlyMatchingSecUuid;
	}

	public boolean isOnlyMatchingClassificationUuid() {
		return onlyMatchingClassificationUuid;
	}
	public void setOnlyMatchingClassificationUuid(
			boolean onlyMatchingClassificationUuid) {
		this.onlyMatchingClassificationUuid = onlyMatchingClassificationUuid;
	}

    public List<String> getPropertyPath() {
        return propertyPath;
    }
    public void setPropertyPath(List<String> propertyPath) {
        this.propertyPath = propertyPath;
    }

    public boolean isIncludeUnpublished() {
        return includeUnpublished;
    }
    public void setIncludeUnpublished(boolean includeUnpublished) {
        this.includeUnpublished = includeUnpublished;
    }
}