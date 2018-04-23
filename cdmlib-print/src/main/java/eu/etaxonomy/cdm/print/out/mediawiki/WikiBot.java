/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.print.out.mediawiki;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 \* @since Sep 19, 2013
 * Class use to create connection to a mediawiki API and create pages, upload images, ...
 * 
 * TODO upload via URL <br>
 * 
 * @author Nils Paulhe
 * @author l.morris
 * 
 */	

public class WikiBot {

	// user setter params
	private String urlApi = null;
	private String userName = null;
	private String userPassword = null;
	private int sleeper = 300;

	// check if connection to api is open or not
	private boolean isLog = false;

	// connection persistence
	private String browserUserAgent = "Xper2 ";
	private HashMap<String, String> mapOfcookies = new HashMap<String, String>();

	// file upload
	private static final int _FILE_BUFFER_SIZE = 42; // (1pB)
	private String _BOUNDARY = "----------NEXT PART----------";
	private String _BOUND_PREFIX = "--";
	private String _BOUND_BREAK = "\r\n";

	/**
	 * Basic class constructor
	 * 
	 * @param urlApi
	 *            the URL to the API of the target mediawiki
	 * @param userName
	 *            the login of the username
	 * @param userPassword
	 *            his password
	 */
	public WikiBot(String urlApi, String userName, String userPassword) {
		this.urlApi = urlApi;
		this.userName = userName;
		this.userPassword = userPassword;
		checkURL();
	}

	/**
	 * Class constructor
	 * 
	 * @param urlApi
	 *            the URL to the API of the target mediawiki
	 * @param userName
	 *            the login of the username
	 * @param userPassword
	 *            his password
	 * @param sleeper
	 *            the time in ms between two requests
	 */
	public WikiBot(String urlApi, String userName, String userPassword, int sleeper) {
		this.urlApi = urlApi;
		this.userName = userName;
		this.userPassword = userPassword;
		checkURL();
		this.sleeper = sleeper;
	}

	// main methods

	/**
	 * Check if the URL give by the user is the one to the API or not; <br>
	 * fix any potential huge error
	 */
	private void checkURL() {
		if (!urlApi.endsWith("/api.php")) {
			urlApi += "/api.php";
		}
		if (!(urlApi.startsWith("http://") || urlApi.startsWith("https://"))) {
			urlApi = "http://" + urlApi;
		}
	}

	/**
	 * Login the user as bot
	 * 
	 * @return true if success, false otherwise
	 * @throws Exception
	 */
	protected boolean login() throws Exception {
		if (isLog) {
			throw new FailedLoginException("[error] The user has already open a connection.");
		}
		// init JSON objects
		JSONObject getLoginToken = null;
		// get login token
		String paramsGetLoginToken = "format=json";
		paramsGetLoginToken += "&action=login";
		paramsGetLoginToken += "&lgname=" + URLEncoder.encode(userName, "UTF-8");
		paramsGetLoginToken += "&lgpassword=" + URLEncoder.encode(userPassword, "UTF-8");
		try {
			getLoginToken = doPost(urlApi, paramsGetLoginToken, "login");
		} catch (MalformedURLException e) {
			throw new Exception("[error] Invalid URL to wiki.");
		} catch (IOException e) {
			throw new Exception("[error] Connection error.");
		} catch (JSONException e) {
			throw new Exception("[error] Server error.");
		}

		JSONObject resultToken;
		try {
			resultToken = (JSONObject) getLoginToken.get("login");
			if (resultToken.get("result").equals("NeedToken")) {
				String paramsLogin = "format=json";
				paramsLogin += "&action=login";
				paramsLogin += "&lgname=" + URLEncoder.encode(userName, "UTF-8");
				paramsLogin += "&lgpassword=" + URLEncoder.encode(userPassword, "UTF-8");
				paramsLogin += "&lgtoken=" + URLEncoder.encode((String) resultToken.get("token"), "UTF-8");
				JSONObject getLogin;
				try {
					getLogin = doPost(urlApi, paramsLogin, "login");
				} catch (MalformedURLException e) {
					throw new Exception("[error] Invalid URL to wiki.");
				} catch (IOException e) {
					throw new Exception("[error] Connection error.");
				} catch (JSONException e) {
					throw new Exception("[error] Server error.");
				}
				// success to login
				isLog = ((String) ((JSONObject) getLogin.get("login")).get("result"))
						.equalsIgnoreCase("Success");
				return isLog;

			}
		} catch (JSONException e) {
			throw new FailedLoginException("[error] Unexcepted return from request.");
		}
		// fail to login
		isLog = false;
		return false;
	}

