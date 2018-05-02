/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import java.util.Set;
import java.util.UUID;

import javax.persistence.Transient;

/**
 * The common interface which is implemented by {@link DefinedTermBase defined terms} and enumerations
 * replacing fully {@link DefinedTermBase defined terms}.
 * 
 * @author a.mueller
 * @since 14-Jul-1913
 */
public interface ISimpleTerm<T extends ISimpleTerm> {

	@Transient
	public UUID getUuid();
	
	//is static !!
//	public T getByUuid(UUID uuid);


	/**
	 * Returns the defined term this term is a kind of.
	 * Therefore the returned term is a generalization of <code>this</code> term
	 */
	public T getKindOf();
	

	/**
	 * Returns all defined terms this term is a direct generalization for.
	 * Therefore the returned terms are kind of <code>this</code> term.
	 */
	public Set<T> getGeneralizationOf();
	

	/**
	 * Computes if <code>this</code> term is kind of the <code>ancestor</code> term.
	 * So the <code>ancestor</code> term is direct or indirect (recursive) generalization
	 * of <code>this</code> term.
	 * @param ancestor the potential ancestor term
	 * @see #getKindOf()
	 * @see #getGeneralizationOf()
	 */
	public boolean isKindOf(T ancestor);
	
	/**
	 * Returns all defined terms this term is a generalization for.
	 * Therefore the returned terms are kind of <code>this</code> term.
	 * If parameter <code>recursive</code> is <code>false</code> only the
	 * direct descendants will be returned. If it is <code>true</code>
	 * the direct descendants and there recursive descendants (all descendants)
	 * will be returned. 
	 */
	public Set<T> getGeneralizationOf(boolean recursive);

}
