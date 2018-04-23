/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.kohlbecker
 * @since Dec 4, 2013
 *
 */
public abstract class AbstractModifiableThingBeanProcessor<T extends CdmBase> extends AbstractCdmBeanProcessor<T> implements IMultilanguageTextBeanProcessor{

    private static final List<String> IGNORE_LIST = Arrays.asList(new String[]{"modifyingText"});
    private boolean replaceMultilanguageText = false;

    @Override
    public void setReplaceMultilanguageText(boolean replace) {
        replaceMultilanguageText = replace;
    }

    @Override
    public boolean isReplaceMultilanguageText() {
        return replaceMultilanguageText;
    }

    @Override
    public List<String> getMultilanguageTextIgnoreList() {
        return IGNORE_LIST;
    }

    @Override
    public List<String> getIgnorePropNames() {
        return getMultilanguageTextIgnoreList();
    }

}
