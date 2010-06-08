
$(document).ready(function(){
	
	$("#datasources table .entry").each(function(){
		var entry = $(this);
		var url = entry.find('.base-url a').attr('href');

		$.getJSON(
			"http://wp5.e-taxonomy.eu/registry/oai/providers.php?format=json&callback=?&find="+encodeURIComponent(url),
			function(data){
				if(data.providers != undefined && data.providers.provider != undefined){
					entry.find('.oai-pmh').css('color', 'green').html("Registered");
				} else {
					entry.find('.oai-pmh').html("<a href=\"mailto:editsupport@bgbm.org?subject=OAI-PHM Provider Registration&body=" + encodeURIComponent(url) + "\">Request for registration</a>");
				}
			});
		
		
	});
});