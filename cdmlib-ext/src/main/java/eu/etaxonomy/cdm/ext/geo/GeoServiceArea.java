// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.xml.sax.InputSource;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * Holds all values to map an NamedArea to a geo service area
 * @author a.mueller
 * @date 11.08.2011
 *
 */
public class GeoServiceArea {
	
	private static final String VALUE = "value";
	private static final String FIELD = "field";
	private static final String LAYER = "layer";
	private static final String AREA = "area";
	private static final String MAP_SERVICE_NAMESPACE = "http://www.etaxonomy.eu/cdm";
	private static final String MAP_SERVICE = "mapService";
	TreeSet<SubArea> subAreas = new TreeSet<SubArea>(); 
	
	private class SubArea implements Comparable<SubArea>{
		private String layer;
		private String field;
		private String value;
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int hash;
			hash = 236435;
			hash += layer != null ? layer.hashCode() * 29 : 32;
			hash += field != null ? field.hashCode() * 31 : 32;
			hash += value != null ? value.hashCode() * 37 : 32;
			return hash;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object otherArea) {
			if (! (otherArea instanceof SubArea)){
				return false;
			}
			SubArea subArea = (SubArea)otherArea;
			if (CdmUtils.nullSafeEqual(layer, subArea.layer) 
					&& CdmUtils.nullSafeEqual(field, subArea.field) 
					&& CdmUtils.nullSafeEqual(value, subArea.value)){
				return true;
			}else{
				return false;
			}
		}
		@Override
		public int compareTo(SubArea otherArea) {
			int compareLayer = CdmUtils.Nz(this.layer).compareToIgnoreCase(CdmUtils.Nz(otherArea.layer));
			int compareField = CdmUtils.Nz(this.field).compareToIgnoreCase(CdmUtils.Nz(otherArea.field));
			int compareValue = CdmUtils.Nz(this.value).compareToIgnoreCase(CdmUtils.Nz(otherArea.value));
			
			if (compareLayer != 0){
				return compareLayer;
			}else if (compareField != 0 ){
				return compareField;
			}else {
				return compareValue;
			}
		}
		
		
	}
	
	public void add(String layer, String field, String value){
		SubArea newArea = new SubArea();
		newArea.layer = layer;
		newArea.field = field;
		newArea.value = value;
		subAreas.add(newArea);
	}
	
	public Map<String, Map<String, List<String>>> getAreas(){
		Map<String, Map<String, List<String>>> result = new HashMap<String, Map<String,List<String>>>();
				
		for (SubArea area : subAreas){
			//layer
			Map<String, List<String>> layer = result.get(area.layer);
			if (layer == null ){
				layer = new HashMap<String, List<String>>();
				result.put(area.layer, layer);
			}
			//field
			List<String> field = layer.get(area.field);
			if (field == null ){
				field = new ArrayList<String>();
				layer.put(area.field, field);
			}
			//value
			if (! field.contains(area.value)){
				field.add(area.value);
			}

		}
		return result;
	}
	
	public static GeoServiceArea valueOf (String xml){
//		StringReader reader = new StringReader (xml);
//		(new InputSource(reader));
//		InputStream is = new java.io.StringBufferInputStream(xml);
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		GeoServiceArea result = new GeoServiceArea();
		
		try {
			Element root = XmlHelp.getRoot(is);
			if (! root.getName().equals(MAP_SERVICE) || ! root.getNamespace().getURI().equals(MAP_SERVICE_NAMESPACE)   ){
				return null;
			}else{
				//TODO schema validation
				
				Namespace ns = root.getNamespace();
				List<Element> elAreas = root.getChildren(AREA, ns);
				for (Element elArea : elAreas){
					Element layer = elArea.getChild(LAYER, ns);
					Element field = elArea.getChild(FIELD, ns);
					//TODO multiple values
					List<Element> values = elArea.getChildren(VALUE, ns);
					for (Element value : values){
						result.add(layer.getTextTrim(), field.getTextTrim(), value.getTextTrim());
					}
				}
				return result;	
			}
				
			
			
		} catch (JDOMException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String toXml() throws XMLStreamException{
		XMLStreamWriter writer = null;
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			StringWriter stringWriter = new StringWriter();
			writer = factory.createXMLStreamWriter(stringWriter);
			
			String rootNamespace = MAP_SERVICE_NAMESPACE;
			String rootName = MAP_SERVICE;
			
			
			// create header 
			writer.writeStartDocument(); 
			writer.setDefaultNamespace(rootNamespace);
			
				// create root element 
				writer.writeStartElement(rootName);
				
				writer.writeNamespace(null, rootNamespace);
				writer.writeAttribute("type", "editMapService");
				
				writeAreas(writer);
				
				writer.writeEndElement(); 
			writer.writeEndDocument(); 
				
			return stringWriter.getBuffer().toString();
	}

	private void writeAreas(XMLStreamWriter writer) throws XMLStreamException {
		Map<String, Map<String, List<String>>> areaMap = getAreas();
		//TODO multiple
		for (String layerKey : areaMap.keySet()){
			Map<String, List<String>> layer = areaMap.get(layerKey);
			for (String fieldKey: layer.keySet()){
				List<String> field = layer.get(fieldKey);
				//area
				writer.writeStartElement(AREA);
				//layer
				writer.writeStartElement(LAYER);
				writer.writeCharacters(layerKey);
				writer.writeEndElement();
				//field
				writer.writeStartElement(FIELD);
				writer.writeCharacters(fieldKey);
				writer.writeEndElement();
				//value
				for (String value : field){
					writer.writeStartElement(VALUE);
					writer.writeCharacters(value);
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		
	}

	/**
	 * Transforms the area to an geoservice area
	 * @param area the NamedArea
	 * @param appConfig for future use
	 * @return
	 */
	public static GeoServiceArea valueOf(NamedArea area, ICdmApplicationConfiguration appConfig) {
		for (Annotation annotation : area.getAnnotations()){
			if (AnnotationType.TECHNICAL().equals(annotation.getAnnotationType())){
				GeoServiceArea areas = valueOf(annotation.getText());
				return areas;
			}
		}
		
		return null;
	}

	public static void set(NamedArea areaBangka, GeoServiceArea geoServiceArea) throws XMLStreamException {
		AnnotationType type = AnnotationType.TECHNICAL();
		Annotation annotation = Annotation.NewInstance(geoServiceArea.toXml(), type, Language.DEFAULT());
		areaBangka.addAnnotation(annotation);
	}

	public int size() {
		return this.subAreas.size();
	}

}
