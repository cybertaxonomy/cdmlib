/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.validation;

import javax.validation.ConstraintViolation;

/**
 *
 * @author ayco_holleman
 \* @since 15 jan. 2015
 *
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 4324647930626552152L;

    public ValidationException(String message) {
        super(message);

    }

    public ValidationException(ConstraintViolation<?> constraintViolation) {
        super(constraintViolation.getMessage());
    }
}
