/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;


/**
 * @author a.mueller
 * @since 21.02.2010
 * @version 1.0
 */
public class PartitionerProfiler {
	private static final Logger logger = Logger.getLogger(PartitionerProfiler.class);
	
	ResultSetPartitioner partitioner;
	
	DateTime startTx = new DateTime();
	DateTime startRs = new DateTime();
	DateTime startRelObjects = new DateTime();
	DateTime startRS2 = new DateTime();
	DateTime startDoPartition = new DateTime();
	DateTime startDoSave = new DateTime();
	DateTime startDoCommit = new DateTime();
	DateTime end = new DateTime();

	private Duration durTxStartAll = new Duration(0, 0);
	private Duration durPartitionRs1All= new Duration(0, 0);
	private Duration durRelObjectsAll = new Duration(0, 0);
	private Duration durPartitionRs2All =new Duration(0, 0);
	private Duration durPartitionAll = new Duration(0, 0);
	private Duration durTxCommitAll = new Duration(0, 0);
	private Duration durSaveAll = new Duration(0, 0);

	
	private ReadableDuration durTxStartSingle;
	private ReadableDuration durPartitionRs1Single;
	private ReadableDuration durRelObjectsSingle;
	private ReadableDuration durPartitionRs2Single;
	private ReadableDuration durPartitionSingle;
	private ReadableDuration durSaveSingle;
	private ReadableDuration durTxCommitSingle;

	public void startTx(){
		startTx = new DateTime();

	}
	
	public void startRs(){
		startRs = new DateTime();
		durTxStartSingle = new Duration(startTx, startRs);
		durTxStartAll = durTxStartAll.withDurationAdded(durTxStartSingle, 1);
	}

	public void startRelObjects(){
		startRelObjects = new DateTime();
		durPartitionRs1Single = new Duration(startRs, startRelObjects);
		durPartitionRs1All= durPartitionRs1All.withDurationAdded(durPartitionRs1Single, 1);
	}

	public void startRs2(){
		startRS2 = new DateTime();
		durRelObjectsSingle = new Duration(startRelObjects, startRS2);
		durRelObjectsAll = durRelObjectsAll.withDurationAdded(durRelObjectsSingle, 1);
	}

	public void startDoPartition(){
		startDoPartition = new DateTime();
		startDoSave = new DateTime();
		durPartitionRs2Single = new Duration(startRS2, startDoPartition);
		durPartitionRs2All = durPartitionRs2All.withDurationAdded(durPartitionRs2Single, 1);
	}
	
	public void startDoSave(){
		startDoSave = new DateTime();
		//durSaveSingle = new Duration(startRS2, startSave);
		//durPartitionRs2All = durPartitionRs2All.withDurationAdded(durPartitionRs2Single, 1);
	}

	public void startDoCommit(){
		startDoCommit = new DateTime();
		durPartitionSingle = new Duration(startDoPartition, startDoCommit);
		durPartitionAll = durPartitionAll.withDurationAdded(durPartitionSingle, 1);
		durSaveSingle = new Duration(startDoSave, startDoCommit);
		durSaveAll = durSaveAll.withDurationAdded(durSaveSingle, 1);
	}
	
	public void end(){
		end = new DateTime();
		durTxCommitSingle = new Duration(startDoCommit, end);
		durTxCommitAll = durTxCommitAll.withDurationAdded(durTxCommitSingle, 1);
	}

	public void print(){
		if (logger.isDebugEnabled()){
			System.out.println("Durations: " +
					"Start Transaction: " + durTxStartSingle.getMillis() + "/" + durTxStartAll.getMillis() +   
					"; partitionRS1: " + durPartitionRs1Single.getMillis() + "/" + durPartitionRs1All.getMillis() +
					"; getRelatedObjects: " + durRelObjectsSingle.getMillis() + "/" + durRelObjectsAll.getMillis() +
					"; partitionRS2 " + durPartitionRs2Single.getMillis() + "/" + durPartitionRs2All.getMillis() +
					"; doPartition " + durPartitionSingle.getMillis() + "/" + durPartitionAll.getMillis() +
					"; doSave " + durSaveSingle.getMillis() + "/" + durSaveAll.getMillis() +
					"; commit " + durTxCommitSingle.getMillis() + "/" + durTxCommitAll.getMillis() 
			);
		}
	}
	
}
