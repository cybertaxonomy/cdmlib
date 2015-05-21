/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.Validator;

/**
 * A subclass of {@code Thread} specialized in running validation tasks. Each
 * {@code ValidationThread} has its own {@link Validator} instance. In addition
 * it allows a flag to be set (by the main thread) that the currently running
 * {@link ConstraintValidator} may query to see if there is a termination
 * request. See {@link ValidationExecutor} for the rationale behind this.
 *
 * @see {@link #isTerminationRequested()}.
 *
 * @author ayco_holleman
 *
 */
public final class EntityValidationThread extends Thread {

    private final Validator validator;

    private boolean terminationRequested;
    private EntityValidationTaskBase currentTask;

    EntityValidationThread(ThreadGroup group, Runnable runnable, String name, Validator validator) {
        super(group, runnable, name);
        this.validator = validator;
        setPriority(MIN_PRIORITY);
    }

    /**
     * Flag indicating that the {@link ConstraintValidator} currently running in
     * this {@code ValidationThread} is requested to terminate itself.
     * Constraint validators can check whether to abort the validation like so:<br>
     * <code>
     * if(Thread.currentThread() instanceof ValidationThread) {
     * 	ValidationThread vt = (ValidationThread) Thread.currentThread();
     * 	if(vt.isTerminationRequested()) {
     * 		// Stop with what I am doing
     * 	}
     * }
     * </code>
     *
     * @return Whether or not the currently running {@link ConstraintValidator}
     *         is requested to terminate itself
     */
    public boolean isTerminationRequested() {
        return terminationRequested;
    }

    void setTerminationRequested(boolean b) {
        this.terminationRequested = b;
    }

    Validator getValidator() {
        return validator;
    }

    EntityValidationTaskBase getCurrentTask() {
        return currentTask;
    }

    void setCurrentTask(EntityValidationTaskBase currentTask) {
        this.currentTask = currentTask;
    }

}
