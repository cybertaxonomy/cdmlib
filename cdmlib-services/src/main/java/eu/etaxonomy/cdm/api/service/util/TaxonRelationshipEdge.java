/**
 * Copyright (C) 2012 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.api.service.util;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;


/**
 * Holds a ({@link TaxonRelationshipType})
 * and gives it a direction which is one of:
 * <ul>
 * <li>direct, everted: {@link Direction.relatedTo}</li>
 * <li>inverse: {@link Direction.relatedFrom}</li>
 * <li>bidirectional: {@link Direction.relatedTo} and {@link Direction.relatedFrom}</li>
 * </ul>
 *
 * @author a.kohlbecker
 \* @since Dec 7, 2012
 *
 */
public class TaxonRelationshipEdge extends AbstractRelationshipEdge<TaxonRelationshipType> {

    public TaxonRelationshipEdge(Set<TaxonRelationshipType> taxonRelationshipTypes, Direction ... direction) {
        super(taxonRelationshipTypes, direction);
    }

    public TaxonRelationshipEdge(TaxonRelationshipType taxonRelationshipType, Direction ... direction) {
        super(taxonRelationshipType, direction);
    }

}
