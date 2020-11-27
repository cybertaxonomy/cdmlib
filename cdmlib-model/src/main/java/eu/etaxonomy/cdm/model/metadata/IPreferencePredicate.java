/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 12.11.2018
 */
public interface IPreferencePredicate<T extends Object> {

    public String getKey();

    public T getDefaultValue();

    public String getLabel();

    public String getLabel(Language language);

    public UUID getUuid();


    //until now we don't think Predicates will be hierarchical therefore we don't use these methods from IEnumTerm
    //maybe it will come in future
    //
    //    IPreferencePredicate getKindOf();
    //
    //    Set<PreferencePredicate> getGeneralizationOf();
    //
    //    boolean isKindOf(IPreferencePredicate ancestor);
    //
    //    Set<PreferencePredicate> getGeneralizationOf(boolean recursive);

}