/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

/**
 * Common interface for all {@link AbstractCdmBeanProcessor}s for CDM types which
 * have properties of MultilanguageText or LanguageString sets.
 * <p>
 * Implementors of this interface should to define in a private field the variable to be returned by the
 * {@link #getMultilanguageTextIgnoreList()} and by the {@link #isReplaceMultilanguageText()}.
 * The properties to be ignored should be initially skipped. This is done by returning the IgnoreList via the
 * {@link AbstractCdmBeanProcessor#getIgnorePropNames()}. The property can then be added again during the
 * {@link AbstractCdmBeanProcessor#processBeanSecondStep(Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)}
 * where the return value of {@link #isReplaceMultilanguageText()} is evaluated.
 * <p>
 * For reference implementations, please see {@link StateDataBeanProcessor} or {@link DescriptionElementBeanProcessor}.
 *
 * @author a.kohlbecker
 * @since Dec 4, 2013
 *
 */
public interface IMultilanguageTextBeanProcessor {

    public abstract void setReplaceMultilanguageText(boolean replace);

    public abstract boolean isReplaceMultilanguageText();

    public abstract List<String> getMultilanguageTextIgnoreList();

}
