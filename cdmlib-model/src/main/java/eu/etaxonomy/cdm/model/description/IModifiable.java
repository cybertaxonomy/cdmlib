/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.Set;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IVersionableEntity;

/**
 * @author n.hoffmann
 * @since Sep 15, 2010
 */
public interface IModifiable extends IVersionableEntity {

	/** 
	 * Returns the set of {@link Modifier modifiers} used to qualify the validity
	 * of <i>this</i> state data. This is only metainformation.
	 */
	public Set<DefinedTerm> getModifiers();
	
	/**
	 * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> state data.
	 * 
	 * @param modifier	the modifier to be added to <i>this</i> state data
	 * @see    	   		#getModifiers()
	 */
	public void addModifier(DefinedTerm modifier);
	/** 
	 * Removes one element from the set of {@link #getModifiers() modifiers}
	 * used to qualify the validity of <i>this</i> state data.
	 *
	 * @param  modifier	the modifier which should be removed
	 * @see     		#getModifiers()
	 * @see     		#addModifier(Modifier)
	 */
	public void removeModifier(DefinedTerm modifier);
}
