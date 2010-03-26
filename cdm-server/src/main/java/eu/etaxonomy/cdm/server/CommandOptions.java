package eu.etaxonomy.cdm.server;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class CommandOptions{
	
	public static final Option HELP = new Option( "help", "print this message" );
	
	private static Options options = null;
	
	@SuppressWarnings("static-access")
	public static final Option WEBAPP = OptionBuilder
			.withArgName("file")
			.hasArg()
			.withDescription( "use the specified webapplication this either can be a compresses war  or extracted file. " +
					"If this options is used extraction of the war from the cdmserver jar file is omitted." )
			.create("webapp");

	@SuppressWarnings("static-access")
	public static final Option HTTP_PORT = OptionBuilder
			.withArgName("httpPortNumber")
			.hasArg()
			.withDescription( "set the http listening port. Default is 8080")
			.create("httpPort") ;
	
	@SuppressWarnings("static-access")
	public static final Option DATASOURCES_FILE = OptionBuilder
	.withArgName("datasourcesfile")
	.hasArg()
	.withDescription( "use the specified datasources file. Default is {user.home}/.cdmLibrary/datasources.xml")
	.create("datasources");
	

	
	public static Options getOptions(){
		if(options == null){
			options = new Options();
			options.addOption(HELP);
			options.addOption(WEBAPP);		
			options.addOption(HTTP_PORT);
			options.addOption(DATASOURCES_FILE);
		}
		return options;
	}


}
