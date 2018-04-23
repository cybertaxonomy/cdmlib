/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

/**
 * @author a.mueller
 \* @since 14.03.2017
 *
 */
public abstract class SimpleImport<CONFIG extends ImportConfiguratorBase, SOURCE extends Object>
            extends CdmImportBase<CONFIG, EmptyImportState<CONFIG, SimpleImport>>{

    private static final long serialVersionUID = 8928228863002861242L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(EmptyImportState<CONFIG, SimpleImport> state) {
        this.doInvoke(state.getConfig());
        return;
    }

    protected abstract void doInvoke(CONFIG config);

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(EmptyImportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(EmptyImportState state) {
        return false;
    }

}
