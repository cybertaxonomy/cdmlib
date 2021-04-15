/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.util;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;

/**
 * Holds a RelationshipType ({@link RelationshipTermBase}) of type {@code <T>}
 * and gives it a direction which is one of:
 * <ul>
 * <li>direct, everted: {@link Direction.relatedTo}</li>
 * <li>inverse: {@link Direction.relatedFrom}</li>
 * <li>bidirectional: {@link Direction.relatedTo} and {@link Direction.relatedFrom}</li>
 * </ul>
 *
 * @author a.kohlbecker
 * @since Dec 7, 2012
 *
 * @param <T> a sub class of ({@link RelationshipTermBase})
 */
public class AbstractRelationshipEdge<T extends RelationshipTermBase> {

    private Set<T> relationshipTypes;
    private EnumSet<Direction> directions;

    public AbstractRelationshipEdge(T relationshipType, Direction ... direction) {
        super();
        this.relationshipTypes = new HashSet<>();
        this.relationshipTypes.add(relationshipType);
        directions = EnumSet.copyOf(Arrays.asList(direction));
    }

    public AbstractRelationshipEdge(Set<T> relationshipTypes, Direction ... direction) {
        super();
        this.relationshipTypes = relationshipTypes;
        directions = EnumSet.copyOf(Arrays.asList(direction));
    }

    public Set<T> getRelationshipTypes() {
        return relationshipTypes;
    }

    public void setRelationshipTypes(Set<T> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }

    public EnumSet<Direction> getDirections() {
        return directions;
    }

    public void setDirections(EnumSet<Direction> directions) {
        this.directions = directions;
    }

    /**
     * set the <code>directions</code> to {@link Direction.relatedTo}
     */
    public void setIsEvers() {
        directions = EnumSet.of(Direction.relatedTo);
    }

    /**
     * set the <code>directions</code> to {@link Direction.relatedFrom}
     */
    public void setIsInvers() {
        directions = EnumSet.of(Direction.relatedFrom);
    }

    /**
     * set the <code>directions</code> to both {@link Direction.relatedTo} and
     * {@link Direction.relatedFrom}
     */
    public void setIsBidirectional() {
        directions = EnumSet.allOf(Direction.class);
    }

    /**
     * @return <code>true</code> if the <code>directions</code> is set to
     *         {@link Direction.relatedTo}
     */
    public boolean isEvers() {
        return directions.equals(EnumSet.of(Direction.relatedTo));
    }

    /**
     * @return <code>true</code> if the <code>directions</code> is set to
     *         {@link Direction.relatedFrom}
     */
    public boolean isInvers() {
        return directions.equals(EnumSet.of(Direction.relatedFrom));
    }

    /**
     * @return <code>true</code> if the <code>directions</code> is set to both
     *         {@link Direction.relatedTo} and {@link Direction.relatedFrom}
     */
    public boolean isBidirectional() {
        return directions.equals(EnumSet.allOf(Direction.class));
    }
}
