/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

/**
 * Interface representing dynamic {@link Reference references}.
 * {@link OriginalSourceBase sources} based on these references should
 * support {@link OriginalSourceBase#accessed}.
 *
 * Note: Originally the dynymic references supported #accessed themselves.
 *       This was removed in #10145 (in connection with #10057).
 *
 * @author a.mueller
 * @date 26.07.2022
 */
public interface IDynamicReference {

}
