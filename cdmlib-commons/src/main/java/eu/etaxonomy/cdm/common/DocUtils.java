package eu.etaxonomy.cdm.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.xhtml.XhtmlSinkFactory;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.ReaderFactory;

/**
 * The utility class which provides methods relating to documentation.
 *
 * @author c.mathew
 * @created 01-Aug-2012
 */

public class DocUtils {

    /**
     * Converts an apt file into html.
     *
     * @param aptFile apt file
     *
     * @return html as string or error message if exception
     *
     */
    public static String convertAptToHtml(File aptFile) {
        PlexusContainer container;
        try {
            container = new DefaultPlexusContainer();
        } catch (PlexusContainerException e) {
            return "Error in generating documentation : " + e.getMessage();
        }
        //FIXME : Plexus does not seem to work for looking up Sink Factory, so XhtmlSinkFactory is called directory
        //SinkFactory sinkFactory = (SinkFactory) container.lookup( SinkFactory.ROLE, "html" ); // Plexus lookup

        SinkFactory sinkFactory = new XhtmlSinkFactory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Sink sink;
        try {
            sink = sinkFactory.createSink(baos);
        } catch (IOException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        AptParser parser;
        try {
            parser = (AptParser)container.lookup(Parser.ROLE, "apt");
        } catch (ComponentLookupException e) {
            return "Error in generating documentation : " + e.getMessage();
        }
        Reader reader;
        try {
            reader = ReaderFactory.newReader( aptFile, "UTF-8" );
        } catch (FileNotFoundException e) {
            return "Error in generating documentation : " + e.getMessage();
        } catch (UnsupportedEncodingException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        try {
            parser.parse( reader, sink );
        } catch (ParseException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        return baos.toString();
    }

    /**
     * Converts an apt input stream into html.
     *
     * @param aptInputStream apt input stream
     *
     * @return html as string or error message if exception
     *
     */
    public static String convertAptToHtml(InputStream aptInputStream) {
        PlexusContainer container;
        try {
            container = new DefaultPlexusContainer();
        } catch (PlexusContainerException e) {
            return "Error in generating documentation : " + e.getMessage();
        }
        //FIXME : Plexus does not seem to work for looking up Sink Factory, so XhtmlSinkFactory is called directory
        //SinkFactory sinkFactory = (SinkFactory) container.lookup( SinkFactory.ROLE, "html" ); // Plexus lookup

        SinkFactory sinkFactory = new XhtmlSinkFactory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Sink sink;
        try {
            sink = sinkFactory.createSink(baos);
        } catch (IOException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        AptParser parser;
        try {
            parser = (AptParser)container.lookup(Parser.ROLE, "apt");
        } catch (ComponentLookupException e) {
            return "Error in generating documentation : " + e.getMessage();
        }
        Reader reader;
        try {
            reader = ReaderFactory.newReader( aptInputStream, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        try {
            parser.parse( reader, sink );
        } catch (ParseException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        return baos.toString();
    }

}