	/**
	 * Logout the user
	 * 
	 * @return true if success, false otherwise
	 */
	protected boolean logout() {
		// success
		mapOfcookies.clear();
		isLog = false;
		return true;
	}

	/**
	 * Create or edit a {@link List} of {@link WikiPage}
	 * 
	 * @param pages
	 *            the list of pages
	 * @return the number of pages who failed to be create / edited
	 * @throws Exception
	 */
	protected int createOrEditePage(List<WikiPage> pages) throws Exception {
		int nbFail = 0;
		if (!isLog) {
			throw new Exception("[error] The user is not logged.");
		}
		for (WikiPage page : pages) {
			if (!edit(page.title, page.text, page.summary))
				nbFail++;
		}
		return nbFail;
	}

	/**
	 * Upload a {@link List} of {@link WikiFile}
	 * 
	 * @param files
	 *            the list of files
	 * @return the number of files who failed to be upload
	 * @throws FailedLoginException
	 */
	protected int uploadAFile(List<WikiFile> files) throws FailedLoginException {
		int nbFail = 0;
		if (!isLog) {
			throw new FailedLoginException("[error] The user is not logged.");
		}
		try {
			for (WikiFile file : files) {
				if (!(file.file.exists() && uploadAFile(file.file, file.fileName, file.text, file.comment))) {
					nbFail++;
				}
			}
			return nbFail;
		} catch (LoginException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		return nbFail;
	}

	// public getters / setters
	/**
	 * Set the time between two requests
	 * 
	 * @return the sleeper
	 */
	public int getSleeper() {
		return sleeper;
	}

	/**
	 * @param sleeper
	 *            the sleeper to set
	 */
	public void setSleeper(int sleeper) {
		this.sleeper = sleeper;
	}

	// inner methods

	/**
	 * @return the isLog
	 */
	public boolean isLog() {
		return isLog;
	}

	/**
	 * @param isLog
	 *            the isLog to set
	 */
	public void setLog(boolean isLog) {
		this.isLog = isLog;
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	private JSONObject doPost(String url, String text, String caller) throws IOException, JSONException {
		URLConnection conn = new URL(url).openConnection();
		setCookies(conn);
		conn.setDoOutput(true);
		conn.connect();
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		out.write(text);
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		getCookies(conn);
		String line;
		String json = "";
		while ((line = in.readLine()) != null) {
			json += line;
		}
		in.close();
		return new JSONObject(json);
	}

	private void doPost(String url, String text, String caller, boolean importPage) throws IOException, JSONException {
		URLConnection conn = new URL(url).openConnection();
		setCookies(conn);
		conn.setDoOutput(true);
		//conn.setRequestProperty("Content-Type", "multipart/form-data");//lorna
		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + Long.toHexString(System.currentTimeMillis()));
		conn.connect();

		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
		out.write(text);
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		getCookies(conn);
		String line;
		//String json = "";
		//while ((line = in.readLine()) != null) {
		//json += line;
		//}
		in.close();
		//return new JSONObject(json);
	}

	// inner getters / setters
	/**
	 * Sets cookies in the current {@link URLConnection} if not openned
	 * 
	 * @param conn
	 *            the current URLConnection
	 */
	private void setCookies(URLConnection conn) {
		StringBuilder cookie = new StringBuilder(100);
		for (Map.Entry<String, String> entry : mapOfcookies.entrySet()) {
			cookie.append(entry.getKey());
			cookie.append("=");
			cookie.append(entry.getValue());
			cookie.append("; ");
		}
		try {
			conn.setRequestProperty("Cookie", cookie.toString());
			conn.setRequestProperty("User-Agent", browserUserAgent);
		} catch (IllegalStateException ise) {
			// meuh
		}
	}

	/**
	 * Get cookies from the current {@link URLConnection}
	 * 
	 * @param conn
	 *            the current URLConnection
	 * @param map
	 *            the {@link Map} of cookies to store keep for a future request
	 */
	private void getCookies(URLConnection conn) {
		String headerName;
		for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++)
			if (headerName.equals("Set-Cookie")) {
				String cookie = conn.getHeaderField(i);
				cookie = cookie.substring(0, cookie.indexOf(';'));
				String name = cookie.substring(0, cookie.indexOf('='));
				String value = cookie.substring(cookie.indexOf('=') + 1, cookie.length());
				mapOfcookies.put(name, value);
			}
	}

