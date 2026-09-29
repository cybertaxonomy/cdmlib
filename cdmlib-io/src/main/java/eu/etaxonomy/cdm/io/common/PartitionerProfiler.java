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
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.mueller
 * @since 21.02.2010
 */
public class PartitionerProfiler {

    private static final Logger logger = LogManager.getLogger();

	ResultSetPartitioner<?> partitioner;

	LocalDateTime startTx = LocalDateTime.now();
	LocalDateTime startRs = LocalDateTime.now();
	LocalDateTime startRelObjects = LocalDateTime.now();
	LocalDateTime startRS2 = LocalDateTime.now();
	LocalDateTime startDoPartition = LocalDateTime.now();
	LocalDateTime startDoSave = LocalDateTime.now();
	LocalDateTime startDoCommit = LocalDateTime.now();
	LocalDateTime end = LocalDateTime.now();

	private Duration durTxStartAll = Duration.ZERO;
	private Duration durPartitionRs1All= Duration.ZERO;
	private Duration durRelObjectsAll = Duration.ZERO;
	private Duration durPartitionRs2All = Duration.ZERO;
	private Duration durPartitionAll = Duration.ZERO;
	private Duration durTxCommitAll = Duration.ZERO;
	private Duration durSaveAll = Duration.ZERO;

	private Duration durTxStartSingle;
	private Duration durPartitionRs1Single;
	private Duration durRelObjectsSingle;
	private Duration durPartitionRs2Single;
	private Duration durPartitionSingle;
	private Duration durSaveSingle;
	private Duration durTxCommitSingle;

	public void startTx(){
		startTx = LocalDateTime.now();
	}

	public void startRs(){
		startRs = LocalDateTime.now();
		durTxStartSingle = Duration.between(startTx, startRs);
		durTxStartAll = durTxStartAll.plus(durTxStartSingle);
	}

	public void startRelObjects(){
		startRelObjects = LocalDateTime.now();
		durPartitionRs1Single = Duration.between(startRs, startRelObjects);
		durPartitionRs1All= durPartitionRs1All.plus(durPartitionRs1Single);
	}

	public void startRs2(){
		startRS2 = LocalDateTime.now();
		durRelObjectsSingle = Duration.between(startRelObjects, startRS2);
		durRelObjectsAll = durRelObjectsAll.plus(durRelObjectsSingle);
	}

	public void startDoPartition(){
		startDoPartition = LocalDateTime.now();
		startDoSave = LocalDateTime.now();
		durPartitionRs2Single = Duration.between(startRS2, startDoPartition);
		durPartitionRs2All = durPartitionRs2All.plus(durPartitionRs2Single);
	}

	public void startDoSave(){
		startDoSave = LocalDateTime.now();
		//durSaveSingle = Duration.between(startRS2, startSave);
		//durPartitionRs2All = durPartitionRs2All.withDurationAdded(durPartitionRs2Single, 1);
	}

	public void startDoCommit(){
		startDoCommit = LocalDateTime.now();
		durPartitionSingle = Duration.between(startDoPartition, startDoCommit);
		durPartitionAll = durPartitionAll.plus(durPartitionSingle);
		durSaveSingle = Duration.between(startDoSave, startDoCommit);
		durSaveAll = durSaveAll.plus(durSaveSingle);
	}

	public void end(){
		end = LocalDateTime.now();
		durTxCommitSingle = Duration.between(startDoCommit, end);
		durTxCommitAll = durTxCommitAll.plus(durTxCommitSingle);
	}

	public void print(){
		if (logger.isDebugEnabled()){
			System.out.println("Durations: " +
					"Start Transaction: " + durTxStartSingle.getNano() + "/" + durTxStartAll.getNano() +
					"; partitionRS1: " + durPartitionRs1Single.getNano() + "/" + durPartitionRs1All.getNano() +
					"; getRelatedObjects: " + durRelObjectsSingle.getNano() + "/" + durRelObjectsAll.getNano() +
					"; partitionRS2 " + durPartitionRs2Single.getNano() + "/" + durPartitionRs2All.getNano() +
					"; doPartition " + durPartitionSingle.getNano() + "/" + durPartitionAll.getNano() +
					"; doSave " + durSaveSingle.getNano() + "/" + durSaveAll.getNano() +
					"; commit " + durTxCommitSingle.getNano() + "/" + durTxCommitAll.getNano()
			);
		}
	}
}