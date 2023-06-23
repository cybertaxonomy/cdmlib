// $Id$
/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.UUID;

import org.joda.time.DateTime;

/**
 * @author K.Luther
 * @date 06.06.2023
 *
 */
public interface ICdmBaseDto {
    public UUID getUuid();
    public int getId();
    public DateTime getCreated();
    public String getCreatedBy();
    public DateTime getUpdated();
    public String getUpdatedBy();
}
