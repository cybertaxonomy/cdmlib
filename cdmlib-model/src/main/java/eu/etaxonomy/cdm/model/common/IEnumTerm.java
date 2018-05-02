/**
 *
 */
package eu.etaxonomy.cdm.model.common;


/**
 * Interface to combine {@link ISimpleTerm} and {@link IKeyTerm} properties.
 * To be used by enumerated terms.
 *
 * @author a.mueller
 * @since 15-Jul-2013
 *
 */
public interface IEnumTerm<T extends IEnumTerm<T>>
        extends ISimpleTerm<T>, IKeyTerm {

}
