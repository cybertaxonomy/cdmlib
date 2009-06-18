// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext;

import java.awt.Color;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.kohlbecker
 * @date 18.06.2009
 *
 */
public interface IEditGeoService {

	public String getEditGeoServiceUrlParameterString(Taxon taxon, 
			Map<PresenceAbsenceTermBase<?>,Color> presenceAbsenceTermColors, 
			int width, 
			int height, 
			String bbox, 
			String backLayer);
}
