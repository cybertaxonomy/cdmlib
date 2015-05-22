package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.util.UUID;

import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExportConfigurator;

public class CsvNameExportState extends XmlExportState<CsvNameExportConfigurator>{

	boolean namesOnly = true;
	UUID classificationUUID; 
	
	public CsvNameExportState(CsvNameExportConfigurator config) {
		super(config);
		// TODO Auto-generated constructor stub
	}
	
	

}
