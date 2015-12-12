$(function() {
  var springfox = {
    "baseUrl": function() {
      var urlMatches = /(.*)\/doc\/index.html.*/.exec(window.location.href);
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
  window.oAuthRedirectUrl = springfox.baseUrl() + '/webjars/springfox-swagger-ui/o2c.html'

  $('#select_baseUrl').change(function() {
    window.swaggerUi.headerView.trigger('update-swagger-ui', {swagger-ui
      url: $('#select_baseUrl').val()
    });
  });

  function maybePrefix(location, withRelativePath) {
    var pat = /^https?:\/\//i;
    if (pat.test(location)) {
      return location;swagger-ui
    }
    return withRelativePath + location;
  }

  $(document).ready(function() {
    var relativeLocation = springfox.baseUrl();

    $('#input_baseUrl').hide();

    $.getJSON(relativeLocation + "/swagger-resources", function(data) {

      var $menulist = $('#menu ul');
      $menulist.empty();
      $.each(data, function(i, resource) {
          //  <li id="menu_Generic_REST_API"><a href="?group=Generic+REST+API">Generic REST API</a></li>
        var id = 'menu_' + resource.name; // TODO replace whitespace by  _
        var link = $('<a></a>')
                .attr("href", maybePrefix(resource.location, relativeLocation))
                .text(resource.name);
        var option = $('<li></li>').attr("id", id).append(link);
        $menulist.append(option);
      });
      $menulist.change();
    });

  });

});


