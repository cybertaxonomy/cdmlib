/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Interface for validation level 3. Validation level 3 indicates an optional
 * complex validation that usually involves multiple {@linkplain CdmBase CDM
 * objects}. In contrary to level 1 validations level 2 and level 3 validations
 * are optional, so they do have more the status of a warning rather then
 * indicating that some data is inconsistent to the CDM.
 *
 * @author b.clark
 *
 */
public interface Level3 {

}
