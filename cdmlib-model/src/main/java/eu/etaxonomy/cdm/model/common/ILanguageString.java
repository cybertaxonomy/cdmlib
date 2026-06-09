/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import javax.persistence.Transient;

/**
 * @author muellera
 * @since 24.04.2026
 */
public interface ILanguageString {

    public Language getLanguage();
    public void setLanguage(Language language);

    public String getText();
    public void setText(String text);

    @Transient
    public String getLanguageLabel();
    public String getLanguageLabel(Language lang);
}
