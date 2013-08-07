package eu.etaxonomy.cdm.odfprint;

import java.util.UUID;
import org.odftoolkit.simple.TextDocument;
import org.apache.log4j.Logger;

public class OdfPublisher {

	private static final Logger logger = Logger.getLogger(OdfPublisher.class);

	private OdfConfigurator configurator;
	private IOdfEntityCollector entityCollector;
	private OdfFactory odfFactory;

	public OdfConfigurator getConfigurator() {
		return configurator;
	}

	public void setConfigurator(OdfConfigurator configurator) {
		this.configurator = configurator;
	}

	public OdfPublisher(OdfConfigurator configurator) {
		this.configurator = configurator;
		// get tools for publishing from configurator:
		
	}

	public void publish() {

		// TODO create one file for all or one file for each taxon, depending on
		// configurator parameters.

		// if (configurator.isAllInOne()) {
		
		
		try {
			TextDocument document = TextDocument.newTextDocument();
			
			entityCollector = configurator.newEntityCollector();
			odfFactory = configurator.newOdfFactory();
			
			for (UUID taxonNodeUUid : configurator.getNodesToPublish()) {
				String taxonTitle=entityCollector.getTaxonTitle(taxonNodeUUid);
				// TODO
				// get taxa with collector
				// and create "chapters" for document with the factory
				// and add them to the document.

			}

			document.save(configurator.getExportFile());

		} catch (Exception e) {
			logger.info("Unable to create odf document.");
			e.printStackTrace();
		}
		// }

	}

}