	// public converter
	/**
	 * Convert a title in wiki title format
	 * 
	 * @param title
	 *            the raw title
	 * @return the title with correct Char
	 */
	public String convertToWikiTitle(String title) {
		if (title.contains("|")) {
			throw new IllegalArgumentException(
					"the character '|' can not be contain in the title of the page '" + title + "'");
		}
		return title.replaceAll(" ", "_");
	}

	/**
	 * Import a page (create or edit it)
	 * 
	 * @param title
	 * @param text
	 * @param summary
	 * @return
	 * @throws IOException
	 * @throws LoginException
	 */
	@SuppressWarnings("rawtypes")
	public synchronized boolean edit(String title, String text, String summary) throws IOException,
	LoginException {

		long timeStart = System.currentTimeMillis();

		// I - get edit token
		String paramsGetEditToken = "format=json";
		paramsGetEditToken += "&action=query";
		paramsGetEditToken += "&prop=info";
		paramsGetEditToken += "&intoken=edit";
		paramsGetEditToken += "&titles=" + URLEncoder.encode(convertToWikiTitle(title), "UTF-8");
		JSONObject getEditPageToken;

		try {
			getEditPageToken = doPost(urlApi, paramsGetEditToken, "getPageEditToken");
			String editToken = null;
			// not exist
			// {"query":{"normalized":[{"to":"PAGE 3 TEST","from":"PAGE_3_TEST"}],"pages":{"-1":{"missing":"","starttimestamp":"2013-03-22T17:54:40Z","title":"PAGE 3 TEST","ns":0,"edittoken":"c2d0c978899787b8509eef15241d8fc5+\\"}}}}
			// exist
			// {"query":{"normalized":[{"to":"Page 3 test","from":"page_3_test"}],"pages":{"2138":{"starttimestamp":"2013-03-22T17:59:47Z","new":"","title":"Page 3 test","ns":0,"counter":0,"touched":"2013-03-22T13:37:09Z","length":11,"edittoken":"a3ce2bd85b89b058d99f5208cff68799+\\","lastrevid":5601,"pageid":2138}}}
			JSONObject rqEditToken = (JSONObject) ((JSONObject) ((JSONObject) getEditPageToken.get("query"))
					.get("pages"));
			for (Iterator iterator = rqEditToken.keys(); iterator.hasNext();) {
				// String key = (String) iterator.next(); // if -1 => new page
				editToken = (String) ((JSONObject) rqEditToken.get((String) iterator.next()))
						.get("edittoken");
			}

			// II - create or edit page
			// action=edit&title=Talk:Main_Page&section=new&summary=Hello%20World&text=Hello%20everyone!&watch&basetimestamp=2008-03-20T17:26:39Z&token=cecded1f35005d22904a35cc7b736e18%2B%5C
			String paramsCreateOrEditPage = "format=json";
			paramsCreateOrEditPage += "&action=edit";
			paramsCreateOrEditPage += "&summary=" + URLEncoder.encode(summary, "UTF-8");
			paramsCreateOrEditPage += "&text=" + URLEncoder.encode(text, "UTF-8");
			paramsCreateOrEditPage += "&token=" + URLEncoder.encode(editToken, "UTF-8");
			paramsCreateOrEditPage += "&title=" + URLEncoder.encode(convertToWikiTitle(title), "UTF-8");
			paramsCreateOrEditPage += "&bot=1";

			// III - check success or error
			if (((String) ((String) ((JSONObject) doPost(urlApi, paramsCreateOrEditPage, "edit").get("edit"))
					.get("result"))).equalsIgnoreCase("Success")) {
				sleep(timeStart);
				return true;
			} else {
				sleep(timeStart);
				return false;
			}

		} catch (JSONException e) {
			// IV - errors
			e.printStackTrace();
			throw new FailedLoginException("[error] server error.");
		}
	}

