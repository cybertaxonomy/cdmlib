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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 20.04.2011
 */
public class DwcaImageExport extends DwcaDataExportBase {
    private static final long serialVersionUID = -4997807762779037215L;

    private static final Logger logger = Logger.getLogger(DwcaImageExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Image";
	protected static final String fileName = "images.txt";

    private DwcaMetaDataRecord metaRecord;

	/**
	 * Constructor
	 */
	public DwcaImageExport(DwcaTaxExportState state) {
		super();
		this.ioName = this.getClass().getSimpleName();
        metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
        state.addMetaRecord(metaRecord);
        file = DwcaTaxExportFile.IMAGE;
	}

    @Override
    protected void doInvoke(DwcaTaxExportState state){}

    /**
     * @param state
     * @param node
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    @Override
    protected void handleTaxonNode(DwcaTaxExportState state, TaxonNode node)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {
        try {
            DwcaTaxExportConfigurator config = state.getConfig();
            Taxon taxon = CdmBase.deproxy(node.getTaxon());
            Set<? extends DescriptionBase<?>> descriptions = taxon.getDescriptions();
            for (DescriptionBase<?> description : descriptions){
            	for (DescriptionElementBase o : description.getElements()){
            		DescriptionElementBase el = CdmBase.deproxy(o);
            		if (el.getMedia().size() > 0){
            			for (Media media: el.getMedia()){
            				for (MediaRepresentation repr : media.getRepresentations()){
            					for (MediaRepresentationPart part : repr.getParts()){
            						if (! state.recordExists(file, part)){
            							DwcaImageRecord record = new DwcaImageRecord(metaRecord, config);
            							handleMedia(state, record, media, repr, part, taxon);
            							PrintWriter writer = createPrintWriter(state, file);
            				            record.write(state, writer);
            							state.addExistingRecord(file,part);
            						}
            					}
            				}
            			}
            		}
            	}
            }

        } catch (Exception e) {
            String message = "Unexpected exception: " + e.getMessage();
            state.getResult().addException(e, message);
        }finally{
            flushWriter(state, file);
        }
    }




	private void handleMedia(DwcaTaxExportState state, DwcaImageRecord record, Media media, MediaRepresentation repr, MediaRepresentationPart part, Taxon taxon) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());
		if (part.getUri() == null){
			String message = "No uri available for media ("+media.getId()+"). URI is required field. Taxon: " + this.getTaxonLogString(taxon);
			state.getResult().addWarning(message);
		}
		record.setIdentifier(part.getUri());
		record.setTitle(media.getTitleCache());
		//TODO description if default language description is not available
		LanguageString description = media.getDescription(Language.DEFAULT());
		record.setDescription(description == null ? null: description.getText());
		//TODO missing
		record.setSpatial(null);
		//TODO missing
		record.setCoordinates(null);
		record.setFormat(repr.getMimeType());
		//FIXME missing ??
		record.setLicense(media.getRights());
		record.setCreated(media.getMediaCreated());
		record.setCreator(media.getArtist());
		//TODO missing
		record.setContributor(null);
		//TODO missing
		record.setPublisher(null);
		//TODO missing
		record.setAudience(null);
	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	public boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoImages();
	}

}
