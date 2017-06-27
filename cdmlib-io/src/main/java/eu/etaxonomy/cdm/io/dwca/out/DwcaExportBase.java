/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IOriginalSource;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;

/**
 * @author a.mueller
 * @date 18.04.2011
 *
 */
public abstract class DwcaExportBase
            extends CdmExportBase<DwcaTaxExportConfigurator, DwcaTaxExportState, IExportTransformer>
            implements ICdmExport<DwcaTaxExportConfigurator, DwcaTaxExportState>{

    private static final long serialVersionUID = -3214410418410044139L;

    private static final Logger logger = Logger.getLogger(DwcaExportBase.class);

    protected static final boolean IS_CORE = true;


    @Override
    public int countSteps(DwcaTaxExportState state) {
        //FIXME count without initialization
        List<TaxonNode> allNodes =  allNodes(state);
        return allNodes.size();
    }

    /**
     * Returns the list of {@link TaxonNode taxon nodes} that correspond to the
     * given filter criteria (e.g. subtreeUUids). If no filter is given
     * all taxon nodes of all classifications are returned. If the list has been
     * computed before it is taken from the state cache. Nodes that do not have
     * a taxon attached are not returned. Instead a warning is given that the node is
     * ommitted (empty taxon nodes should not but do exist in CDM databases).
     * <BR>
     * Preliminary implementation. Better implement API method for this.
     */
    //TODO unify with similar methods for other exports
    protected List<TaxonNode> allNodes(DwcaTaxExportState state) {

        Set<UUID> subtreeUuidSet = state.getConfig().getSubtreeUuids();
        if (subtreeUuidSet == null){
            subtreeUuidSet = new HashSet<>();
        }
        //handle empty list as no filter defined
        if (subtreeUuidSet.isEmpty()){
            List<Classification> classificationList = getClassificationService().list(Classification.class, null, 0, null, null);
            for (Classification classification : classificationList){
                subtreeUuidSet.add(classification.getRootNode().getUuid());
            }
        }

        //TODO memory critical to store ALL node
        if (state.getAllNodes().isEmpty()){
            makeAllNodes(state, subtreeUuidSet);
        }
        List<TaxonNode> allNodes = state.getAllNodes();
        return allNodes;
    }

    private void makeAllNodes(DwcaTaxExportState state, Set<UUID> subtreeSet) {

        boolean doSynonyms = false;
        boolean recursive = true;
        Set<UUID> uuidSet = new HashSet<>();

        for (UUID subtreeUuid : subtreeSet){
            uuidSet.add(subtreeUuid);
            List<TaxonNodeDto> records = getTaxonNodeService().pageChildNodesDTOs(subtreeUuid,
                    recursive, doSynonyms, null, null, null).getRecords();
            for (TaxonNodeDto dto : records){
                uuidSet.add(dto.getUuid());
            }
        }
        List<TaxonNode> allNodes =  getTaxonNodeService().find(uuidSet);

        List<TaxonNode> result = new ArrayList<>();
        for (TaxonNode node : allNodes){
            if(node.getParent()== null){  //root (or invalid) node
                continue;
            }
            node = CdmBase.deproxy(node);
            Taxon taxon = CdmBase.deproxy(node.getTaxon());
            if (taxon == null){
                String message = "There is a taxon node without taxon. id=" + node.getId();
                state.getResult().addWarning(message);
                continue;
            }
            result.add(node);
        }
        state.setAllNodes(result);
    }


    /**
     * Creates the locationId, locality, countryCode triple
     * @param record
     * @param area
     */
    protected void handleArea(IDwcaAreaRecord record, NamedArea area, TaxonBase<?> taxon, boolean required) {
        if (area != null){
            record.setLocationId(area);
            record.setLocality(area.getLabel());
            if (area.isInstanceOf(Country.class)){
                Country country = CdmBase.deproxy(area, Country.class);
                record.setCountryCode(country.getIso3166_A2());
            }
        }else{
            if (required){
                String message = "Description requires area but area does not exist for taxon " + getTaxonLogString(taxon);
                logger.warn(message);
            }
        }
    }


    protected String getTaxonLogString(TaxonBase<?> taxon) {
        return taxon.getTitleCache() + "(" + taxon.getId() + ")";
    }


    protected String getSources(ISourceable<?> sourceable, DwcaTaxExportConfigurator config) {
        String result = "";
        for (IOriginalSource<?> source: sourceable.getSources()){
            if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
            }
        }
        return result;
    }

    protected String getSources3(ISourceable<?> sourceable, DwcaTaxExportConfigurator config) {
        String result = "";
        for (IOriginalSource<?> source: sourceable.getSources()){
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
        }
        return result;
    }

    protected String getSources2(Set<DescriptionElementSource> set, DwcaTaxExportConfigurator config) {
        String result = "";
        for(DescriptionElementSource source: set){
            if (StringUtils.isBlank(source.getIdInSource())){//idInSource indicates that this source is only data provenance, may be changed in future
                if (source.getCitation() != null){
                    String ref = source.getCitation().getTitleCache();
                    result = CdmUtils.concat(config.getSetSeparator(), result, ref);
                }
            }
        }
        return result;
    }


    /**
     * @param config
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     */
    protected FileOutputStream createFileOutputStream(DwcaTaxExportConfigurator config, String thisFileName) throws IOException, FileNotFoundException {
        String filePath = config.getDestinationNameString();
        String fileName = filePath + File.separatorChar + thisFileName;
        File f = new File(fileName);
        if (!f.exists()){
            f.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(f);
        return fos;
    }


    protected XMLStreamWriter createXmlStreamWriter(DwcaTaxExportState state, DwcaTaxOutputFile table)
            throws IOException, FileNotFoundException, XMLStreamException {

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        OutputStream os;
        boolean useZip = state.isZip();
        if (useZip){
            os = state.getZipStream(table.getTableName());
        }else if(state.getConfig().getDestination() != null){
            os = createFileOutputStream(state.getConfig(), table.getTableName());
        }else{
            os = new ByteArrayOutputStream();
            state.getProcessor().put(table, (ByteArrayOutputStream)os);
        }
        XMLStreamWriter  writer = factory.createXMLStreamWriter(os);
        return writer;
    }


    /**
     * @param writer2
     * @param coreTaxFileName
     * @param config
     * @return
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    protected PrintWriter createPrintWriter(DwcaTaxExportState state, DwcaTaxOutputFile file)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        if (state.getWriter(file) == null){

            boolean useZip = state.isZip();
            OutputStream os;
            if (useZip){
                os = state.getZipStream(file.getTableName());
            }else if(state.getConfig().getDestination() != null){
                os = createFileOutputStream(state.getConfig(), file.getTableName());
            }else{
                os = new ByteArrayOutputStream();
                state.getProcessor().put(file, (ByteArrayOutputStream)os);
            }
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF8"), true);
            state.putWriter(file, writer);
        }
        return state.getWriter(file);
    }


    /**
     * flushes the writer for the according file if exists.
     */
    protected void flushWriter(DwcaTaxExportState state, DwcaTaxOutputFile file) {
        if (state.getWriter(file) != null){
            state.getWriter(file).flush();
        }
    }


    /**
     * Closes the writer
     * @param file
     * @param state
     */
    protected void closeWriter(DwcaTaxOutputFile file, DwcaTaxExportState state) {
        PrintWriter writer = state.getWriter(file);
        if (writer != null && state.isZip() == false){
            writer.close();
        }
    }



    /**
     * Closes the writer.
     * Note: XMLStreamWriter does not close the underlying stream.
     * @param writer
     * @param state
     */
    protected void closeWriter(XMLStreamWriter writer, DwcaTaxExportState state) {
        if (writer != null && state.isZip() == false){
            try {
                writer.close();
            } catch (XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
