/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

/**
 * @author muellera
 * @since 06.03.2025
 */
public interface IHasEditor {

    /**
     * If true, the {@link #getAuthorship() author} is the editor
     * and should be formatted accordingly. See #7987, #10710
     */
    public boolean isAuthorIsEditor();
    public void setAuthorIsEditor(boolean authorIsEditor);
}
