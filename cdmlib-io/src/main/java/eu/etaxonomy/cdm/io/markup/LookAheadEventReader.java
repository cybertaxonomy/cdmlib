/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.markup;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 \* @since 28.06.2011
 */
public class LookAheadEventReader implements XMLEventReader {
	private static final Logger logger = Logger.getLogger(LookAheadEventReader.class);

	private XMLEventReader reader;

	private List<XMLEvent> cachedEvents = new ArrayList<>();

	private List<XMLEvent> usedEvents = new ArrayList<>();

	public LookAheadEventReader(StartElement startElement, XMLEventReader parentReader) throws XMLStreamException{
		int depth = 0;
		this.reader = parentReader;
		XMLEvent next = parentReader.nextEvent();
		while (! next.isEndElement() ||
				 ! next.asEndElement().getName().equals(startElement.getName()) ||
				 depth > 0){
			cachedEvents.add(next);
			if (next.isStartElement() && next.asStartElement().getName().equals(startElement.getName())){
				depth++;
			}else if (next.isEndElement() && next.asEndElement().getName().equals(startElement.getName())){
				depth--;
			}
			next = reader.nextEvent();
		}
		cachedEvents.add(next);
	}

	@Override
	public Object next() {
		throw new RuntimeException("Iterator methods are not supported by this EventReader");
	}


	@Override
	public void remove() {
		throw new RuntimeException("Iterator methods are not supported by this EventReader");
	}


	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		if (! cachedEvents.isEmpty()){
			XMLEvent result = cachedEvents.get(0);
			XMLEvent removedEvent = cachedEvents.remove(0);
			usedEvents.add(removedEvent);
			return result;
		}else{
			logger.warn("LookAheadReader reads uncached data. This is not ");
			throw new IllegalStateException("LookAheadReader reads uncached data. This is not ");
//			return reader.nextEvent();
		}
	}


	@Override
	public boolean hasNext() {
		return reader.hasNext();
	}


	@Override
	public XMLEvent peek() throws XMLStreamException {
		return reader.peek();
	}

	@Override
	public String getElementText() throws XMLStreamException {
		return reader.getElementText();
	}

	@Override
	public XMLEvent nextTag() throws XMLStreamException {
		return reader.nextTag();
	}

	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return reader.getProperty(name);
	}

	@Override
	public void close() throws XMLStreamException {
		reader.close();
	}

	public boolean previousWasEnd(String name){
		return hasStartElement(name, -1, 0);
	}

	public boolean nextIsStart(String name){
		return hasStartElement(name, 0, 1);
	}


	public boolean hasStartElement(String name, int start, int end){
		if (start < 0 ){
			start = 0;
		}
		if (end > cachedEvents.size()){
			end = cachedEvents.size();
		}
		for (int i = start; i < end; i++){

			XMLEvent ev = cachedEvents.get(i);
			if (ev.isStartElement() && ev.asStartElement().getName().getLocalPart().equals(name)){
				return true;
			}
		}
		return false;
	}

	public boolean hasEndElement(String name, int start, int end){
		if (end > 0 ){
			end = 0;
		}
		if (-start > usedEvents.size()){
			start = -cachedEvents.size();
		}
		for (int i = start; i < end; i++){

			XMLEvent ev = cachedEvents.get( cachedEvents.size() + i );
			if (ev.isEndElement() && ev.asEndElement().getName().getLocalPart().equals(name)){
				return true;
			}
		}
		return false;
	}

	public List<XMLEvent> getCachedEvents(boolean onlyCharData){
	    List<XMLEvent> result = new ArrayList<>();
	    for (XMLEvent event: cachedEvents){
	        if (!onlyCharData || event.isCharacters()){
	            result.add(event);
	        }
	    }
	    return result;
	}

}
