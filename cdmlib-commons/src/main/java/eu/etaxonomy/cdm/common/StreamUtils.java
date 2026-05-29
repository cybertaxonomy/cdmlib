/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author a.kohlbecker
 * @since 16.12.2010
 */
public class StreamUtils {

	private static final Logger logger = LogManager.getLogger();

	private static final int BUFFER_SIZE = 4096;

	/**
	 * Replaces each substring of this stream that matches the literal search sequence with the specified literal replace sequence.
	 * The replacement proceeds from the beginning of the stream to the end, for example, replacing "aa" with "b" in the string "aaa" will result in "ba" rather than "ab".
	 *
	 * @param stream
	 * @param search The sequence of char values to be replaced
	 * @param replace The replacement sequence of char values
	 * @return
	 * @throws IOException
	 *
	 */
	public static InputStream streamReplace(InputStream inputStream, String search, String replace) throws IOException {
        // 1. Read input stream with correct character set
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // 2. convert to a single String and replace regex
            String content = reader.lines().collect(Collectors.joining("\n"));
            String modifiedContent = content.replace(search, replace);

            // 3. return modified content as InputStream
            return new ByteArrayInputStream(modifiedContent.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

	}

    public InputStream streamReplaceAll(InputStream inputStream, String regex, String replacement) throws IOException {
        // 1. Read input stream with correct character set
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            // 2. convert to a single String and replace regex
            String content = reader.lines().collect(Collectors.joining("\n"));
            String modifiedContent = content.replaceAll(regex, replacement);

            // 3. return modified content as InputStream
            return new ByteArrayInputStream(modifiedContent.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	public static String readToString(InputStream stream) throws IOException {
		InputStreamReader reader = new InputStreamReader(stream);
		StringBuilder strBuilder = new StringBuilder();

		char[] cbuf = new char[1024];
		int charsRead = -1;
		while ((charsRead = reader.read(cbuf)) > -1){
			strBuilder.append(cbuf, 0, charsRead);
		}
		return strBuilder.toString();
	}

	public static void downloadFile(URL url, String saveDir)
            throws IOException {

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = url.getFile().toString().substring(url.getFile().lastIndexOf("/") + 1,
                        url.getFile().length());
            }

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();


        } else {
           logger.error("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }


}
