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
 * @created 14-Jul-1913
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
	 * Returns all defined terms this term is a generalization for.
	 * Therefore the returned terms are kind of <code>this</code> term
	 */
	public Set<T> getGeneralizationOf();

}
