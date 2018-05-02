/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.sql.ResultSet;
import java.util.Map;

import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 * @since 16.02.2010
 */
public interface IPartitionedIO<STATE extends IPartitionedState> {
	
	public final String ID_LIST_TOKEN = "@IdList";
	
	//TODO make state more generic
	public boolean doPartition(ResultSetPartitioner partitioner, STATE state);
	
	public TransactionStatus startTransaction();
	
	public void commitTransaction(TransactionStatus txStatus);
	
	public String getPluralString();

	/**
	 * @param rs
	 * @return
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, STATE state);
//	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs);

	public IReferenceService getReferenceService();

}
