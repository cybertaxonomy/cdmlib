/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Payload;

/**
 * A class conveying the severity of a {@link ConstraintViolation}. Severity
 * levels are extraneous to the javax.validation framework and can only be
 * conveyed using generic {@link Payload} objects. Unfortunately, only the class
 * of those objects is communicated back to the client. The class <i>is</i> the
 * message. Concrete instances or {@code enum} s (an obvious choice for severity
 * levels) cannot function as {@code Payload} objects. The Severity class
 * enables you to program using true Severity instances (one for each level),
 * while behind the scenes only the class of those instances is taken into
 * account.
 *
 * @author ayco_holleman
 */
public abstract class Severity implements Payload {

    public static final Notice NOTICE = new Notice();
    public static final Warning WARNING = new Warning();
    public static final Error ERROR = new Error();

    public static final class Notice extends Severity {
    }

    public static final class Warning extends Severity {
    }

    public static final class Error extends Severity {
    }

    /**
     * Get {@code Severity} object for the specified {@code String}
     * representation. Does the opposite of {@link #toString()}.
     *
     * @param name
     *            The {@code String} representation of {@code Severity} object
     *            you want.
     *
     * @return The {@code Severity} object
     */
    public static Severity forName(String name) {
        if (name.equals(Error.class.getSimpleName())) {
            return ERROR;
        }
        if (name.equals(Warning.class.getSimpleName())) {
            return WARNING;
        }
        return NOTICE;
    }

    /**
     * Get the {@code Severity} of the specified {@code ConstraintViolation}.
     *
     * @param error
     *            The {@code ConstraintViolation}
     *
     * @return The {@code Severity}
     */
    public static Severity getSeverity(ConstraintViolation<?> error) {
        Set<Class<? extends Payload>> payloads = error.getConstraintDescriptor().getPayload();
        for (Class<? extends Payload> payload : payloads) {
            if (payload == Error.class) {
                return ERROR;
            }
            if (payload == Warning.class) {
                return WARNING;
            }
            if (payload == Notice.class) {
                return NOTICE;
            }
        }
        return null;
    }

    private Severity() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
