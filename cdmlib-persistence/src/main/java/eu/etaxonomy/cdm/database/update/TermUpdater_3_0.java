// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class TermUpdater_3_0 implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdater_3_0.class);
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor){
		boolean result = true;
		if (getPreviousUpdater() != null){
			result &= getPreviousUpdater().invoke(datasource, monitor);
		}
		
		List<SingleTermUpdater> list = new ArrayList<SingleTermUpdater>();
		
		UUID uuidTerm = UUID.fromString("d901d455-4e01-45cb-b653-01a840b97eed");
		String description = "Combination Illegitimate";
		String label = "Combination Illegitimate";
		String abbrev = "comb. illeg.";
		String dtype = NomenclaturalStatusType.class.getSimpleName();
		UUID uuidVocabulary = UUID.fromString("bb28cdca-2f8a-4f11-9c21-517e9ae87f1f");
		Integer orderIndex = null;
		
		list.add( SingleTermUpdater.NewInstance(uuidTerm, description, label, abbrev, dtype, orderIndex, uuidVocabulary));
		
		
		for (SingleTermUpdater singleUpdater : list){
			try {
				result &= singleUpdater.insertNewTerm(datasource, monitor);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ITermUpdater getNextUpdater() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ITermUpdater getPreviousUpdater() {
		return null;
	}


	@Override
	public int countSteps(ICdmDataSource datasource) {
		// TODO Auto-generated method stub
		return 1;
	}
}
