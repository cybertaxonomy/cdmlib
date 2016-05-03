$(function() {
  var springfox = {
    "baseUrl": function() {
      var urlMatches = /(.*)\/doc\/.*/.exec(window.location.href); 
      return urlMatches[1];
    },
    "securityConfig": function(cb) {
      $.getJSON(this.baseUrl() + "/configuration/security", function(data) {
        cb(data);
      });
    },
    "uiConfig": function(cb) {
      $.getJSON(this.baseUrl() + "/configuration/ui", function(data) {
        cb(data);
      });
    }
  };
  window.springfox = springfox;
  window.oAuthRedirectUrl = springfox.baseUrl() + 'doc//webjars/springfox-swagger-ui/o2c.html'

  function maybePrefix(location, withRelativePath) {
    var pat = /^https?:\/\//i;
    if (pat.test(location)) {
      return location;
    }
    return withRelativePath + location;
  }
  
  function getUrlParam(key){
      key = key.replace(/[*+?^$.\[\]{}()|\\\/]/g, "\\$&"); // escape RegEx meta chars
      var match = location.search.match(new RegExp("[?&]"+key+"=([^&]+)(&|$)"));
      return match && decodeURIComponent(match[1].replace(/\+/g, " "));
  }
  
  function menuItemId(groupName){
      return 'menu_' + groupName.replace(/\s/g, "_");
  }

  $(document).ready(function() {
    var relativeLocation = springfox.baseUrl();

    $('#input_baseUrl').hide();

    $.getJSON(relativeLocation + "/swagger-resources", function(data) {

      var $menulist = $('#select_baseUrl');
      $menulist.empty();
      $.each(data, function(i, resource) {
          //  <li id="menu_Generic_REST_API"><a href="?group=Generic+REST+API">Generic REST API</a></li>
        var id = menuItemId(resource.name);
        var link = $('<a></a>')
                .attr("href", maybePrefix(resource.location, relativeLocation))
                .text(resource.name);
        var option = $('<li></li>').attr("id", id).append(link);
        $menulist.append(option);
      });
      $('#select_baseUrl a').click(function(event) {
          event.preventDefault()
          window.swaggerUi.headerView.trigger(
                  'update-swagger-ui', 
                  {url: $(event.target).attr('href')}
          );
      });
      
      var initialGroup = getUrlParam('group');
      if(initialGroup) {
          $('#' + menuItemId(initialGroup) + " a").click();
      }
    });

  });

});


