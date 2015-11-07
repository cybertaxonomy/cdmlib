/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.profiler;

import org.apache.log4j.Logger;

import com.yourkit.api.Controller;
//TODO move to common
public class ProfileController {


    private static final Logger logger = Logger.getLogger(ProfileController.class);

    private static Controller controller;
    private static int memSnapshotCnt = 0;

    private static void init(){
        if(controller == null){
            try {
                controller = new Controller();
                memSnapshotCnt = 0;
            } catch (Exception e) {
                logger.info("The initialization of ProfileController fails");
                System.err.println("The initialization of ProfileController fails " + e.getMessage());
            }
        }
    }

    public static void memorySnapshot() {
        init();
        try {
            controller.forceGC();
            logger.info("snapshot " + memSnapshotCnt++ + " to file: " + controller.captureMemorySnapshot());
            System.out.println("snapshot " + memSnapshotCnt++ + " to file: " + controller.captureMemorySnapshot());
        } catch (Exception e) {
            logger.info("taking memory snapshot " + memSnapshotCnt + " failed");
            System.err.println("taking memory snapshot " + memSnapshotCnt + " failed " + e.getMessage());
        }
    }

}
