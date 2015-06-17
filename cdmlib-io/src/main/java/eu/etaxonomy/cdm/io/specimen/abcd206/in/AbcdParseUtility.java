// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import java.net.URI;
import java.util.Date;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author pplitzner
 * @date 16.06.2015
 *
 */
public class AbcdParseUtility {

    private static final Logger logger = Logger.getLogger(AbcdParseUtility.class);


    public static URI parseFirstUri(NodeList nodeList){
        URI uri = null;
        String textContent = parseFirstTextContent(nodeList);
        if(textContent!=null){
            try {
                uri = URI.create(textContent);
            } catch (IllegalArgumentException e) {
                //nothing
            }
        }
        return uri;
    }

    public static String parseFirstTextContent(NodeList nodeList){
        return parseFirstTextContent(nodeList, true);
    }

    public static String parseFirstTextContent(NodeList nodeList, boolean cleanUpWhiteSpaces){
        String string = null;
        if(nodeList.getLength()>0){
            string = nodeList.item(0).getTextContent();
            if(cleanUpWhiteSpaces){
                string = string.replace("\n", "").replaceAll("( )+", " ").trim();
            }
        }
        return string;
    }

    public static Double parseFirstDouble(NodeList nodeList){
        if(nodeList.getLength()>0){
            return parseDouble(nodeList.item(0));
        }
        return null;
    }

    public static Double parseDouble(Node node){
        String message = "Could not parse double value for node " + node.getNodeName();
        Double doubleValue = null;
        try{
            String textContent = node.getTextContent();
            //remove 1000 dots
            textContent = textContent.replace(".","");
            //convert commmas
            textContent = textContent.replace(",",".");
            doubleValue = Double.parseDouble(textContent);
        } catch (NullPointerException npe){
            logger.error(message, npe);
        } catch (NumberFormatException nfe){
            logger.error(message, nfe);
        }
        return doubleValue;
    }

    public static DateTime parseFirstDateTime(NodeList nodeList) {
        DateTime dateTime = null;
        String textContent = parseFirstTextContent(nodeList);
        if(textContent!=null){
            dateTime = DateTime.parse(textContent);
        }
        return dateTime;
    }

    public static Date parseFirstDate(NodeList nodeList) {
        Date date = null;
        DateTime dateTime = parseFirstDateTime(nodeList);
        date = dateTime.toDate();
        return date;
    }

}
