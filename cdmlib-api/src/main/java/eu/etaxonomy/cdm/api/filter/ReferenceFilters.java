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
    /**
     * Reduces the returned references to the citation of this source and sections of this citation.
     */
    public static EntityFilter<Reference> publishedUnitOrSectionOfPubishedUnit(NamedSourceBase sourceOfPublishedUnit){
        return (root, cb) -> {
            if (sourceOfPublishedUnit == null || sourceOfPublishedUnit.getCitation() == null) {
                return null;
            }
            return cb.or(
                    cb.and(cb.equal(root.get("inReference"), sourceOfPublishedUnit.getCitation()),
                            cb.equal(root.get("type"), ReferenceType.Section)),
                    cb.equal(root.get("id"), sourceOfPublishedUnit.getCitation().getId()));
        };
    }

    /**
     * Reduces the returned references to the published unit and sections of the published unit.
     */
    public static EntityFilter<Reference> publishedUnitOrSectionOfPubishedUnit(Reference publishedUnit){
        return (root, cb) -> {
            if (publishedUnit == null) {
                return null;
            }
            return cb.or(
                    cb.and(cb.equal(root.get("inReference"), publishedUnit),
                            cb.equal(root.get("type"), ReferenceType.Section)),
                    cb.equal(root.get("id"), publishedUnit.getId()));
        };
    }
}