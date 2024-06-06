/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.tmp;

import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.LabeledEntityDto;

/**
 * @author muellera
 * @since 29.02.2024
 */
public class TermTreeDto extends LabeledEntityDto {

    private TermNodeDto root;

    public TermTreeDto(UUID uuid) {
        super(uuid, null, null);
    }


    public TermTreeDto(UUID uuid, Integer id, String label) {
        super(uuid, id, label);
    }

    public TermTreeDto(UUID uuid, Integer id, String label, boolean createRoot) {
        super(uuid, id, label);
        if (createRoot) {
            this.setRoot(new TermNodeDto(null, null, "root", null));
        }
    }


    public TermNodeDto getRoot() {
        return root;
    }
    public void setRoot(TermNodeDto root) {
        this.root = root;
    }
}