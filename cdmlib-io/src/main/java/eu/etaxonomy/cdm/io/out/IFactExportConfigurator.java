/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.out;

/**
 * Interface for export configurators exporting (beside others).
 *
 * @author muellera
 * @since 19.01.2024
 */
public interface IFactExportConfigurator {

    public boolean isDoFactualData();
    public void setDoFactualData(boolean doFactualData);

    public boolean isIncludeUnpublishedFacts();
    public void setIncludeUnpublishedFacts(boolean includeUnpublishedFacts);
}
