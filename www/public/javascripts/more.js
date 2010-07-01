function import_permissions(role_id) {
	if (role_id > 0)
	{
		$('#loading').show();
		$.post('/roles/getpermissions?id='+role_id,
			function(data) {
				$('select#object_permissions > option').each(function() {
					$(this).attr('selected', false);
				});
				$.each(data.permissions, function(index, item) {
					$('select#object_permissions > option[value="'+item.id+'"]').attr('selected', true);
				})
				$('#loading').hide();
			});
	}
}

function check_all_perms() {
	$('select#object_permissions option').each(function(){
		$(this).attr('selected', true);
	});
}

function uncheck_all_perms() {
	$('select#object_permissions option').each(function(){
		$(this).attr('selected', false);
	});
}


//Simple JavaScript Templating
//John Resig - http://ejohn.org/ - MIT Licensed
(function(){
var cache = {};

this.tmpl = function tmpl(str, data){
 // Figure out if we're getting a template, or if we need to
 // load the template - and be sure to cache the result.
 var fn = !/\W/.test(str) ?
   cache[str] = cache[str] ||
     tmpl(document.getElementById(str).innerHTML) :
  
   // Generate a reusable function that will serve as a template
   // generator (and which will be cached).
   new Function("obj",
     "var p=[],print=function(){p.push.apply(p,arguments);};" +
    
     // Introduce the data as local variables using with(){}
     "with(obj){p.push('" +
    
     // Convert the template into pure JavaScript
     str
       .replace(/[\r\t\n]/g, " ")
       .split("<%").join("\t")
       .replace(/((^|%>)[^\t]*)'/g, "$1\r")
       .replace(/\t=(.*?)%>/g, "',$1,'")
       .split("\t").join("');")
       .split("%>").join("p.push('")
       .split("\r").join("\\'")
   + "');}return p.join('');");

 // Provide some basic currying to the user
 return data ? fn( data ) : fn;
};
})();
function overlayOpen(href)
{
	$('#getOverlay').load(href, function(){
		$('.formatDate').each(function(){
			$(this).html( formatDate( new Date(getDateFromFormat($(this).html(),'yyyy-MM-dd HH:mm:ss')), 'd MMM, yyyy') );
		});
		$('.formatTime').each(function(){
			$(this).html( formatDate( new Date(Number($(this).html())), 'd MMM, yyyy hh:mma') );
		});

	    $("a").tipTip({delay:0});
	    $("td").tipTip({delay:0});
	    $("span").tipTip({delay:0});
    
	    $("button").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $("a button").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $("input[type='submit']").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $("input[type='button']").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $("input[type='ok']").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $("input[type='add']").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $("input[type='send']").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only",0);
	    $('div.crudField').each(function(){
				if ($(this).html().trim() == '') {
					$(this).remove();
				}
		    });
	}).show();
	
}

function overlayClose()
{
	$('#getOverlay').hide();
}