/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.pilotOutputHtml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.jaxb.CdmMarshallerListener;
import eu.etaxonomy.cdm.io.sdd.out.SDDDataSet;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Writes the SDD XML file.
 *
 * @author h.fradin
 * @since 10.12.2008
 * @version 1.0
 */

public class PilotOutputDocumentBuilder {

	private SDDDataSet cdmSource;

	private static final Logger logger = Logger.getLogger(PilotOutputDocumentBuilder.class);

	// private SDDContext sddContext;

	public PilotOutputDocumentBuilder() throws IOException {

	}

	public void marshal(SDDDataSet cdmSource, String sddDestination) throws IOException {

		this.cdmSource = cdmSource;
		Marshaller marshaller;
		CdmMarshallerListener marshallerListener = new CdmMarshallerListener();
		logger.info("Start marshalling");
		writeCDMtoHTML(sddDestination);
	}

	/**Write the HTML document.
	 * @param base
	 * @throws IOException
	 */
	public void writeCDMtoHTML(String sddDestination) throws IOException {

		FileWriter writer = null;
		String texte = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n<html>\n<head>\n";
		texte += "<title>Taxon descriptions included in the CDM base</title>\n";
		texte += "</head>\n";
		texte += "<body bgcolor=\"#FFFFE8\" topmargin=\"5\">\n";
		texte += "<h1 align=\"center\">Taxon descriptions included in the CDM base</h1>\n";

		for (Iterator<? extends TaxonBase> tb = this.cdmSource.getTaxa().iterator() ; tb.hasNext() ;){

			Taxon taxon = (Taxon) tb.next();
			texte = writeTaxonDescriptionsToHTML(taxon, texte);

		}
		texte += "</body>\n</html>\n";

		try{
			writer = new FileWriter(sddDestination, true);
			writer.write(texte,0,texte.length());
		}catch(IOException ex){
			ex.printStackTrace();
		}finally{
			if(writer != null){
				writer.close();
			}
		}
	}

	/**Write the HTML part for one taxon.
	 * @param base
	 * @throws IOException
	 */
	public String writeTaxonDescriptionsToHTML(Taxon taxon, String texte) throws IOException {

		TaxonName taxonName = taxon.getName();
		String name = taxonName.getTitleCache();
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		for (Iterator<TaxonDescription> td = descriptions.iterator() ; td.hasNext() ;){
			TaxonDescription description = td.next();

			if (!description.getTitleCache().equals("")) {
				texte += "<h3>Name of description: " + description.getTitleCache() + "</h3>\n";
			} else {
				texte += "No title for this description.<br/>\n";
			}

			if (!name.equals("")) {
				texte += "<h3>Taxon name: " + name + "</h3>\n";
			} else {
				texte += "No taxon name attached to this description.<br/><br/>\n";
			}

			texte += "<ul type=\"circle\">\n";

			for (Iterator<? extends DescriptionElementBase> dep = description.getElements().iterator() ; dep.hasNext() ;){
				DescriptionElementBase descriptionElement = dep.next();

				if (descriptionElement instanceof CategoricalData) {
					CategoricalData categorical = (CategoricalData) descriptionElement;
					Feature feature = categorical.getFeature();
					Representation representation = (Representation) feature.getRepresentations().toArray()[0];
					texte += "<li>Categorical data associated with feature: <b>" + representation.getLabel() + "</b><br/>\nStates: ";
					for (Iterator<? extends StateData> sd = categorical.getStateData().iterator() ; sd.hasNext() ;){
						StateData stateData = sd.next();
						texte += ((Representation) stateData.getState().getRepresentations().toArray()[0]).getLabel();
						if (sd.hasNext()) {
                            texte += "; ";
                        } else {
                            texte += ".<br/>\n";
                        }
					}
					texte += "<br/>\n";
				}

				if (descriptionElement instanceof QuantitativeData) {
					QuantitativeData quantitative = (QuantitativeData) descriptionElement;
					Feature feature = quantitative.getFeature();
					Representation representation = (Representation) feature.getRepresentations().toArray()[0];
					texte += "<li>Quantitative data associated with feature: <b>" + representation.getLabel() + "</b><br/>\n";
					MeasurementUnit mu = quantitative.getUnit();
					String unit = ((Representation) mu.getRepresentations().toArray()[0]).getLabel();
					if (!unit.equals("")) {
                        texte += "Measurement unit: " + unit + "<br/>\n";
                    }
					for (Iterator<? extends StatisticalMeasurementValue> smv = quantitative.getStatisticalValues().iterator() ; smv.hasNext() ;){
						StatisticalMeasurementValue statistical = smv.next();
						texte += ((Representation) statistical.getType().getRepresentations().toArray()[0]).getLabel();
						texte += " = " + statistical.getValue() + "<br/>\n";
					}
					texte += "<br/>\n";
				}

				if (descriptionElement instanceof TextData) {
					TextData text = (TextData) descriptionElement;
					Feature feature = text.getFeature();
					Representation representation = (Representation) feature.getRepresentations().toArray()[0];
					texte += "<li>Text data associated with feature: <b>" + representation.getLabel() + "</b><br/>\n";
					texte += "Text: " + text.getMultilanguageText().get((text.getMultilanguageText().keySet().toArray()[0])).getText() + "<br/>\n";
					texte += "<br/>\n";
				}

			}

			texte += "</ul>\n";
		}
		return texte;
	}
}


