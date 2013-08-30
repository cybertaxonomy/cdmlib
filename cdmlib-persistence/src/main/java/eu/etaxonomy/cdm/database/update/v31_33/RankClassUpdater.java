// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v31_33;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;

/**
 * @author a.mueller
 * @date 15.12.2013
 */
public class RankClassUpdater extends SchemaUpdaterStepBase<RankClassUpdater> implements ITermUpdaterStep{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(RankClassUpdater.class);

	private static final String stepName = "Update rank class values";
	
// **************************** STATIC METHODS ********************************/

	public static final RankClassUpdater NewInstance(){
		return new RankClassUpdater(stepName);	
	}

	protected RankClassUpdater(String stepName) {
		super(stepName);
	}

	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		
		try {
			//update representation label
			String sql;
			sql = " SELECT orderindex FROM DefinedTermBase WHERE uuid = '%s'";
			Integer genusOrderIndex = (Integer)datasource.getSingleValue(String.format(sql, Rank.uuidGenus));
			Integer infraGenericOrderIndex = (Integer)datasource.getSingleValue(String.format(sql, Rank.uuidInfragenericTaxon));
			Integer speciesOrderIndex = (Integer)datasource.getSingleValue(String.format(sql, Rank.uuidSpecies));
			
			//suprageneric
			sql = " UPDATE DefinedTermBase dtb " + 
					" SET dtb.rankClass = '%s' " + 
					" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex < %d";
			datasource.executeUpdate(String.format(sql, RankClass.Suprageneric.getKey(), genusOrderIndex));

			
			//genus
			sql = " UPDATE DefinedTermBase dtb " + 
					" SET dtb.rankClass = '%s' " + 
					" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex = %d";
			datasource.executeUpdate(String.format(sql, RankClass.Genus.getKey(), genusOrderIndex));
			
			//infrageneric
			sql = " UPDATE DefinedTermBase dtb " + 
					" SET dtb.rankClass = '%s' " + 
					" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex > %d AND dtb.orderindex <= %d";
			datasource.executeUpdate(String.format(sql, RankClass.Infrageneric.getKey(), genusOrderIndex, infraGenericOrderIndex));

			//species group
			sql = " UPDATE DefinedTermBase dtb " + 
					" SET dtb.rankClass = '%s' " + 
					" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex > %d AND dtb.orderindex < %d";
			datasource.executeUpdate(String.format(sql, RankClass.SpeciesGroup.getKey(), infraGenericOrderIndex, speciesOrderIndex));
			
			//species
			sql = " UPDATE DefinedTermBase dtb " + 
					" SET dtb.rankClass = '%s' " + 
					" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex = %d";
			datasource.executeUpdate(String.format(sql, RankClass.Species.getKey(), speciesOrderIndex));

			//infraspecific
			sql = " UPDATE DefinedTermBase dtb " + 
					" SET dtb.rankClass = '%s' " + 
					" WHERE dtb.DTYPE = 'Rank' AND dtb.orderindex > %d";
			datasource.executeUpdate(String.format(sql, RankClass.Infraspecific.getKey(), speciesOrderIndex));

			
			return 0;
		} catch (Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.warn(e.getMessage());
			return null;
		}
	}

	
}
