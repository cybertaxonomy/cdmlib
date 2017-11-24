/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.media.in;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.media.ImageInfo;
import eu.etaxonomy.cdm.io.common.utils.ImportDeduplicationHelper;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.media.in.MediaExcelImportConfigurator.MediaTitleEnum;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.media.RightsType;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @date 30.10.2017
 */
@Component
public class MediaExcelImport
        extends ExcelImportBase<MediaExcelImportState, MediaExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = -428449749189166794L;

    private static final String COL_TAXON_UUID = "taxonUuid";
    private static final String COL_NAME_CACHE = "nameCache";
    private static final String COL_NAME_TITLE = "nameTitle";
    private static final String COL_TAXON_TITLE = "taxonTitle";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_TITLE = "title";
    private static final String COL_COPYRIGHT = "copyright";
    private static final String COL_ARTIST = "artist";
    private static final String COL_DATE = "date";

    private ImportDeduplicationHelper<MediaExcelImportState> deduplicationHelper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void analyzeRecord(HashMap<String, String> record, MediaExcelImportState state) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void firstPass(MediaExcelImportState state) {
        HashMap<String, String> record = state.getOriginalRecord();
        String line = "row " + state.getCurrentLine() + ": ";
        String linePure = "row " + state.getCurrentLine();
        System.out.println(linePure);

        //taxon
        Taxon taxon = getTaxonByCdmId(state, COL_TAXON_UUID,
                COL_NAME_CACHE, COL_NAME_TITLE, COL_TAXON_TITLE,
                Taxon.class, linePure);

        //media
        Media media = Media.NewInstance();

        //description
        String description = record.get(COL_DESCRIPTION);
        if (isNotBlank(description)){
            Language descriptionLanguage = state.getConfig().getDescriptionLanguage();
            descriptionLanguage = descriptionLanguage == null? Language.UNKNOWN_LANGUAGE(): descriptionLanguage;
            media.putDescription(descriptionLanguage, description);
        }

        //title
        String title = record.get(COL_TITLE);
        if (isBlank(title)){
            title = makeTitle(state, taxon, line);
        }
        if (isNotBlank(title)){
            Language titleLanguage = state.getConfig().getTitleLanguage();
            titleLanguage = titleLanguage == null? Language.UNKNOWN_LANGUAGE(): titleLanguage;
            media.putTitle(titleLanguage, title);
        }

        //copyright
        String copyright = record.get(COL_COPYRIGHT);
        if (isNotBlank(copyright)){
            AgentBase<?> agent = makePerson(state, copyright, line);
            Rights right = Rights.NewInstance(RightsType.COPYRIGHT(), agent);
            right = getDeduplicationHelper(state).getExistingCopyright(state, right);
            media.addRights(right);
        }

        //artist
        String artistStr = record.get(COL_ARTIST);
        if (isNotBlank(artistStr)){
            AgentBase<?> artist = makePerson(state, artistStr, line);
            media.setArtist(artist);
        }

        //date
        String dateStr = record.get(COL_DATE);
        if (isNotBlank(artistStr)){
            TimePeriod timePeriod = TimePeriodParser.parseString(dateStr);
            if (timePeriod.getFreeText()!=  null){
                String message = "Date could not be parsed: %s";
                message = String.format(message, dateStr);
                state.getResult().addWarning(message, null, line);
            }
            if (timePeriod.getEnd() !=  null){
                String message = "Date is a period with an end date. Periods are currently not yet supported: %s";
                message = String.format(message, dateStr);
                state.getResult().addWarning(message, null, line);
            }

            Partial start = timePeriod.getStart();
            DateTime dateTime = toDateTime(state, start, dateStr, line);
            media.setMediaCreated(dateTime);
        }

        //URLs
        List<URI> uris = getUrls(state, line);
        for (URI uri : uris){
            handleUri(state, uri, media, line);

        }


//        for (URI baseUrl : state.getConfig().getBaseUrls()){
//            if (!baseUrl.toString().endsWith("/")){
//                baseUrl = URI.create(baseUrl.toString() +  "/"); //is this always correct?
//            }
//            String url = baseUrl + fileName;
//            readImage
//        }

        //source
        String id = null;
        String idNamespace = null;
        Reference reference = getSourceReference(state);
        media.addImportSource(id, idNamespace, reference, linePure);

        if (taxon == null){
            return;
        }

        String taxonTitle = taxon.getName() == null ? taxon.getTitleCache() :
            isBlank(taxon.getName().getNameCache()) ? taxon.getName().getTitleCache():
                taxon.getName().getNameCache();
        TaxonDescription taxonDescription = taxon.getOrCreateImageGallery(taxonTitle);
        TextData textData = taxonDescription.getOrCreateImageTextData();
        textData.addMedia(media);
    }



    /**
     * @param state
     * @param taxon
     * @param line
     * @return
     */
    private String makeTitle(MediaExcelImportState state, Taxon taxon, String line) {
        MediaTitleEnum mediaTitleType = state.getConfig().getMediaTitle();
        if (mediaTitleType == null || mediaTitleType == MediaTitleEnum.NONE){
            return null;
        }else if(mediaTitleType == MediaTitleEnum.FILE_NAME){
            URI source = state.getConfig().getSource();
            if (source != null){
                String result = source.toString();
                while (result.endsWith("/")){
                    result = result.substring(0, result.length() - 1);
                }
                while (result.contains("/")){
                    result = result.substring(result.lastIndexOf("/"));
                }
                return result;
            }else{
               mediaTitleType = MediaTitleEnum.NAME_TITLE_CACHE;
            }
        }
        if (taxon == null){
            return null;
        }
        if (taxon.getName() == null || mediaTitleType == MediaTitleEnum.TAXON_TITLE_CACHE){
            return taxon.getTitleCache();
        }else{
            TaxonName name = taxon.getName();
            if (mediaTitleType == MediaTitleEnum.NAME_TITLE_CACHE || isBlank(name.getNameCache())){
                return name.getTitleCache();
            }else{
                return name.getNameCache();
            }
        }
    }

    /**
     * @param start
     * @return
     */
    private DateTime toDateTime(MediaExcelImportState state, Partial partial, String dateStr, String line) {
        if (partial == null){
            return null;
        }
        List<DateTimeFieldType> typeList = Arrays.asList(partial.getFieldTypes());
        if ( typeList.contains(DateTimeFieldType.year())
                && typeList.contains(DateTimeFieldType.monthOfYear())
                && typeList.contains(DateTimeFieldType.dayOfMonth())
                ){
            DateTime result = partial.toDateTime(DateTime.now());
            return result;
        }else{
            String message = "Date time does not include year, month and day information. Currently all these 3 parts are required: %s";
            message = String.format(message, dateStr);
            state.getResult().addWarning(message, null, line);
            return null;
        }
    }

    /**
     * @param state
     * @param uri
     * @param media
     * @param line
     */
    private void handleUri(MediaExcelImportState state, URI uri, Media media, String line) {
            ImageInfo imageInfo = null;
            try {
                if (state.getConfig().isReadMediaData()){
                    imageInfo = ImageInfo.NewInstance(uri, 0);
                }
            } catch (Exception e) {
                String message = "An error occurred when trying to read image meta data for %s. Image was created but without metadata.";
                message = String.format(message, uri.toString());
                state.getResult().addException(e, message, null, line);
            }
            ImageFile imageFile = ImageFile.NewInstance(uri, null, imageInfo);

            MediaRepresentation representation = MediaRepresentation.NewInstance();

            if(imageInfo != null){
                representation.setMimeType(imageInfo.getMimeType());
                representation.setSuffix(imageInfo.getSuffix());
            }
            representation.addRepresentationPart(imageFile);
            media.addRepresentation(representation);
    }

    /**
     * @param state
     * @return
     */
    private List<URI> getUrls(MediaExcelImportState state, String line) {
        List<URI> list = new ArrayList<>();
        HashMap<String, String> record = state.getOriginalRecord();
        for (String str : record.keySet()){
            if (str.equals("url") || str.matches("url_size\\d+") ){
                String url = record.get(str);
                try {
                    url = url.replace(" ", "%20");  //replace whitespace
                    URI uri = URI.create(url);
                    list.add(uri);
                } catch (Exception e) {
                    String msg = "Incorrect url " + url;
                    state.getResult().addError(msg, e, null, line);
                }
            }
        }

        return list;
    }

    /**
     * @param state
     * @return
     */
    private ImportDeduplicationHelper<MediaExcelImportState> getDeduplicationHelper(MediaExcelImportState state) {
        if (this.deduplicationHelper == null){
            this.deduplicationHelper = ImportDeduplicationHelper.NewInstance(this, state);
        }
        return deduplicationHelper;
    }

    private Person makePerson(MediaExcelImportState state, String artist, String line) {
        Person person = Person.NewInstance();
        artist = artist.trim();

        String regExAbbrev = "((?:[A-Z]\\. ?)+)([A-Z][a-z\\-\u00E4\u00F6\u00FC]+)";
        Matcher matcherAbbrev = Pattern.compile(regExAbbrev).matcher(artist);

        String regExFull = "([A-Z][a-z\\-\u00E4\u00F6\u00FC]+\\s)([A-Z][a-z\\-\u00E4\u00F6\u00FC]+)";
        Matcher matcherFull = Pattern.compile(regExFull).matcher(artist);

        if (matcherAbbrev.matches()){
            person.setFirstname(matcherAbbrev.group(1).trim());
            person.setLastname(matcherAbbrev.group(2).trim());
        }else if (matcherFull.matches()){
            person.setFirstname(matcherFull.group(1).trim());
            person.setLastname(matcherFull.group(2).trim());
        }else{
            person.setTitleCache(artist, true);
            String message = "A name of a person can not be atomized: %s";
            message = String.format(message, artist);
            state.getResult().addWarning(message, null, line);

        }

        Person result = (Person)getDeduplicationHelper(state).getExistingAuthor(null, person);
        return person;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void secondPass(MediaExcelImportState state) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(MediaExcelImportState state) {
        return false;
    }
}