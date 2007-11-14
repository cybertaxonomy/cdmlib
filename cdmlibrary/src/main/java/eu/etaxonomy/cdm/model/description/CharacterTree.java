/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Character trees arrange concepts and characters. They may also be used to
 * define flat char. subsets for filtering purposes.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@Entity
public class CharacterTree extends VersionableEntity {
	static Logger logger = Logger.getLogger(CharacterTree.class);
	private Set characters;

	public Set getCharacters(){
		return this.characters;
	}

	/**
	 * 
	 * @param characters    characters
	 */
	public void setCharacters(Set characters){
		this.characters = characters;
	}

}