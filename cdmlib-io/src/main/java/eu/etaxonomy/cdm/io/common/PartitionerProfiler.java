/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.apache.log4j.Logger;



/**
 * @author a.mueller
 * @created 21.02.2010
 * @version 1.0
 */
public class PartitionerProfiler {
	private static final Logger logger = Logger.getLogger(PartitionerProfiler.class);

	ResultSetPartitioner partitioner;

	ZonedDateTime startTx = ZonedDateTime.now();
	ZonedDateTime startRs = ZonedDateTime.now();
	ZonedDateTime startRelObjects = ZonedDateTime.now();
	ZonedDateTime startRS2 = ZonedDateTime.now();
	ZonedDateTime startDoPartition = ZonedDateTime.now();
	ZonedDateTime startDoSave = ZonedDateTime.now();
	ZonedDateTime startDoCommit = ZonedDateTime.now();
	ZonedDateTime end = ZonedDateTime.now();

	private Duration durTxStartAll =Duration.ofMinutes(0);
	private Duration durPartitionRs1All= Duration.ofMinutes(0);
	private Duration durRelObjectsAll = Duration.ofMinutes(0);
	private Duration durPartitionRs2All =Duration.ofMinutes(0);
	private Duration durPartitionAll = Duration.ofMinutes(0);
	private Duration durTxCommitAll = Duration.ofMinutes(0);
	private Duration durSaveAll = Duration.ofMinutes(0);


	private Duration durTxStartSingle;
	private Duration durPartitionRs1Single;
	private Duration durRelObjectsSingle;
	private Duration durPartitionRs2Single;
	private Duration durPartitionSingle;
	private Duration durSaveSingle;
	private Duration durTxCommitSingle;

	public void startTx(){
		startTx = ZonedDateTime.now();

	}

	public void startRs(){
		startRs = ZonedDateTime.now();

		durTxStartSingle = Duration.between(startTx, startRs);
		durTxStartAll = durTxStartAll.plus(durTxStartSingle);
	}

	public void startRelObjects(){
		startRelObjects =ZonedDateTime.now();
		durPartitionRs1Single = Duration.between(startRs, startRelObjects);
		durPartitionRs1All= durPartitionRs1All.plus(durPartitionRs1Single);
	}

	public void startRs2(){
		startRS2 = ZonedDateTime.now();
		durRelObjectsSingle = Duration.between(startRelObjects, startRS2);
		durRelObjectsAll = durRelObjectsAll.plus(durRelObjectsSingle);
	}

	public void startDoPartition(){
		startDoPartition = ZonedDateTime.now();
		startDoSave = ZonedDateTime.now();
		durPartitionRs2Single = Duration.between(startRS2, startDoPartition);
		durPartitionRs2All = durPartitionRs2All.plus(durPartitionRs2Single);
	}

	public void startDoSave(){
		startDoSave = ZonedDateTime.now();
		//durSaveSingle = new Duration(startRS2, startSave);
		//durPartitionRs2All = durPartitionRs2All.withDurationAdded(durPartitionRs2Single, 1);
	}

	public void startDoCommit(){
		startDoCommit = ZonedDateTime.now();
		durPartitionSingle = Duration.between(startDoPartition, startDoCommit);
		durPartitionAll = durPartitionAll.plus(durPartitionSingle);
		durSaveSingle = Duration.between(startDoSave, startDoCommit);
		durSaveAll = durSaveAll.plus(durSaveSingle);
	}

	public void end(){
		end = ZonedDateTime.now();
		durTxCommitSingle = Duration.between(startDoCommit, end);
		durTxCommitAll = durTxCommitAll.plus(durTxCommitSingle);
	}

	public void print(){
		if (logger.isDebugEnabled()){
			System.out.println("Durations: " +
					"Start Transaction: " + durTxStartSingle.getNano() + "/" + durTxStartAll.getNano() +
					"; partitionRS1: " + durPartitionRs1Single.getNano() + "/" + durPartitionRs1All.getNano()+
					"; getRelatedObjects: " + durRelObjectsSingle.getNano() + "/" + durRelObjectsAll.getNano() +
					"; partitionRS2 " + durPartitionRs2Single.getNano() + "/" + durPartitionRs2All.getNano() +
					"; doPartition " + durPartitionSingle.getNano() + "/" + durPartitionAll.getNano() +
					"; doSave " + durSaveSingle.getNano() + "/" + durSaveAll.getNano() +
					"; commit " + durTxCommitSingle.getNano() + "/" + durTxCommitAll.getNano()
			);
		}
	}

}
