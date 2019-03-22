/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.term;

/**
 * @author a.mueller
 * @since 22.01.2019
 *
 */
public interface IHasTermType {

    public TermType getTermType();

    /**
     * Checks if term type of term1 is not <code>null</code>.
     * @param term any instance implementing {@link IHasTermType}
     * @throws IllegalStateException if term typeis <code>null</code>
     */
    public static void checkTermTypeNull(IHasTermType term) {
        if (term.getTermType()== null){
            throw new IllegalStateException("Term types must not be null");
        }
    }
    /**
     * Checks if term types of term1 and term2 are equal
     * and both term types are not <code>null</code>.
     * If term1 or term2 is null nothing happens.
     * @param term1 any instance implementing {@link IHasTermType}
     * @param term2 any instance implementing {@link IHasTermType}
     * @throws IllegalStateException if term types are either null or not equal
     */
    public static void checkTermTypes(IHasTermType term1, IHasTermType term2) {
        if (term1 != null && term2 != null){
            checkTermTypeNull(term1);
            checkTermTypeNull(term2);
            if (term1.getTermType()!= term2.getTermType()){
                throw new IllegalStateException("Term types must match");
            }
        }
    }

    /**
     * Checks if term types of term1 and term2 are either equal
     * term type of descendant is kind of term type of ancestor.
     * Also both term types must not be null <code>null</code> and
     * If ancestor or descendant is null nothing happens.
     * @param ancestor
     * @param descendant
     * @throws IllegalStateException if any of the checks are not successful
     * @see #checkTermTypes(IHasTermType, IHasTermType)
     */
    public static void checkTermTypeEqualOrDescendant(IHasTermType ancestor, IHasTermType descendant){
        checkTermTypeNull(ancestor);
        checkTermTypeNull(descendant);
        if (ancestor.getTermType()!= descendant.getTermType() && !descendant.getTermType().isKindOf(ancestor.getTermType())){
            throw new IllegalStateException("Term types must match ");
        }
    }
}
