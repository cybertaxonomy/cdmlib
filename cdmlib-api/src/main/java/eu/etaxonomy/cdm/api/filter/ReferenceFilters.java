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

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

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

        return (root, cb) -> {
            if (date == null) {
                return null;
            }

            // path to partial fields
            Path<Partial> startPath = root.get("datePublished").get("start");
            Path<Partial> endPath = root.get("datePublished").get("end");


            // 1. Convert date time parameter to partial to make it comparable.
            Partial dateAsPartial = new Partial()
                .with(DateTimeFieldType.year(), date.getYear())
                .with(DateTimeFieldType.monthOfYear(), date.getMonthOfYear())
                .with(DateTimeFieldType.dayOfMonth(), date.getDayOfMonth());

            //2. Define predicates for valid years
            Predicate startHasValidYear = cb.not(cb.like(startPath.as(String.class), "0000%"));
            Predicate endHasValidYear = cb.not(cb.like(endPath.as(String.class), "0000%"));
            //TODO also define valid months (but is not critical as no data exists)

            // 3. Compare logic is handled via Partial object
            // Hibernate-UserType translates cb.lessThan() into database comparison
            Predicate startCompare = cb.and(
                cb.isNotNull(startPath),
                startHasValidYear,
                cb.lessThan(startPath, dateAsPartial)
            );

            Predicate endCompare = cb.and(
                cb.isNotNull(endPath),
                endHasValidYear,
                cb.lessThan(endPath, dateAsPartial)
            );

            //4. fallback if start does not exist but end exists
            return cb.or(
                startCompare,
                cb.and(
                    cb.isNull(startPath),
                    endCompare
                )
            );
        };
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