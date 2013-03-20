/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.profiler;

import org.apache.log4j.Logger;

import com.yourkit.api.Controller;
//TODO move to common
public class ProfilerController {
	

	private static final Logger logger = Logger.getLogger(ProfilerController.class);
	
	private static Controller controller;
	private static int memSnapshotCnt = 0;
	
	private static void init(){
		if(controller == null){
			try {
				controller = new Controller();
				memSnapshotCnt = 0;
			} catch (Exception e) {
				logger.info("The initialization of ProfilerCOntroller fails");
			}
		}
	}
	
	public static void memorySnapshot() {
		init();
		try {
			logger.info("taking memory snapshot " + memSnapshotCnt++);
			controller.captureMemorySnapshot();
		} catch (Exception e) {
			logger.info("taking memory snapshot " + memSnapshotCnt++ + " fails");
		}
	}

}
