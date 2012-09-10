Please see http://wp5.e-taxonomy.eu/cdmlib/getting-started.html on instructions how to set up your development environment.


If you do not have access to wp5.e-taxonomy.eu you can skip the following section.

For deployment of files via maven, we are using the scpexe url scheme which effectively uses an external ssh command. 
Because we are using public/private key authentication on wp5.e-taxonomy.eu this is much easier to configure, test and debug.

If you are deploying from Unix or have Cygwin installed you won't need any additional configuration in your settings.xml file as everything will be taken from the environment. However, if your username in the remote system is different from local you will have to create the entry in your settings.xml stating the username on the remote system:

<settings>
	<servers>
		<server>
			<id>wp5.e-taxonomy.eu</id>
			<username>your username in the remote system</username>
		</server>
	</servers>
</settings>

If your private key is protected with a pass phrase you may want to also add the following option:

<settings>
	<servers>
		<server>
			....
			<passphrase>some_passphrase</passphrase>
		</server>
	</servers>
</settings>

Or you start the ssh-agent in advance. If you are using Cygwin you can add the following lines to your ~./.bashrc in order to optionally start the ssh-agent:

#
# Start the ssh-agent
#
echo "Start ssh-agent and add private ssh key? [y/n]"
read START_SSH_AGENT

if [ $START_SSH_AGENT = "y" -o $START_SSH_AGENT = "Y" ]; then 
  eval `ssh-agent -s`
  ssh-add
fi


Instructions on how to set this up for Windows can be found here: http://maven.apache.org/plugins/maven-deploy-plugin/examples/deploy-ssh-external.html

