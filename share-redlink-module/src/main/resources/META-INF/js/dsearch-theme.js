(function ($) {

AjaxSolr.theme.prototype.result = function (doc, snippet) {
  var output = '<div><h2>' + doc.stanbolreserved_title + '</h2>';
  output += '<p id="links_' + doc.stanbolreserved_id + '" class="links"></p>';
  output += '<p>' + snippet + '</p></div>';
   return output;
};

AjaxSolr.theme.prototype.snippet = function (doc) {
  var output = 'Date: ' + doc.stanbolreserved_creationdate + "\nEnhancements: " + doc.stanbolreserved_enhancementcount + "\nContent: " + Base64.decode(doc.stanbolreserved_binarycontent) ;
  return output;
};

AjaxSolr.theme.prototype.tag = function (value, weight, handler) {
  return $('<a href="#" class="tagcloud_item"/>').text(value).addClass('tagcloud_size_' + weight).click(handler);
};

AjaxSolr.theme.prototype.facet_link = function (value, handler) {
  return $('<a href="#"/>').text(value).click(handler);
};

AjaxSolr.theme.prototype.no_items_found = function () {
  return 'no items found in current selection';
};

})(jQuery);