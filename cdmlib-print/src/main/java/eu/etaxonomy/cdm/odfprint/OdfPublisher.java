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
		entityCollector = configurator.getEntityCollector();
		odfFactory = configurator.newOdfFactory();
	}

	public void publish() {

		// TODO create one file for all or one file for each taxon, depending on
		// configurator parameters.

		// if (configurator.isAllInOne()) {
		TextDocument document;
		try {
			document = TextDocument.newTextDocument();

			for (UUID taxonNodeUUid : configurator.getNodesToPublish()) {

				// TODO
				// get taxa with collector
				// and create "chapters" for document with the factory
				// and add them to the document.

			}

			document.save("quick.odt.zip");

		} catch (Exception e) {
			logger.info("Unable to create odf document.");
			e.printStackTrace();
		}
		// }

	}

}
