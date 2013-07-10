// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.validation;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportConfigurator;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns.CentralAfricaFernsImportState;

/**
 * @author a.mueller
 * @created 17.02.2010
 * @version 1.0
 */
public class CentralAfricaFernsTaxonImportValidator implements IOValidator<CentralAfricaFernsImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsTaxonImportValidator.class);

	public boolean validate(CentralAfricaFernsImportState state){
		boolean result = true;
		CentralAfricaFernsImportConfigurator config = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
//		result &= checkParentTaxonStatus(config);
//		result &= checkAccParentTaxonStatus(config);
		result &= checkSynonymsAcceptedTaxonStatus(config);
		return result;
	}
	
//	private boolean checkAccParentTaxonStatus(CentralAfricaFernsImportConfigurator bmiConfig){
//		try {
//			boolean result = true;
//			Source source = bmiConfig.getSource();
//			String strSQL = 
//				" SELECT    myTaxon.id AS childId, childStatus.status_name AS childStatus, myTaxon.tu_status, " +
//                      " myTaxon.tu_displayname AS childDisplayName, parent.id AS parentId, parent.tu_status AS parentStatusId, parentStatus.status_name AS parentStatus, " + 
//                      " parent.tu_displayname as parentName, parentAcc.id AS parentAccId, parentAccStatus.status_name AS parentAccStatus, parentAcc.tu_displayname AS parentAccName, " + 
//                      " parentAcc.tu_status AS Expr1 " +
//                " FROM status AS parentAccStatus INNER JOIN " +
//                      " tu AS parentAcc ON parentAccStatus.status_id = parentAcc.tu_status RIGHT OUTER JOIN " +
//                      " tu AS parent ON parentAcc.id = parent.tu_acctaxon RIGHT OUTER JOIN " +
//                      " tu AS myTaxon ON parent.id = myTaxon.tu_parent LEFT OUTER JOIN " + 
//                      " status AS parentStatus ON parent.tu_status = parentStatus.status_id LEFT OUTER JOIN " +
//                      " status AS childStatus ON myTaxon.tu_status = childStatus.status_id " +
//                 " WHERE     (myTaxon.tu_status = 1) AND (parent.tu_status <> 1) " + 
//                 " ORDER BY parentStatusId";
//			ResultSet rs = source.getResultSet(strSQL);
//			boolean firstRow = true;
//			int i = 0;
//			while (rs.next()){
//				i++;
//				if (firstRow){
//					System.out.println("========================================================");
//					logger.warn("There are accepted taxa that have an unaccepted parent and also the parents accepted taxon (tu_acctaxon) is not accepted. ");
//					System.out.println("========================================================");
//				}
//				int childId = rs.getInt("childId");
//				String childName = rs.getString("childDisplayName");
//				
//				int parentId = rs.getInt("parentId");
//				String parentName = rs.getString("parentName");
//				String parentStatus = rs.getString("parentStatus");
//				
//				int accParentId = rs.getInt("parentAccId");
//				String accParentName = rs.getString("parentAccName");
//				String accParentStatus = rs.getString("parentAccStatus");
//				
//				System.out.println(
//						"ChildId:" + childId + "\n    childName: " + childName + 
//						"\n  ParentId: " + parentId + "\n    parentName: " + parentName + "\n    parentStatus: " + parentStatus + 
//						"\n  ParentAccId: " + accParentId +  "\n    accParentName: " + accParentName + "\n   accParentStatus: " + accParentStatus );
//				result = firstRow = false;
//			}
//			if (i > 0){
//				System.out.println(" ");
//			}
//			
//			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
//	}
	
	private boolean checkSynonymsAcceptedTaxonStatus(CentralAfricaFernsImportConfigurator bmiConfig){
//		try {
//			boolean result = true;
////			Source source = bmiConfig.getSource();
////			String strSQL = 
////				" SELECT    myTaxon.id AS synonymId, myTaxon.tu_displayname AS synonymName, synonymStatus.status_name AS synonymStatus, " + 
////					" accTaxon.id AS acceptedId, accTaxon.tu_displayname AS acceptedName, acceptedStatus.status_name AS acceptedStatus " +
////				" FROM tu AS myTaxon INNER JOIN " +
////                    " tu AS accTaxon ON myTaxon.tu_acctaxon = accTaxon.id INNER JOIN " + 
////                    " status AS synonymStatus ON myTaxon.tu_status = synonymStatus.status_id INNER JOIN " +
////                    " status AS acceptedStatus ON accTaxon.tu_status = acceptedStatus.status_id " +
////                " WHERE (myTaxon.tu_status <> 1) AND (accTaxon.tu_status <> 1) " +
////                " ORDER BY myTaxon.tu_status, accTaxon.tu_status ";
////			ResultSet rs = source.getResultSet(strSQL);
////			boolean firstRow = true;
////			int i = 0;
////			while (rs.next()){
////				i++;
////				if (firstRow){
////					System.out.println("========================================================");
////					logger.warn("There are accepted synonyms that have an unaccepted taxon that has no status 'accepted'. ");
////					System.out.println("========================================================");
////				}
////				int synonymId = rs.getInt("synonymId");
////				String synonymName = rs.getString("synonymName");
////				String synonymStatus = rs.getString("synonymStatus");
////				
////				int acceptedId = rs.getInt("acceptedId");
////				String acceptedName = rs.getString("acceptedName");
////				String acceptedStatus = rs.getString("acceptedStatus");
////				
////				System.out.println(
////						"SynonymId:" + synonymId + "\n    synonymName: " + synonymName + "\n    synonymStatus: " + synonymStatus + 
////						"\n  AcceptedId: " + acceptedId + "\n    acceptedName: " + acceptedName + "\n    acceptedStatus: " + acceptedStatus  
//////					+   "\n parentAccId: " + acceptedId +  "\n  accParentName: " + accParentName + "\n accParentStatus: " + accParentStatus 
////						);
////				result = firstRow = false;
////			}
////			if (i > 0){
////				System.out.println(" ");
////			}
////			
//			return result;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}
			return true;
	}
	


}
