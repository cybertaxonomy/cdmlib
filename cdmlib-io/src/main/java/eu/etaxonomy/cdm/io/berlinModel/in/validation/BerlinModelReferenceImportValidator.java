// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in.validation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelReferenceImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class BerlinModelReferenceImportValidator implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceImportValidator.class);

	public boolean validate(BerlinModelImportState state, BerlinModelReferenceImport refImport){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkArticlesWithoutJournal(bmiConfig);
		result &= checkPartOfJournal(bmiConfig);
		result &= checkPartOfUnresolved(bmiConfig);
		result &= checkPartOfPartOf(bmiConfig);
		result &= checkPartOfArticle(bmiConfig);
		result &= checkJournalsWithSeries(bmiConfig);
		result &= checkObligatoryAttributes(bmiConfig, refImport);
		result &= checkPartOfWithVolume(bmiConfig);
		result &= checkArticleWithEdition(bmiConfig);
		
		if (result == false ){System.out.println("========================================================");}
		
		return result;
		
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Deprecated  //use validate(state, import) instead
	public boolean validate(BerlinModelImportState state) {
		logger.warn("BerlinModelReferenceImport uses wrong validation method");
		return false;
	}
	
	
	//******************************** CHECK *************************************************
		
		private static boolean checkArticlesWithoutJournal(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
							" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
							" WHERE (Reference.RefCategoryFk = 1) AND (InRef.RefCategoryFk <> 9) ";
				ResultSet resulSetarticlesWithoutJournal = source.getResultSet(strQueryArticlesWithoutJournal);
				boolean firstRow = true;
				while (resulSetarticlesWithoutJournal.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are Articles with wrong inRef type!");
						System.out.println("========================================================");
					}
					int refId = resulSetarticlesWithoutJournal.getInt("RefId");
					//int categoryFk = resulSetarticlesWithoutJournal.getInt("RefCategoryFk");
					String cat = resulSetarticlesWithoutJournal.getString("RefCategoryAbbrev");
					int inRefFk = resulSetarticlesWithoutJournal.getInt("InRefId");
					//int inRefCategoryFk = resulSetarticlesWithoutJournal.getInt("InRefCatFk");
					String inRefCat = resulSetarticlesWithoutJournal.getString("InRefCat");
					String refCache = resulSetarticlesWithoutJournal.getString("RefCache");
					String nomRefCache = resulSetarticlesWithoutJournal.getString("nomRefCache");
					String title = resulSetarticlesWithoutJournal.getString("title");
					String inRefTitle = resulSetarticlesWithoutJournal.getString("InRefTitle");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
							"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
							"\n  inRefTitle: " + inRefTitle );
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		private static boolean checkPartOfJournal(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
				" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
							" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 9) ";
				ResultSet rs = source.getResultSet(strQueryPartOfJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are part-of-references that have a Journal as in-reference!");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("RefId");
					//int categoryFk = rs.getInt("RefCategoryFk");
					String cat = rs.getString("RefCategoryAbbrev");
					int inRefFk = rs.getInt("InRefId");
					//int inRefCategoryFk = rs.getInt("InRefCatFk");
					String inRefCat = rs.getString("InRefCat");
					String refCache = rs.getString("RefCache");
					String nomRefCache = rs.getString("nomRefCache");
					String title = rs.getString("title");
					String inRefTitle = rs.getString("InRefTitle");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
							"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
							"\n  inRefTitle: " + inRefTitle );
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		
		private static boolean checkPartOfUnresolved(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle " + 
				" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
							" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 10) ";
				ResultSet rs = source.getResultSet(strQueryPartOfJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are part-of-references that have an 'unresolved' in-reference!");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("RefId");
					//int categoryFk = rs.getInt("RefCategoryFk");
					String cat = rs.getString("RefCategoryAbbrev");
					int inRefFk = rs.getInt("InRefId");
					//int inRefCategoryFk = rs.getInt("InRefCatFk");
					String inRefCat = rs.getString("InRefCat");
					String refCache = rs.getString("RefCache");
					String nomRefCache = rs.getString("nomRefCache");
					String title = rs.getString("title");
					String inRefTitle = rs.getString("InRefTitle");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
							"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
							"\n  inRefTitle: " + inRefTitle );
					result = firstRow = false;
				}
				if (result == false){
					System.out.println("\nChoose a specific type from the following reference types: \n" +
							"  1) Article \n  2) Book \n  3) BookSection \n  4) CdDvd \n  5) ConferenceProceeding \n  6) Database\n" + 
							"  7) Generic \n  7) InProceedings \n  8) Journal \n  9) Map \n 10) Patent \n 11) PersonalCommunication\n" +
							" 12) PrintSeries \n 13) Proceedings \n 14) Report \n 15) Thesis \n 16) WebPage");
				}
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		private static boolean checkPartOfPartOf(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle, InRef.InRefFk as InInRefId, InInRef.Title as inInRefTitle, InInRef.RefCategoryFk as inInRefCategory " + 
							" FROM Reference " +
								" INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId " + 
								" INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId " + 
								" INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
								" INNER JOIN Reference AS InInRef ON InRef.InRefFk = InInRef.RefId " + 
							" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 2) ";
				ResultSet rs = source.getResultSet(strQueryPartOfJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are part-of-references that are part of an other 'part-of' reference!\n" + 
								"         This is invalid or ambigous. Please try to determine the reference types more detailed ");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("RefId");
					//int categoryFk = rs.getInt("RefCategoryFk");
					String cat = rs.getString("RefCategoryAbbrev");
					int inRefFk = rs.getInt("InRefId");
					//int inRefCategoryFk = rs.getInt("InRefCatFk");
					String inRefCat = rs.getString("InRefCat");
					String refCache = rs.getString("RefCache");
					String nomRefCache = rs.getString("nomRefCache");
					String title = rs.getString("title");
					String inRefTitle = rs.getString("InRefTitle");
					int inInRefId = rs.getInt("InInRefId");
					String inInRefTitle = rs.getString("inInRefTitle");
					int inInRefCategory = rs.getInt("inInRefCategory");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + 
							"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
							"\n  inRefTitle: " + inRefTitle + "\n  inInRefId: " + inInRefId + "\n  inInRefTitle: " + inInRefTitle +
							"\n  inInRefCategory: " + inInRefCategory );
					result = firstRow = false;
				}
				if (result == false){
					System.out.println("\nChoose a specific type from the following reference types: \n" +
							"  1) BookSection - Book - PrintSeries \n" +
							"  2) InProceedings - pProceedings  - PrintSeries");
				}
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}

		
		private static boolean checkPartOfArticle(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryPartOfJournal = "SELECT Reference.RefId, InRef.RefId AS InRefID, Reference.RefCategoryFk, InRef.RefCategoryFk AS InRefCatFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, Reference.NomTitleAbbrev as nomTitleAbbrev, RefCategory.RefCategoryAbbrev, InRefCategory.RefCategoryAbbrev AS InRefCat, InRef.Title AS InRefTitle, InRef.nomTitleAbbrev AS inRefnomTitleAbbrev, InRef.refCache AS inRefCache, InRef.nomRefCache AS inRefnomRefCache " + 
				" FROM Reference INNER JOIN Reference AS InRef ON Reference.InRefFk = InRef.RefId INNER JOIN RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId INNER JOIN RefCategory AS InRefCategory ON InRef.RefCategoryFk = InRefCategory.RefCategoryId " +
							" WHERE (Reference.RefCategoryFk = 2) AND (InRef.RefCategoryFk = 1) ";
				ResultSet rs = source.getResultSet(strQueryPartOfJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are part-of-references that have an article as in-reference!");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("RefId");
					//int categoryFk = rs.getInt("RefCategoryFk");
					String cat = rs.getString("RefCategoryAbbrev");
					int inRefFk = rs.getInt("InRefId");
					//int inRefCategoryFk = rs.getInt("InRefCatFk");
					String inRefCat = rs.getString("InRefCat");
					String refCache = rs.getString("RefCache");
					String nomRefCache = rs.getString("nomRefCache");
					String title = rs.getString("title");
					String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
					String inRefTitle = rs.getString("InRefTitle");
					String inRefnomTitleAbbrev = rs.getString("inRefnomTitleAbbrev");
					String inRefnomRefCache = rs.getString("inRefnomRefCache");
					String inRefCache = rs.getString("inRefCache");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + "\n  title: " + title + "\n  titleAbbrev: " + nomTitleAbbrev + 
							"\n  inRefFk: " + inRefFk + "\n  inRefCategory: " + inRefCat + 
							"\n  inRefTitle: " + inRefTitle + "\n  inRefTitleAbbrev: " + inRefnomTitleAbbrev +
							"\n  inRefnomRefCache: " + inRefnomRefCache + "\n  inRefCache: " + inRefCache 
							);
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		private static boolean checkJournalsWithSeries(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryArticlesWithoutJournal = "SELECT Reference.RefId, Reference.RefCategoryFk, Reference.RefCache, Reference.NomRefCache, Reference.Title, Reference.NomTitleAbbrev, RefCategory.RefCategoryAbbrev  " + 
							" FROM Reference INNER JOIN " +
									" RefCategory ON Reference.RefCategoryFk = RefCategory.RefCategoryId  " +
							" WHERE (Reference.RefCategoryFk = 9)  AND ( Reference.series is not null OR Reference.series <>'') ";
				ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are Journals with series!");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("RefId");
					//int categoryFk = rs.getInt("RefCategoryFk");
					String cat = rs.getString("RefCategoryAbbrev");
					String nomRefCache = rs.getString("nomRefCache");
					String refCache = rs.getString("refCache");
					String title = rs.getString("title");
					String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + 
							"\n  title: " + title +  "\n  nomTitleAbbrev: " + nomTitleAbbrev +
							"" );
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		private static boolean checkPartOfWithVolume(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryArticlesWithoutJournal = "SELECT Ref.RefId as refId, RefCategory.RefCategoryAbbrev as refCategoryAbbrev, Ref.nomRefCache as nomRefCache, Ref.refCache as refCache,Ref.volume as volume, Ref.Series as series, Ref.Edition as edition, Ref.title as title, Ref.nomTitleAbbrev as nomTitleAbbrev,InRef.RefCache as inRefRefCache, InRef.NomRefCache  as inRefNomRefCache, InRef.RefId as inRefId, InRef.Volume as inRefVol, InRef.Series as inRefSeries, InRef.Edition as inRefEdition" +
						" FROM Reference AS Ref " + 
						 	" INNER JOIN RefCategory ON Ref.RefCategoryFk = RefCategory.RefCategoryId " +
						 	"  LEFT OUTER JOIN Reference AS InRef ON Ref.InRefFk = InRef.RefId " +
						" WHERE (Ref.RefCategoryFk = 2) AND ((Ref.Volume IS NOT NULL) OR (Ref.Series IS NOT NULL) OR (Ref.Edition IS NOT NULL)) " ; 
				ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are PartOfOtherTitles with volumes, editions or series !");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("refId");
					String cat = rs.getString("refCategoryAbbrev");
					String nomRefCache = rs.getString("nomRefCache");
					String refCache = rs.getString("refCache");
					String title = rs.getString("title");
					String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
					String volume = rs.getString("volume");
					String edition = rs.getString("edition");
					String series = rs.getString("series");
					String inRefRefCache = rs.getString("inRefRefCache");
					String inRefNomRefCache = rs.getString("inRefNomRefCache");
					int inRefId = rs.getInt("inRefId");
					String inRefVolume = rs.getString("inRefVol");
					String inRefSeries = rs.getString("inRefSeries");
					String inRefEdition = rs.getString("inRefEdition");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + 
							"\n  title: " + title +  "\n  nomTitleAbbrev: " + nomTitleAbbrev + "\n  volume: " + volume + "\n  series: " + series +"\n  edition: " + edition +
							"\n  inRef-ID:" + inRefId + "\n  inRef-cache: " + inRefRefCache +  
							"\n  inRef-nomCache: " + inRefNomRefCache + "\n  inRef-volume: " + inRefVolume +"\n  inRef-series: " + inRefSeries +"\n  inRef-edition: " + inRefEdition +
							"" );
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		private static boolean checkArticleWithEdition(BerlinModelImportConfigurator bmiConfig){
			try {
				boolean result = true;
				Source source = bmiConfig.getSource();
				String strQueryArticlesWithoutJournal = "SELECT Ref.RefId as refId, RefCategory.RefCategoryAbbrev as refCategoryAbbrev, Ref.nomRefCache as nomRefCache, Ref.refCache as refCache,Ref.edition as edition, Ref.title as title, Ref.nomTitleAbbrev as nomTitleAbbrev,InRef.RefCache as inRefRefCache, InRef.NomRefCache  as inRefNomRefCache, InRef.RefId as inRefId, InRef.Edition as inRefEdition" +
						" FROM Reference AS Ref " + 
						 	" INNER JOIN RefCategory ON Ref.RefCategoryFk = RefCategory.RefCategoryId " +
						 	"  LEFT OUTER JOIN Reference AS InRef ON Ref.InRefFk = InRef.RefId " +
						" WHERE (Ref.RefCategoryFk = 1) AND (NOT (Ref.Edition IS NULL))  " +
						" ORDER BY InRef.RefId "; 
				ResultSet rs = source.getResultSet(strQueryArticlesWithoutJournal);
				boolean firstRow = true;
				while (rs.next()){
					if (firstRow){
						System.out.println("========================================================");
						logger.warn("There are Articles with editions !");
						System.out.println("========================================================");
					}
					int refId = rs.getInt("refId");
					String cat = rs.getString("refCategoryAbbrev");
					String nomRefCache = rs.getString("nomRefCache");
					String refCache = rs.getString("refCache");
					String title = rs.getString("title");
					String nomTitleAbbrev = rs.getString("nomTitleAbbrev");
					String edition = rs.getString("edition");
					String inRefRefCache = rs.getString("inRefRefCache");
					String inRefNomRefCache = rs.getString("inRefNomRefCache");
					int inRefId = rs.getInt("inRefId");
					String inRefEdition = rs.getString("inRefEdition");
					
					System.out.println("RefID:" + refId + "\n  cat: " + cat + 
							"\n  refCache: " + refCache + "\n  nomRefCache: " + nomRefCache + 
							"\n  title: " + title +  "\n  nomTitleAbbrev: " + nomTitleAbbrev + "\n  edition: " + edition + 
							"\n  inRef-ID:" + inRefId + "\n  inRef-cache: " + inRefRefCache +  
							"\n  inRef-nomCache: " + inRefNomRefCache + "\n  inRef-edition: " + inRefEdition +
							"" );
					result = firstRow = false;
				}
				
				return result;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		protected boolean checkObligatoryAttributes(IImportConfigurator config, BerlinModelReferenceImport refImport){
			boolean result = true;
			
			try {
				String strQuery = " SELECT Reference.* " +
				    " FROM Reference " +
//	            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
				    " WHERE (1=0) ";
				BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
				Source source = bmiConfig.getSource();
				ResultSet rs = source.getResultSet(strQuery);
				int colCount = rs.getMetaData().getColumnCount();
				Set<String> existingAttributes = new HashSet<String>();
				for (int c = 0; c < colCount ; c++){
					existingAttributes.add(rs.getMetaData().getColumnLabel(c+1).toLowerCase());
				}
				Set<String> obligatoryAttributes = refImport.getObligatoryAttributes(true, bmiConfig);
				
				obligatoryAttributes.removeAll(existingAttributes);
				for (String attr : obligatoryAttributes){
					logger.warn("Missing attribute: " + attr);
				}
				
				//additional Attributes
				obligatoryAttributes = refImport.getObligatoryAttributes(true, bmiConfig);
				
				existingAttributes.removeAll(obligatoryAttributes);
				for (String attr : existingAttributes){
					logger.warn("Additional attribute: " + attr);
				}
			} catch (SQLException e) {
				logger.error(e);
				e.printStackTrace();
				result = false;
			}
			return result;
		}

		protected boolean checkRefDetailUnimplementedAttributes(IImportConfigurator config){
			boolean result = true;
			
			try {
				String strQuery = " SELECT Count(*) as n" +
				    " FROM RefDetail " +
//	            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
				    " WHERE SecondarySources is not NULL AND SecondarySources <> '' ";
				BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
				Source source = bmiConfig.getSource();
				ResultSet rs = source.getResultSet(strQuery);
				
				rs.next();
				int count = rs.getInt("n");
				if (count > 0){
					System.out.println("========================================================");
					logger.warn("There are "+ count + " RefDetails with SecondarySources <> NULL ! Secondary sources are not yet implemented for Berlin Model Import");
					System.out.println("========================================================");
					
				}
				strQuery = " SELECT Count(*) as n" +
				    " FROM RefDetail " +
//		            	" INNER JOIN Reference ON Reference.RefId = RefDetail.RefFk " +
				    " WHERE IdInSource is not NULL AND IdInSource <> '' ";
				rs = source.getResultSet(strQuery);
				
				rs.next();
				count = rs.getInt("n");
				if (count > 0){
					System.out.println("========================================================");
					logger.warn("There are "+ count + " RefDetails with IdInSource <> NULL ! IdInSource are not yet implemented for Berlin Model Import");
					System.out.println("========================================================");
					
				}
				
			} catch (SQLException e) {
				logger.error(e);
				e.printStackTrace();
				result = false;
			}
			return result;
		}



}
