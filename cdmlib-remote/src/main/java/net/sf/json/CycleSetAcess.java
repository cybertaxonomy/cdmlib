/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package net.sf.json;


/**
 * Base class for JSON Beanprocessors.
 *
 * This class must be located in the net.sf.json since it
 * needs access to the protected static methods in {@link JSONObject}
 *
 * @author a.kohlbecker
 * @date 19.03.2009
 *
 */
public class CycleSetAcess{

	public void removeFromCycleSet(Object instance) {
		JSONObject.removeInstance(instance);
	}

	public void addToCycleSet(Object instance) {
		JSONObject.addInstance(instance);
	}

}
