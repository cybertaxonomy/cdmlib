/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.validation;

/**
 * The CRUD event that triggered a validation. When an entity violates some
 * constraint, it might be helpful to report back to the user what type of CRUD
 * event caused the violation. Note that validation may not not have been
 * triggered by any CRUD event at all, e.g. during some batch-like validation
 * process. Level-2 validation can never be triggered by a DELETE event, because
 * Level-2 validation only validates the entity itself. However, a DELETE event
 * <i>can</i> possibly trigger a Level-3 validation, because that disrupts the
 * object graph the entity was part of.
 *
 * @author ayco_holleman
 *
 */
public enum CRUDEventType {
    NONE, INSERT, UPDATE, DELETE
}
