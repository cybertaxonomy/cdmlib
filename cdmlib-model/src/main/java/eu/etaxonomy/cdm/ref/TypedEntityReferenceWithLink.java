/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ref;

import java.net.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author muellera
 * @since 14.03.2024
 */
public class TypedEntityReferenceWithLink<T extends CdmBase> extends TypedEntityReference<T> {

    private static final long serialVersionUID = 3798818217955462916L;

    private URI link;

    public TypedEntityReferenceWithLink(Class<T> type, UUID uuid, String label, URI link) {
        super(type, uuid, label);
        this.setLink(link);
    }


    public URI getLink() {
        return link;
    }
    public void setLink(URI link) {
        this.link = link;
    }

}
