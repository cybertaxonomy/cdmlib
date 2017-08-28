/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.Contact;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaEmlExport extends DwcaExportBase {

    private static final long serialVersionUID = -1762545757384406718L;

    private static final Logger logger = Logger.getLogger(DwcaEmlExport.class);

	protected static final String fileName = "eml.xml";

	private String emlNamespace = "eml://ecoinformatics.org/eml-2.1.1";
	private String mdNamespace="eml://ecoinformatics.org/methods-2.1.0";
	private String projNamespace="eml://ecoinformatics.org/project-2.1.0";
	private String datasetNamespace="eml://ecoinformatics.org/dataset-2.1.0";
	private String resNamespace="eml://ecoinformatics.org/resource-2.1.0";
	private String dcNamespace="http://purl.org/dc/terms/";
	private String xsiNamespace="http://www.w3.org/2001/XMLSchema-instance";
	private String schemaLocation = "eml://ecoinformatics.org/eml-2.1.1 http://rs.gbif.org/schema/eml-gbif-profile/dev/eml.xsd";

	/**
	 * Constructor
	 */
	public DwcaEmlExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/**
	 * Retrieves the MetaData for a Darwin Core Archive File.
	 * <BR>
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();

		DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, null);
		metaRecord.setMetaData(true);
		state.addMetaRecord(metaRecord);

		DwcaEmlRecord emlRecord = config.getEmlRecord();
		if (emlRecord == null){
			return;
		}

		XMLStreamWriter writer = null;
		try {
			writer = createXmlStreamWriter(state, DwcaTaxExportFile.EML);

			String rootName = "eml";

			// create header
			//TODO encoding
			writer.writeStartDocument();
//			writer.setDefaultNamespace(rootNamespace);

				// create root element
//				writer.setPrefix("eml",emlNamespace);
				writer.writeStartElement("eml", rootName, emlNamespace);
				writer.writeNamespace("eml", emlNamespace);
				writer.writeNamespace("md", mdNamespace);
				writer.writeNamespace("proj", projNamespace);
				writer.writeNamespace("d", datasetNamespace);
				writer.writeNamespace("res", resNamespace);
				writer.writeNamespace("dc", dcNamespace);
				writer.writeNamespace("xsi", xsiNamespace);

				writer.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", schemaLocation);
				writer.writeAttribute("packageId", emlRecord.getIdentifier());
				writer.writeAttribute("system", "CDM Library Darwin Core Archive Exporter");
				writer.writeAttribute("scope", "system");

				if (emlRecord.getMetaDataLanguage() != null ){
					writer.writeAttribute("xml", "lang", emlRecord.getMetaDataLanguage().getIso639_2()); //TODO needed ?
				}

				writeDataSet(writer, config, emlRecord);
				writeAdditionalMetadata(writer, config, emlRecord);
				writer.flush();
				writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			if (e.getNestedException() != null){
				throw new RuntimeException(e.getNestedException());
			}else{
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}  finally{
			closeWriter(writer, state);
		}

		return;
	}


	private void writeAdditionalMetadata(XMLStreamWriter writer, DwcaTaxExportConfigurator config, DwcaEmlRecord emlRecord) throws XMLStreamException {
		writer.writeStartElement("additionalMetadata");
		writer.writeStartElement("metadata");
		writer.writeStartElement("gbif");

			String elementName;
			String text;

			elementName = "dateStamp";
			text = ZonedDateTime.now().toString();
			writeTextElement(writer, elementName, text);

			elementName = "citation";
			text = emlRecord.getExpectedCitation();
			writeTextElement(writer, elementName, text);

			writer.writeStartElement("bibliography");
			for (Reference ref: emlRecord.getReferences()){
				elementName = "citation";
				text = ref.getTitleCache();
				writeTextElement(writer, elementName, text);
			}
			writer.writeEndElement();


		writer.writeEndElement();	//gbif
		writer.writeEndElement();	//metadata
		writer.writeEndElement();	//additionalMetadata

	}

	private void writeDataSet(XMLStreamWriter writer,
			DwcaTaxExportConfigurator config, DwcaEmlRecord emlRecord) throws XMLStreamException {


		writer.writeStartElement("dataset");

			String elementName;
			String text;

			elementName = "alternateIdentifier";
			text = null;
			writeTextElement(writer, elementName, text);


			elementName = "title";
			text = emlRecord.getTitle();
			//TODO language attribute
			writeTextElement(writer, elementName, text);

			//creator
			writer.writeStartElement("creator");
				writePerson(writer, emlRecord.getResourceCreator());
			writer.writeEndElement();

			//metadataProvider
			writer.writeStartElement("metadataProvider");
				writePerson(writer, emlRecord.getResourceCreator());
			writer.writeEndElement();

			//associatedParty
			for (InstitutionalMembership author : emlRecord.getAuthors()){
				writer.writeStartElement("associatedParty");
					writePerson(writer, author);
				writer.writeEndElement();

			}

			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd");
			elementName = "pubDate";
			text = emlRecord.getPublicationDate().format(dateFormatter);
			writeTextElement(writer, elementName, text);

			elementName = "language";
			//TODO
			text = emlRecord.getResourceLanguage()== null? null:emlRecord.getResourceLanguage().getLabel() ;
			writeTextElement(writer, elementName, text);

			elementName = "abstract";
			//TODO para
			text = emlRecord.getAbstractInfo();
			writeTextElement(writer, elementName, text);

			writer.writeStartElement("keywordSet");
				for (String keyword : emlRecord.getKeywords()){
					elementName = "keyword";
					text = keyword;
					writeTextElement(writer, elementName, text);
				}
				elementName = "keyword";
				text = emlRecord.getKeywordThesaurus();
				writeTextElement(writer, elementName, text);
			writer.writeEndElement();

			//TODO taxonomic keywords

			//additional Info //TODO para ?
			elementName = "additionalInfo";
			text = emlRecord.getAdditionalInformation();
			writeTextElement(writer, elementName, text);

			//TODO intellectualRights
//			elementName = "intellectualRights";
//			text = emlRecord.getRights(rights);
//			writeParaTextElement

			//TODO distribution //TODO online
			writer.writeStartElement("distribution");
				writer.writeAttribute("scope", "document");
				writer.writeStartElement("online");
					writer.writeStartElement("url");
					writer.writeAttribute("function", "information");
					writer.writeCharacters(nullSafe(emlRecord.getResourceUrl()));
					writer.writeEndElement();
				writer.writeEndElement(); //online
			writer.writeEndElement(); //distribution

			//TODO coverage
			writeCoverage(writer, emlRecord);

			//contact
			writer.writeStartElement("contact");
				writePerson(writer, emlRecord.getContact());
			writer.writeEndElement();


			//TODO project
			writer.writeStartElement("project");
				//title
				elementName = "title";
				text = emlRecord.getProjectTitle();
				writeTextElement(writer, elementName, text);

				writer.writeStartElement("personnel");
					elementName = "organizationName";
					text = emlRecord.getProjectLead();
					writeTextElement(writer, elementName, text);

					//TODO ??
					elementName = "role";
					text = "Distributor";
					writeTextElement(writer, elementName, text);

				writer.writeEndElement();


				writer.writeStartElement("funding");
				writer.writeEndElement();

				writer.writeStartElement("studyAreaDescription");
					writer.writeStartElement("descriptor");

					elementName = "descriptorValue";
					text = emlRecord.getProjectDescription();
					writeTextElement(writer, elementName, text);

					writer.writeEndElement();
				writer.writeEndElement();

				writer.writeStartElement("designDescription");
				writer.writeEndElement();

		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param emlRecord
	 * @throws XMLStreamException
	 */
	private void writeCoverage(XMLStreamWriter writer, DwcaEmlRecord emlRecord)
			throws XMLStreamException {
		String elementName;
		String text;
		writer.writeStartElement("coverage");
			handleGeoCoverage(writer, emlRecord);
			handleTermporalCoverage(writer, emlRecord);
		writer.writeEndElement(); //coverage
	}

	/**
	 * @param writer
	 * @param emlRecord
	 * @throws XMLStreamException
	 */
	private void handleGeoCoverage(XMLStreamWriter writer,
			DwcaEmlRecord emlRecord) throws XMLStreamException {
		String elementName;
		String text;
		writer.writeStartElement("geographicCoverage");

			//geographic description
			elementName = "geographicDescription";
			text = emlRecord.getRegionalScope();
			writeTextElement(writer, elementName, text);

			//boundingCoordinates
			writer.writeStartElement("boundingCoordinates");
				if (emlRecord.getUpperLeftCorner() != null){
					//west
					elementName = "westBoundingCoordinate";
					text = emlRecord.getUpperLeftCorner().getLatitude().toString();
					writeTextElement(writer, elementName, text);
				}

				if (emlRecord.getLowerRightCorner() != null){
					//east
					elementName = "eastBoundingCoordinate";
					text = emlRecord.getLowerRightCorner().getLatitude().toString();
					writeTextElement(writer, elementName, text);
				}

				if (emlRecord.getUpperLeftCorner() != null){
					//north
					elementName = "northBoundingCoordinate";
					text = emlRecord.getUpperLeftCorner().getLongitude().toString();
					writeTextElement(writer, elementName, text);
				}
				if (emlRecord.getLowerRightCorner() != null){
					//south
					elementName = "southBoundingCoordinate";
					text = emlRecord.getLowerRightCorner().getLongitude().toString();
					writeTextElement(writer, elementName, text);
				}
			writer.writeEndElement(); //boundingCoordinates

		writer.writeEndElement(); //geographicCoverage
	}

	/**
	 * @param writer
	 * @param emlRecord
	 * @throws XMLStreamException
	 */
	private void handleTermporalCoverage(XMLStreamWriter writer,
			DwcaEmlRecord emlRecord) throws XMLStreamException {

		TimePeriod timePeriod = emlRecord.getDate();
		if (timePeriod == null){
			return;
		}

		writer.writeStartElement("termporalCoverage");
			if (! timePeriod.isPeriod()){
				//singleDateTime
				writer.writeStartElement("singleDateTime");
					writeCalendarDate(writer, timePeriod.getStart());
				writer.writeEndElement();
			}else {
				//rangeOfDates
				writer.writeStartElement("rangeOfDates");
					writer.writeStartElement("beginDate");
						writeCalendarDate(writer, timePeriod.getStart());
					writer.writeEndElement();
					writer.writeStartElement("endDate");
						writeCalendarDate(writer, timePeriod.getStart());
					writer.writeEndElement();
				writer.writeEndElement();
			}



		writer.writeEndElement(); //termporalCoverage
	}


	private void writeCalendarDate(XMLStreamWriter writer, Temporal partial) throws XMLStreamException {
		//calendarDate
		String elementName = "calendarDate";
		//FIXME must be something like 37723
		String text = partial.toString();
		writeTextElement(writer, elementName, text);

	}

	private String nullSafe(Object object) {
		return object == null ? null : object.toString();
	}

	private void writePerson(XMLStreamWriter writer, InstitutionalMembership member) throws XMLStreamException {
		String elementName;
		String text;
		if (member == null){
			return ;
		}

		writer.writeStartElement("individualName");
		if (member.getPerson() != null){
			Person person = member.getPerson();

			elementName = "givenName";
			text = person.getFirstname();
			writeTextElement(writer, elementName, text);

			elementName = "surName";
			text = person.getLastname();
			writeTextElement(writer, elementName, text);

		}
		writer.writeEndElement();

		elementName = "organizationName";
		text = member.getInstitute()== null? null: member.getInstitute().getTitleCache();
		writeTextElement(writer, elementName, text);


		if (member.getPerson() != null && member.getPerson().getContact()!= null){
			Contact contact = member.getPerson().getContact();

			if (contact.getAddresses().size() > 0){
				writer.writeStartElement("address");

				//TODO empty
				Address address = contact.getAddresses().iterator().next();

				elementName = "deliveryPoint";
				text = address.getStreet();
				writeTextElement(writer, elementName, text);

				elementName = "city";
				text = address.getLocality();
				writeTextElement(writer, elementName, text);

				elementName = "administrativeArea";
				text = address.getRegion();
				writeTextElement(writer, elementName, text);

				elementName = "postalCode";
				text = address.getPostcode();
				writeTextElement(writer, elementName, text);

				elementName = "country";
				text = address.getCountry()== null? null: address.getCountry().getLabel();
				writeTextElement(writer, elementName, text);

				writer.writeEndElement();   //address
			}

			elementName = "phone";
			text = firstOfList(contact.getPhoneNumbers());
			writeTextElement(writer, elementName, text);

			elementName = "electronicMailAddress";
			text = firstOfList(contact.getEmailAddresses());
			writeTextElement(writer, elementName, text);

			elementName = "onlineUrl";
			text = firstOfList(contact.getPhoneNumbers());
			writeTextElement(writer, elementName, text);


		}
	}

	private String firstOfList(List<String> list) {
		if (list.size() > 0){
			return list.get(0);
		}else{
			return null;
		}
	}

	/**
	 * @param writer
	 * @param altIdentifier
	 * @param text
	 * @throws XMLStreamException
	 */
	private void writeTextElement(XMLStreamWriter writer, String elementName,
			String text) throws XMLStreamException {
		writer.writeStartElement(elementName);
		writer.writeCharacters(text);
		writer.writeEndElement();
	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoEml();
	}

}
