/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.util.EnumSet;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;

/**
 * Factory methods for {@link EntityFilter}s related to {@link Reference}.
 *
 * @author muellera
 * @since 19.05.2026
 */
public class ReferenceFilters {

    public static EntityFilter<Reference> isNotOfType(EnumSet<ReferenceType> types) {
        return (root, cb) -> types == null ? null :  cb.not(root.get("type").in(types));
    }

    public static EntityFilter<Reference> isBeforeDatePublished(DateTime date) {
        return (root, cb) -> date == null ? null : cb.or(
                cb.lessThan(root.get("datePublished.start"), date),
                cb.and(cb.isNull(root.get("datePublished.start")),
                   cb.lessThan(root.get("datePublished.end"), date))
                );
    }

    //required by phycobank
    public static EntityFilter<Reference> isPublishedUnitOrSectionOfPubishedUnit(NamedSourceBase publishedUnit){
        return (root, cb) -> {
            if (publishedUnit == null || publishedUnit.getCitation() == null) {
                return null;
            }
            return cb.or(
                    cb.and(cb.equal(root.get("inReference").get("citation"), publishedUnit.getCitation()),
                            cb.equal(root.get("type"), ReferenceType.Section)),
                    cb.equal(root.get("id"), publishedUnit.getCitation().getId()));
        };
    }

    public static EntityFilter<Reference> isPublishedUnitOrSectionOfPubishedUnit(Reference publishedUnit){
        return (root, cb) -> {
            if (publishedUnit == null) {
                return null;
            }
            return cb.or(
                    cb.and(cb.equal(root.get("inReference"), publishedUnit.getCitation()),
                            cb.equal(root.get("type"), ReferenceType.Section)),
                    cb.equal(root.get("id"), publishedUnit.getId()));
        };
    }


}
