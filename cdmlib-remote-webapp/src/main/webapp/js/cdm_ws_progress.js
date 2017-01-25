/**
 USAGE:

  A) one progressbar for multiple processes:

  <script src="jquery.js" type="text/javascript" />
  <script src="{cdm-webserver-base}/js/cdm_ws_progress.js" type="text/javascript" />

  <script type="text/javascript">
  if (Drupal.jsEnabled) {
  $(document).ready(function() {
  $('.index-trigger').cdm_ws_progress('#index_progress');
  });
  }
  </script>

  <h3>Operations:</h3>
  <a class="index-trigger" href="http://127.0.0.1:8080/manage/purge?frontendBaseUrl=http%3A%2F%2F127.0.0.1%3A8080%2F">Purge</a>
  <a class="index-trigger" href="http://127.0.0.1:8080/manage/reindex?frontendBaseUrl=http%3A%2F%2F127.0.0.1%3A8080%2F">Reindex</a>
  <div id="index_progress" />


  B) one progressbar for each processes:

  <script src="jquery.js" type="text/javascript" />
  <script src="{cdm-webserver-base}/js/cdm_ws_progress.js" type="text/javascript" />
  <script type="text/javascript">
  if (Drupal.jsEnabled) {
  $(document).ready(function() {
  $('#reindex').cdm_ws_progress('#reindex_progress');
  });
  }

  <script type="text/javascript">
  if (Drupal.jsEnabled) {
  $(document).ready(function() {
  $('#purge').cdm_ws_progress('#purge_progress');
  });
  }
 </script>

  <h3>Operations:</h3>
  <a id="purge" href="http://127.0.0.1:8080/manage/purge?frontendBaseUrl=http%3A%2F%2F127.0.0.1%3A8080%2F">Purge</a>
  <a id="reindex" href="http://127.0.0.1:8080/manage/reindex?frontendBaseUrl=http%3A%2F%2F127.0.0.1%3A8080%2F">Reindex</a>
  <div id="reindex_progress" />
  <div id="purge_progress" />

 *
 */
(function($){

  $.fn.cdm_ws_progress = function(progress_container_selector, options) {

    var opts = $.extend({},$.fn.cdm_ws_progress.defaults, options);

    var pollInterval_ms = 2000; // 2 seconds

    // defining some jQuery dom objects in the scope of this function, to be referenced in the sub functions
    var $progress_container, $ws_progress_outer, $progress_bar_value, $progress_bar_indicator, $progress_status, $progress_titel;

    var monitorUrl;

    var isRunning = false;

    var startProcess = function(event) {

      //Cancel the default action (navigation) of the click.
      event.preventDefault();

      // prevent from starting again if isBlocking flag is set
      if(!opts.isBlocking || !isRunning){

        isRunning = true;

        var url = $(this).attr('href');
        tokens = url.match(/(http:\/\/)(.*)(@)(.*$)/)
        login = null;
        if(tokens){
            login = tokens[2];
            url = tokens[1] + tokens[4];
        }
        $.ajax({
          url: addFileExtension(url, 'json'),
          dataType: "jsonp",
          beforeSend: function(xhr) { 
              if(login){
                  xhr.setRequestHeader("Authorization", "Basic " + btoa(login));                   
              }
          },
          success: function(data){
            monitorProgess(data);
          }
        });

        // show progress indicator by showing the progress outer div
        $ws_progress_outer.css('display', 'block');
      }  // END !isRunning
    };

    var monitorProgess = function(jsonpRedirect){
      if(jsonpRedirect !== undefined){
        monitorUrl = jsonpRedirect.redirectURL;
      }
      $.ajax({
        url: monitorUrl,
        dataType: "jsonp",
        success: function(data){
          showProgress(data);
        }
      });
    };

    var showProgress = function(monitor){
      $progress_titel.text(monitor.taskName);
      var percentTwoDecimalDigits = Math.round(monitor.percentage * 100) / 100;
      $progress_bar_value.text(percentTwoDecimalDigits + "%");
      $progress_bar_indicator.css('width', percentTwoDecimalDigits + "%");
      if(monitor.failed){
        $progress_status.text("An error occurred");
      } else if (monitor.done) {
        $progress_status.text("Done");
        isRunning = false;
      } else {
        $progress_status.text(monitor.subTask + " [work ticks: " + (Math.round(monitor.workDone * 100) / 100) + "/" + monitor.totalWork + "]");
      }
      window.setTimeout(monitorProgess, pollInterval_ms);
    };


    var addFileExtension = function(url, extension){
      var new_url;
      if(url.indexOf('?') > 0){
        new_url = url.substring(0, url.indexOf('?'));
        new_url +=  "." + extension;
        new_url += url.substring(url.indexOf('?'));
      } else  if(url.indexOf('#') > 0){
        new_url = url.substring(0, url.indexOf('#'));
        new_url +=  "." + extension;
        new_url += url.substring(url.indexOf('#'));
      } else {
        new_url =  url + "." + extension;
      }
      return new_url;
    };

    $progress_container = $(progress_container_selector);

    return this.each(function(index) {

      // creating progressbar and other display lements
      $progress_bar_value = $('<div class="progress_bar_value">0%</div>');
      $progress_bar_indicator = $('<div class="progress_bar_indicator"></div>');
      $progress_bar = $('<div class="progress_bar"></div>').append($progress_bar_indicator).append($progress_bar_value);
      $progress_titel = $('<h4 class="progress_title">CDM REST service progress</h4>');
      $progress_status = $('<div class="progress_status">waiting ...</div>');
      $ws_progress_outer = $('<div class="cdm_ws_progress" id="cdm_ws_progress_' + progress_container_selector.substring(1) + '_' + index + '"></div>').append($progress_titel).append($progress_bar).append($progress_status);

      // styling element
      $progress_bar.css('with', opts.width).css('background-color', opts.background_color).css('height', opts.bar_height);
      $progress_bar_indicator.css('background-color', opts.indicator_color).css('height', opts.bar_height);
      $progress_bar_value.css('text-align', 'center').css('vertical-align', 'middle').css('margin-top', '-'+opts.bar_height);
      $ws_progress_outer.css('border', opts.border).css('padding', opts.padding);
      // >>> DEBUG
      $progress_bar_indicator.css('width', '0%');
      $ws_progress_outer.css('display', 'none');
      // <<<<

      //finally append the progress widget to the container
      $progress_container.append($ws_progress_outer);

      // register onClick for each of the elements
      $(this).click(startProcess);

    });

  };

  $.fn.cdm_ws_progress.defaults = {// set up default options
      background_color:	"#F3F3F3",
      indicator_color:	"#D9EAF5",
      width: 				"100%",
      bar_height: 		"1.5em",
      border:				"1px solid #D9EAF5",
      padding:			"1em",
      isBlocking:			false
  };

})(jQuery);
