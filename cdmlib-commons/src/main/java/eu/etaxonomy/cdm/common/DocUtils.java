package eu.etaxonomy.cdm.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.xhtml5.Xhtml5SinkFactory;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;

/**
 * The utility class which provides methods relating to documentation.
 *
 * @author c.mathew
 * @since 01-Aug-2012
 */
public class DocUtils {

    /**
     * Converts an apt file into html.
     *
     * @param aptFile apt file
     *
     * @return html as string or error message if exception
     */
    public static String convertAptToHtml(File aptFile) {

        SinkFactory sinkFactory = new Xhtml5SinkFactory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Reader reader = Files.newBufferedReader(aptFile.toPath(), StandardCharsets.UTF_8);
            Sink sink = sinkFactory.createSink(baos)) {

            AptParser parser = new AptParser();

            parser.parse( reader, sink );

            sink.flush();
        } catch (IOException | ParseException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        return baos.toString(StandardCharsets.UTF_8);
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

        SinkFactory sinkFactory = new Xhtml5SinkFactory();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (Reader reader = new InputStreamReader(aptInputStream, StandardCharsets.UTF_8);
             Sink sink = sinkFactory.createSink(baos)) {

            AptParser parser = new AptParser();

            parser.parse(reader, sink);

            sink.flush();

        } catch (IOException | ParseException e) {
            return "Error in generating documentation : " + e.getMessage();
        }

        // Modernes HTML5-Ergebnis zurückgeben
        return baos.toString(StandardCharsets.UTF_8);
    }

}
