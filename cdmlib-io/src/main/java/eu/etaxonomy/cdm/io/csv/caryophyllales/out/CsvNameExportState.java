package eu.etaxonomy.cdm.io.csv.caryophyllales.out;

import java.util.UUID;

import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.model.description.Feature;

public class CsvNameExportState extends XmlExportState<CsvNameExportConfigurator>{

	boolean namesOnly = true;
	UUID classificationUUID;
	Feature notesFeature = null;

	public CsvNameExportState(CsvNameExportConfigurator config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	public Feature getNotesFeature(){
	    return notesFeature;
	}

    /**
     * @param feature
     */
    public void setNotesFeature(Feature feature) {
        notesFeature = feature;

    }


}