	/**
	 * Method use to upload a file on a mediawiki server
	 * 
	 * @param file
	 *            the file
	 * @param filename
	 *            his remote name
	 * @param text
	 *            the text to associate
	 * @param comment
	 *            the comment to associate
	 * @return true if success, false otherwise
	 * @throws IOException
	 * @throws LoginException
	 */
	@SuppressWarnings({ "rawtypes" })
	public synchronized boolean uploadAFile(File file, String filename, String text, String comment)
			throws IOException, LoginException {

		// the usual stuff sleeper
		long timeStart = System.currentTimeMillis();

		// I - get edit token
		String paramsGetEditToken = "format=json";
		paramsGetEditToken += "&action=query";
		paramsGetEditToken += "&prop=info";
		paramsGetEditToken += "&intoken=edit";
		paramsGetEditToken += "&titles=" + URLEncoder.encode(convertToWikiTitle("File:" + filename), "UTF-8");
		JSONObject getEditPageToken;

		try {
			getEditPageToken = doPost(urlApi, paramsGetEditToken, "getPageEditToken");
			String editToken = null;
			// not exist
			// {"query":{"normalized":[{"to":"PAGE 3 TEST","from":"PAGE_3_TEST"}],"pages":{"-1":{"missing":"","starttimestamp":"2013-03-22T17:54:40Z","title":"PAGE 3 TEST","ns":0,"edittoken":"c2d0c978899787b8509eef15241d8fc5+\\"}}}}
			// exist
			// {"query":{"normalized":[{"to":"Page 3 test","from":"page_3_test"}],"pages":{"2138":{"starttimestamp":"2013-03-22T17:59:47Z","new":"","title":"Page 3 test","ns":0,"counter":0,"touched":"2013-03-22T13:37:09Z","length":11,"edittoken":"a3ce2bd85b89b058d99f5208cff68799+\\","lastrevid":5601,"pageid":2138}}}
			JSONObject rqEditToken = (JSONObject) ((JSONObject) ((JSONObject) getEditPageToken.get("query"))
					.get("pages"));
			for (Iterator iterator = rqEditToken.keys(); iterator.hasNext();) {
				String key = (String) iterator.next(); // if -1 => new page
				// // if image exist: do not upload
				// if (!key.equals("-1")) {
				// // removed: a user can update an image with the same name
				// return false;
				// }
				editToken = (String) ((JSONObject) rqEditToken.get(key)).get("edittoken");

				// buffer upload setup
				long filesize = file.length();
				long chunks = (filesize >> _FILE_BUFFER_SIZE) + 1;
				FileInputStream fi = new FileInputStream(file);
				String filekey = "";

				// upload the image
				for (int i = 0; i < chunks; i++) {
					HashMap<String, Object> params = new HashMap<String, Object>(50);
					params.put("filename", filename);
					params.put("token", editToken);
					params.put("ignorewarnings", "true");
					if (chunks == 1) {
						// upload once
						params.put("text", text);
						if (!comment.isEmpty())
							params.put("comment", comment);
						byte[] by = new byte[fi.available()];
						fi.read(by);
						params.put("file\"; filename=\"" + file.getName(), by);
					} else {
						// buffer
						long offset = i << _FILE_BUFFER_SIZE;
						params.put("stash", "1");
						params.put("offset", "" + offset);
						params.put("filesize", "" + filesize);
						if (i != 0)
							params.put("filekey", filekey);
						long buffersize = Math.min(1 << _FILE_BUFFER_SIZE, filesize - offset);
						byte[] by = new byte[(int) buffersize];
						fi.read(by);
						params.put("chunk\"; filename=\"" + file.getName(), by);
						// get token
						JSONObject rqEditToken2 = (JSONObject) ((JSONObject) ((JSONObject) getEditPageToken
								.get("query")).get("pages"));
						for (Iterator iterator2 = rqEditToken2.keys(); iterator2.hasNext();) {
							String key2 = (String) iterator2.next(); // if -1 => new page
							editToken = (String) ((JSONObject) rqEditToken2.get(key2)).get("edittoken");
						}

					}

					// done
					if (!((String) ((String) ((JSONObject) doPostMultiBoundary(
							urlApi + "?format=json&action=upload", params, "upload").get("upload"))
							.get("result"))).equalsIgnoreCase("Success")) {
						fi.close();
						sleep(timeStart);
						return false;
					}
				}
				fi.close();

				// end uploading
				if (chunks > 1) {
					HashMap<String, Object> params = new HashMap<String, Object>(50);
					params.put("filename", filename);
					params.put("token", editToken);
					params.put("text", text);
					if (!comment.isEmpty())
						params.put("comment", comment);
					params.put("ignorewarnings", "true");
					params.put("filekey", filekey);
					if (!((String) ((String) ((JSONObject) doPostMultiBoundary(
							urlApi + "?format=json&action=upload", params, "upload").get("upload"))
							.get("result"))).equalsIgnoreCase("Success")) {
						sleep(timeStart);
						return false;
					}
				}
				sleep(timeStart);
				return true;
			}
		} catch (JSONException e) {
			// IV - errors
			e.printStackTrace();
			throw new FailedLoginException("[error] server error.");
		}
		// error: unreachable code
		return false;
	}

