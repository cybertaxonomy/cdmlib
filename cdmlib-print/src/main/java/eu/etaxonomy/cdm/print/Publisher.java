/**
 * 
 */
package eu.etaxonomy.cdm.print;

import org.apache.log4j.Logger;
import org.jdom.Document;

import eu.etaxonomy.cdm.print.out.IPublishOutputModule;

/**
 * The central or main class that will execute the publishing process
 * 
 * @author n.hoffmann
 * @since Apr 1, 2010
 * @version 1.0
 */
public class Publisher {
	private static final Logger logger = Logger.getLogger(Publisher.class);	
	
	
	/**
	 * Harvests the taxon nodes defined by {@link PublishConfigurator#getSelectedTaxonNodeElements()} and calls 
	 * alls {@link IPublishOutputModule IPublishOutputModules} {@link IPublishOutputModule#output(Document, PublishConfigurator) output}
	 * methods
	 * 
	 * @param configurator a valid {@link PublishConfigurator}
	 */
	public static void publish(PublishConfigurator configurator){
		
		XMLHarvester xmlHarvester = new XMLHarvester(configurator);
		
		Document document = xmlHarvester.harvest(configurator.getSelectedTaxonNodeElements());
		
		if(configurator.getOutputModules().size() == 0){
			logger.warn("No output modules set. Exiting.");
		}
		
		for(IPublishOutputModule outputModule : configurator.getOutputModules()){
			outputModule.output(document, configurator.getExportFolder(), configurator.getProgressMonitor());
		}
		
	}
	
}
