package eu.etaxonomy.cdm.io.profiler;

import org.apache.log4j.Logger;

import com.yourkit.api.Controller;
//TODO move to common
public class ProfilerController {
	

	private static final Logger logger = Logger.getLogger(ProfilerController.class);
	
	private static Controller controller;
	private static int memSnapshotCnt = 0;
	
	private void init(){
		if(controller == null){
			try {
				controller = new Controller();
				memSnapshotCnt = 0;
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public static void memorySnapshot() {
		try {
			logger.info("taking memory snapshot " + memSnapshotCnt++);
			controller.captureMemorySnapshot();
		} catch (Exception e) {
			logger.error("taking memory snapshot " + memSnapshotCnt++, e);
		}
	}

}
