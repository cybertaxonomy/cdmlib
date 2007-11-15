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
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:54
 */
@Entity
public class StatisticalMeasurementValue extends VersionableEntity {
	static Logger logger = Logger.getLogger(StatisticalMeasurementValue.class);
	private float value;
	private Set<Modifier> modifiers;
	private StatisticalMeasure type;
	public StatisticalMeasure getType(){
		return this.type;
	}
	public void setType(StatisticalMeasure type){
		this.type = type;
	}


	public float getValue(){
		return this.value;
	}
	public void setValue(float value){
		this.value = value;
	}
	public Set<Modifier> getModifiers() {
		return modifiers;
	}
	private void setModifiers(Set<Modifier> modifiers) {
		this.modifiers = modifiers;
	}
	public void addModifier(Modifier modifier) {
		this.modifiers.add(modifier);
	}
	public void removeModifier(Modifier modifier) {
		this.modifiers.remove(modifier);
	}

}