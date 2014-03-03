/**
 * Submit Search Event. Search documents annotated by entities with typed label
 */
$(document).ready(function(){

	/* EntityHub Parameters */
	var url = "http://localhost:9080/entityhub/site/dbpedia/find";
	var ldpath = "name = rdfs:label[@en] :: xsd:string; comment = rdfs:comment[@en] :: xsd:string; categories = dc:subject :: xsd:anyURI; mainType = rdf:type :: xsd:anyURI;";
	
	$("#concept").autocomplete({
		minLength:3,
		source: function(request, response){
			var suggestions = [];
			$.ajax({
				url: url,
				type: "post",
				data: { name:request.term+"*", lang:"en", limit:10, offset:0, ldpath:ldpath },
				contentType : "application/x-www-form-urlencoded",
				accepts: {"application/rdf+json": "application/rdf+json"},
				dataType: "json",
				cache: false,
			}).done(function( data ) {
				// [ { label: "Choice1", value: "value1" }, ... ]
				_.each(data.results, function(entity){
					if(entity.id.indexOf("Category") == -1 && entity.id.indexOf("http://dbpedia.org/resource/") != -1){
						var truncatedId = decodeURI(entity.id.replace("http://dbpedia.org/resource/", ""));
						truncatedId = truncatedId.replace("(", "");
						truncatedId = truncatedId.replace(")", "");
						if(entity.mainType){
							var mainType = _.find(entity.mainType, function(type){ return type.value.indexOf("http://dbpedia.org/ontology") != -1;});
							if(mainType){
								var concept = mainType.value.replace("http://dbpedia.org/ontology/", "");
								suggestions.push({label:truncatedId + " (" + concept + ")", value:entity.id, entity:entity});
							}
						}
					}
				});
				response(suggestions);
			});
		},
		select: function(e, ui) {
			e.preventDefault();
			$("#concept").val(ui.item.label);
			disambiguatedSearch(ui.item.entity);
		}
	});
		
	$("#search").submit(function(event) {
		
		/* stop form from submitting normally */
		event.preventDefault(); 
		
		var name = $("#concept").val();
		
		$.ajax({
			url: url,
			type: "post",
			data: { name:name, lang:"en", limit:10, offset:0, ldpath:ldpath },
			contentType : "application/x-www-form-urlencoded",
			accepts: {"application/rdf+json": "application/rdf+json"},
			dataType: "json",
			cache: false,
		}).done(function( data ) {
			_.each(data.results, function(entity){
				disambiguatedSearch(entity);
			});

		}
			);
	
	});
});

function disambiguatedSearch(entity){
	
	if(entity.id.indexOf("Category") == -1){
	
		$("#results").empty();
		
		var Manager = new AjaxSolr.Manager({
			solrUrl: 'http://localhost:9080/solr/default/spotlight/'
		});
		
		Manager.init();
		AjaxSolr.ResultWidget = AjaxSolr.AbstractWidget.extend({});
		
		var truncatedId = decodeURI(entity.id.replace("http://dbpedia.org/resource/", ""));
		truncatedId = truncatedId.replace("(", "");
		truncatedId = truncatedId.replace(")", "");
		var mainType = _.find(entity.mainType, function(type){ return type.value.indexOf("http://dbpedia.org/ontology") != -1;});
		var concept = mainType.value.replace("http://dbpedia.org/ontology/", "");
																				
		Manager.addWidget(new AjaxSolr.ResultWidget({
			id: 'result'+truncatedId,
			target: '#'+truncatedId,
			entity: entity,
			afterRequest: function () {
				if(this.manager.response.response.docs.length > 0){
					var conceptResults = "<div id='" + truncatedId + "'> Results for " + truncatedId + " (" + concept + ")</div>";
					$("#results").append(conceptResults);
				}
					
				for (var i = 0, l = this.manager.response.response.docs.length; i < l; i++) {
					var doc = this.manager.response.response.docs[i];
					$(this.target).append(AjaxSolr.theme('result', doc, AjaxSolr.theme('snippet', doc)));
				}
			}
		}));

		Manager.store.addByValue('q', 'uris:'+entity.id.replace(":", "\\:"));
		Manager.doRequest();
	}
}
