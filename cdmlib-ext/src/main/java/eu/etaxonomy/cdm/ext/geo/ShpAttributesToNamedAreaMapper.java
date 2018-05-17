/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.geo;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Reads csv data containing the attributes from a shape file and adds the
 * shapefile data to each area in the given set of {@link NamedAreas}. The way
 * this data it attached to the areas is specific to the
 * {@link IGeoServiceAreaMapping} implementation. It is recommended to create
 * csv file directly from the original shape file by making use of the
 * {@code org2ogr} command which is contained in the <a
 * href="http://www.gdal.org/ogr2ogr.html">gdal</a> tools:
 *
 * <pre>
 * ogr2ogr -f csv out.csv input_shape_file.shp
 * </pre>
 *
 *
 * @author a.kohlbecker
 * @since Oct 18, 2013
 *
 */
public class ShpAttributesToNamedAreaMapper {

    public static final Logger logger = Logger.getLogger(ShpAttributesToNamedAreaMapper.class);

    private static final char COMMA = ',';

    private final Set<NamedArea> areas;

    private final IGeoServiceAreaMapping areaMapping;

    /**
     * maps the idSearchFields supplied to the {@link #readCsv(File, List)} method to the
     * column index
     */
    private Map<String,Integer> searchColumnMap = null;

    public ShpAttributesToNamedAreaMapper(Set<NamedArea> areas, IGeoServiceAreaMapping areaMapping) {
        this.areas = areas;
        this.areaMapping = areaMapping;
    }

    public ShpAttributesToNamedAreaMapper(TermVocabulary<NamedArea> areaVocabulary, IGeoServiceAreaMapping areaMapping) {
        this.areas = areaVocabulary.getTerms();
        this.areaMapping = areaMapping;
    }

    /**
     * @param reader A reader of the csv data
     * @param idSearchFields
     *            An ordered list column names in the the csv file to be
     *            imported. These columns will be used to search for the
     *            {@link NamedArea#getIdInVocabulary() IdInVocabulary} of each
     *            area
     * @param wmsLayerName
     * @return the resulting table of the import, also together with diagnostic
     *         messages per NamedArea (id not found)
     * @throws IOException
     */
    public Map<NamedArea, String> readCsv(Reader reader, List<String> idSearchFields, String wmsLayerName) throws IOException {

        logger.setLevel(Level.DEBUG);

        Map<NamedArea, String> resultMap = new HashMap<>(areas.size());

        CSVReader csvReader = new CSVReader(reader, COMMA);

        // read header row and prepare the searchColumnMap
        String[] headerRow = csvReader.readNext();
        searchColumnMap = new HashMap<>();
        for(String colName : idSearchFields){
            int idx = ArrayUtils.indexOf(headerRow, colName);
            if(idx > -1){
                searchColumnMap.put(colName, idx);
            } else {
                logger.debug("no header row found for " + colName);
            }
        }

        // read the rest of the file
        List<String[]> data = csvReader.readAll();
        csvReader.close();
        csvReader = null; // release memory

        String matchIdCode;
        String matchColName;
        for(NamedArea a : areas){

            if(a.getIdInVocabulary() == null){
                String message = "has no IdInVocabulary";
                resultMap.put(a, message);
                logger.warn(a.getTitleCache() + " " + message);
                continue;
            }
            matchIdCode = null;
            matchColName = null;
            // search in each of the columns until found
            for(String colName : searchColumnMap.keySet()){

                int idx = searchColumnMap.get(colName);

                for(String[] row : data){
                    String fieldData = row[idx].trim();
                    if(a.getIdInVocabulary().equals(fieldData)){
                        // FOUND!
                        matchIdCode = fieldData;
                        break;
                    }
                }

                if(matchIdCode != null){
                    // no need to search in next column
                    matchColName = colName;
                    break;
                }

            } // END loop columns

            if(matchIdCode != null){
                //TODO need to clear the area mapping since the mapping impl can not distinguish multiple layers
                //     see http://dev.e-taxonomy.eu/trac/ticket/4263
                areaMapping.clear(a);

                GeoServiceArea geoServiceArea;
                geoServiceArea = areaMapping.valueOf(a);
                if(geoServiceArea == null){
                    geoServiceArea = new GeoServiceArea();
                }
                geoServiceArea.add(wmsLayerName, matchColName, matchIdCode);
                areaMapping.set(a, geoServiceArea );

                String message = matchColName + ": " + matchIdCode;
                resultMap.put(a, message);
                if(logger.isDebugEnabled()){
                    logger.debug(a.getIdInVocabulary() + ": " + message);
                }
            } else {
                String message = "no match for " + a.getIdInVocabulary();
                resultMap.put(a, message);
                logger.warn(message);
            }


        } // END of areas loop
        return resultMap;
    }

}