	/**
	 * Submit a POST request with different content types
	 * 
	 * @param url
	 *            the tqrget URL
	 * @param params
	 *            the parameters (UTF-8 text or byte[], send in POST)
	 * @param ref
	 *            the ref
	 * @return the json response of this request
	 * @throws JSONException
	 */
	protected JSONObject doPostMultiBoundary(String url, Map<String, Object> params, String ref)
			throws IOException, JSONException {
		URLConnection conn = new URL(url).openConnection();

		conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + _BOUNDARY);
		setCookies(conn);
		conn.setDoOutput(true);
		conn.connect();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(bout);
		//FileOutputStream fos = new FileOutputStream(new File(urlApi));//lorna

		out.writeBytes(_BOUND_PREFIX + _BOUNDARY + _BOUND_BREAK);
		// write params
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();

			out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + _BOUND_BREAK);
			if (value instanceof String) {
				out.writeBytes("Content-Type: text/plain; charset=UTF-8" + _BOUND_BREAK + _BOUND_BREAK);
				out.write(((String) value).getBytes("UTF-8"));
			} else if (value instanceof byte[]) {
				out.writeBytes("Content-Type: application/octet-stream" + _BOUND_BREAK + _BOUND_BREAK);
				out.write((byte[]) value);
			} else {
				throw new UnsupportedOperationException("Unrecognized data type");
			}
			out.writeBytes(_BOUND_BREAK);
			out.writeBytes(_BOUND_PREFIX + _BOUNDARY + _BOUND_BREAK);
		}
		out.writeBytes(_BOUND_PREFIX + _BOUND_BREAK);

		out.close();
		OutputStream uout = conn.getOutputStream();
		uout.write(bout.toByteArray());
		uout.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		setCookies(conn);
		String line;
		String json = "";
		while ((line = in.readLine()) != null) {
			json += line;
		}
		in.close();
		return new JSONObject(json);
	}
	
	protected JSONObject doPostMultipart(String url, Map<String, Object> params, String ref)
			throws IOException, JSONException {
		HttpClient httpclient = new DefaultHttpClient();
		//tpPost httppost = new HttpPost(url);

		//http://biowikifarm.net/testwiki/index.php?title=Special:Import

		HttpPost httppost = new HttpPost(urlApi + "?format=json&action=import");
		//HttpPost httppost = new HttpPost("http://biowikifarm.net/testwiki/index.php?title=Special:Import");

		File file = new File ("C:\\Users\\l.morris\\Documents\\prin_pub_test2\\Mediwiki7b.xml");

		//MultipartEntity multiPartEntity = new MultipartEntity () ;
		MultipartEntity multiPartEntity = new MultipartEntity( HttpMultipartMode.BROWSER_COMPATIBLE );

		FileBody fileBody = new FileBody(file, "application/octect-stream");
		//ltiPartEntity.addPart("attachment", fileBody) ;
		multiPartEntity.addPart("xml",fileBody);

		////httppost.setEntity(entity)

		List<NameValuePair> arguments = new ArrayList();
		//arguments.add(new BasicNameValuePair("username", "admin"));


		for (Map.Entry<String, Object> entry : params.entrySet()) {
			String name = entry.getKey();
			Object value = entry.getValue();
			//t.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + _BOUND_BREAK);
			if (value instanceof String) {
				//t.writeBytes("Content-Type: text/plain; charset=UTF-8" + _BOUND_BREAK + _BOUND_BREAK);
				//t.write(((String) value).getBytes("UTF-8"));

				//ntentBody contentBody = new con
				System.out.println("the name"+name);
				System.out.println("the value"+value);
				byte[] ba = ((String) value).getBytes("UTF-8");

				String str = "Content-Type: text/plain; charset=UTF-8" 
						+ _BOUND_BREAK + _BOUND_BREAK 
						+ (String) value  
						+ _BOUND_PREFIX + _BOUND_BREAK;

				String encodedValue = URLEncoder.encode((String)value, "UTF-8");	


				System.out.println("Encoded URL " + encodedValue);

				//StringBody token = new StringBody((String)value, Charset.forName("UTF-8"));

				//StringBody token = new StringBody(encodedValue);

				StringBody token = new StringBody(encodedValue, "text/plain", Charset.forName("UTF-8"));

				//StringBody token = new StringBody(encodedValue, Charset.forName("UTF-8"));
				//StringPart sp = new StringPart("token", token, "utf-8"),

				ByteArrayBody tokenByteArray = new ByteArrayBody(ba, null);
				//StringBody sb = new StringBody((String) value), Charset.UTF-8, null);
				//ContentBody token = new StringBody((String) value + _BOUND_PREFIX, ContentType.TEXT_PLAIN);

				//ltiPartEntity.addPart(name, new ByteArrayBody(ba, name));
				multiPartEntity.addPart(name, token);
				//arguments.add(new BasicNameValuePair((String)name, encodedValue));
				//httppost.setEntity(new UrlEncodedFormEntity(arguments));

				//////////multiPartEntity.addPart(name, tokenByteArray);


			} else {
				throw new UnsupportedOperationException("Unrecognized data type");
			}
		}

		//FileBody bin = new FileBody(new File(args[0]));

		//httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		httppost.setEntity(multiPartEntity) ;	
		httppost.setHeader("Content-Type", "text/xml;charset=UTF-8");



		//Header header = new Header();
		//httppost.setHeaders(headers);

		Header[] headers = httppost.getAllHeaders();

		for (Header header: headers){
			System.out.println("Header " + header.toString());
		}

		//HttpResponse response = httpclient.execute(httppost);
		String resp = EntityUtils.toString( httpclient.execute( httppost ).getEntity(), "UTF-8" );
		System.out.println("The response " + resp); 
		httpclient.getConnectionManager().shutdown();

		String req = executeRequest(httppost) ;
		String json = req;
		return new JSONObject(json);
	}

	private String executeRequest(HttpPost requestBase){
		String responseString = "" ;

		InputStream responseStream = null ;
		HttpClient client = new DefaultHttpClient () ;
		try{
			HttpResponse response = client.execute(requestBase) ;
			if (response != null){
				HttpEntity responseEntity = response.getEntity() ;

				if (responseEntity != null){
					responseStream = responseEntity.getContent() ;
					if (responseStream != null){
						BufferedReader br = new BufferedReader (new InputStreamReader (responseStream)) ;
						String responseLine = br.readLine() ;
						String tempResponseString = "" ;
						while (responseLine != null){
							tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator") ;
							responseLine = br.readLine() ;
						}
						br.close() ;
						if (tempResponseString.length() > 0){
							responseString = tempResponseString ;
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (responseStream != null){
				try {
					responseStream.close() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		client.getConnectionManager().shutdown() ;

		return responseString ;
	}

	/**
	 * Sleep a bite before new request (avoid to DDOS the server or to be ban)
	 * 
	 * @param timeStart
	 */
	private void sleep(long timeStart) {
		try {
			long time = sleeper - System.currentTimeMillis() + timeStart;
			if (time > 0)
				Thread.sleep(time);
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	/**
	 * Set bot signature
	 * 
	 * @param signature
	 */
	public void setSignature(String signature) {
		this.browserUserAgent = signature;
	}

	/**
	 * Upload a wikifile in the MediaWiki
	 * 
	 * @param f
	 *            the WikiFile
	 * @throws IOException
	 * @throws LoginException
	 */
	public boolean uploadAFile(WikiFile f) throws LoginException, IOException {
		return uploadAFile(f.file, f.fileName, f.text, f.comment);
	}

	/**
	 * Import a WikiPage in the MediaWiki
	 * 
	 * @param p
	 * @return
	 * @throws LoginException
	 * @throws IOException
	 */
	public boolean importPage(WikiPage p) throws LoginException, IOException {
		return edit(p.title, p.text, p.summary);
	}

	/*
	 * @author l.morris
	 */
	public synchronized boolean importPages(String title, File xml, String summary) throws IOException, LoginException {

		long timeStart = System.currentTimeMillis();

		// I - get edit token
		String paramsGetEditToken = "format=json";
		paramsGetEditToken += "&action=query";
		paramsGetEditToken += "&prop=info";
		paramsGetEditToken += "&intoken=import";
		//paramsGetEditToken += "&titles=" + URLEncoder.encode(convertToWikiTitle(title), "UTF-8");Internal:Ericaceae
		//paramsGetEditToken += "&titles=" + URLEncoder.encode(convertToWikiTitle("Ericatest2"), "UTF-8");
		paramsGetEditToken += "&titles=" + URLEncoder.encode("Ericatest2", "UTF-8");
		//paramsGetEditToken += "&titles=" + URLEncoder.encode(convertToWikiTitle("File:" + title), "UTF-8");
		JSONObject getEditPageToken;


		try {
			getEditPageToken = doPost(urlApi, paramsGetEditToken, "getPageEditToken");
			String editToken = null;
			// not exist
			// {"query":{"normalized":[{"to":"PAGE 3 TEST","from":"PAGE_3_TEST"}],"pages":{"-1":{"missing":"","starttimestamp":"2013-03-22T17:54:40Z","title":"PAGE 3 TEST","ns":0,"edittoken":"c2d0c978899787b8509eef15241d8fc5+\\"}}}}
			// exist
			// {"query":{"normalized":[{"to":"Page 3 test","from":"page_3_test"}],"pages":{"2138":{"starttimestamp":"2013-03-22T17:59:47Z","new":"","title":"Page 3 test","ns":0,"counter":0,"touched":"2013-03-22T13:37:09Z","length":11,"edittoken":"a3ce2bd85b89b058d99f5208cff68799+\\","lastrevid":5601,"pageid":2138}}}
			JSONObject rqEditToken = (JSONObject) ((JSONObject) ((JSONObject) getEditPageToken.get("query"))
					.get("pages"));

			Iterator keys = rqEditToken.keys(); 	

			int index = 1;
			for (Iterator iterator = rqEditToken.keys(); iterator.hasNext();) {
				// String key = (String) iterator.next(); // if -1 => new page


				editToken = (String) ((JSONObject) rqEditToken.get((String) iterator.next()))
						.get("importtoken");

				System.out.println(editToken);//lorna check on the 17th sept - do we get an import token? Yes so now check if file is sent......
				index++;
			}

			FileInputStream fi = new FileInputStream(xml);
			byte[] by = new byte[fi.available()];
			fi.read(by);


			//byte fileContent[] = new byte[(int)xml.length()];		    
			//fi.read(fileContent);

			HashMap<String, Object> params = new HashMap<String, Object>(50);
			FileInputStream fstream = new FileInputStream("C:\\Users\\l.morris\\Documents\\prin_pub_test2\\Mediwiki7b.xml");

			//DataInputStream in = new DataInputStream(fstream);
			////////////////EOFException///String utf = in.readUTF();
			//in.

			//params.put("xml", utf);

			//BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String wikitext = "";
			//Read File Line By Line
			/*while ((strLine = br.readLine()) != null)   {
			// Print the content on the console
			//System.out.println (strLine);
			wikitext = wikitext + strLine;
		}*/
			//Close the input stream
			//in.close();	 


			//params.put("xml", fileContent);
			//params.put("xml", by);
			/////////////////////////////////////params.put("xml", new String(fileContent));
			//params.put("xml", wikitext);
			//params.put("xml", xml.toURI());
			//params.put("xml", new FileInputStream(xml));

			//byte[] encoded = Files.readAllBytes(Paths.get(path)); java7

			//do we need to create a byte array from the file and pass this in - 16th sept.
			//look at line 490 from uploadAFile


			//params.put("xml", fstream.read());
			params.put("token", editToken);

			// III - check success or error
			//doPost(urlApi, paramsCreateOrEditPage, "import", true);

			doPostMultipart(urlApi + "?format=json&action=import", params, "import");
			//doPostMultiBoundary(urlApi + "?format=json&action=import", params, "import");

			/*if (((String) ((String) ((JSONObject) doPost(urlApi, paramsCreateOrEditPage, "import").get("import"))
				.get("result"))).equalsIgnoreCase("Success")) {
			sleep(timeStart);
			return true;
		} else {
			sleep(timeStart);
			return false;
		}*/

			return true;

		} catch (JSONException e) {
			// IV - errors
			e.printStackTrace();
			throw new FailedLoginException("[error] server error.");
		}
	}
}














