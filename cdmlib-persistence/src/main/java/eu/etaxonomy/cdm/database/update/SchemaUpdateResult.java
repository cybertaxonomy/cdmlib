/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import eu.etaxonomy.cdm.common.IoResultBase;

/**
 * @author a.mueller
 * @date 04.06.2017
 *
 */
public class SchemaUpdateResult extends IoResultBase{

    private static final long serialVersionUID = -1154009912578583419L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setExceptionState() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAborted() {
        // TODO Auto-generated method stub
    }

    /**
     * @return
     */
    public boolean isSuccess() {
        return this.getExceptions().isEmpty() && this.getErrors().isEmpty();
    }

    /**
     * @param e
     * @param message
     * @param classificationRootNodeUpdater
     * @param methodName
     */
    public void addException(Exception e, String message, SchemaUpdaterStepBase step,
            String methodName) {
        addException(e, message, step.getStepName() + ", " +  step.getClass().getName() + "." + methodName);
    }

    /**
     * @param message
     * @param singleTermUpdater
     * @param string
     */
    public void addError(String message, SchemaUpdaterStepBase step, String string) {
        addError(message, step.getStepName() + ", " +  step.getClass().getName() +"." + string);
    }

    /**
     * @param message
     * @param singleTermUpdater
     * @param string
     */
    public void addWarning(String message, SchemaUpdaterStepBase step, String string) {
        addWarning(message, step.getStepName() + ", " +  step.getClass().getName() +"." + string);
    }

    @Override
    public StringBuffer createReport() {
        boolean isSuccess = isSuccess();
        String strSuccess = "Schema-Update ended " +  (isSuccess ? "successful" : "with errors");
        StringBuffer superReport = super.createReport();
        superReport.insert(0, strSuccess + "\n");
        return superReport;
    }

}
