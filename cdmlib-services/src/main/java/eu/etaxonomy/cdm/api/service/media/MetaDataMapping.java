// $Id$
/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import eu.etaxonomy.cdm.model.metadata.IKeyLabel;

/**
 * @author kluther
 * @date 10.02.2023
 *
 */
public enum MetaDataMapping implements IKeyLabel{
    //if Credit and Artist is filled the values would be added comma separated, we need to decide which should be used.
    //Credit("Credit ", "Photographer"),
    Taxon("Title", "Taxon"),
    Headline("Headline","Taxon"),
    Sublocation("Sublocation", "Locality"),
    OriginialTransmissionReference("Original Transmission, Reference", "Original Transmission Reference"),
    Artist("Artist", "Photographer")
    ;



    private String label;
    private String key;

    private MetaDataMapping(String key, String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getKey() {
        return key;
    }





}
