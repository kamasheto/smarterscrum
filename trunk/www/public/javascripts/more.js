
function setBaseRole(role) {
	$('p').each(function(){
		$(this).removeClass('baseRole')
	})	
	$.post('/roles/setbaserole?id=' + role, function() {
		$('#role_p_' + role).addClass('baseRole')
		$.bar({message:'Base role updated successfully'})
	})
}

function import_permissions(role_id) {
	if (role_id > 0)
	{
		$('#loading').show();
		$.post('/roles/getpermissions?id='+role_id,
			function(data) {
				$('select#object_permissions option').each(function() {
					$(this).attr('selected', false);
				});
				$.each(data.permissions, function(index, item) {
					$('select#object_permissions option[value="'+item.id+'"]').attr('selected', true);
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


$(document).keyup(function(e) {
  if (e.keyCode == 27) { overlayClose() }   // esc
});
	
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
	$('#theLoadedContent').attr('src',href);
	$('#getOverlay').show();	
}

function overlayClose()
{
	$('#getOverlay').hide();
}

/** auto iframe height do not remove **/
function doIframe(){
	o = document.getElementsByTagName('iframe');
	for(i=0;i<o.length;i++){
		if (/\bautoHeight\b/.test(o[i].className)){
			setHeight(o[i]);
			addEvent(o[i],'load', doIframe);
		}
	}
}

function setHeight(e){
	if(e.contentDocument){
		e.height = e.contentDocument.body.offsetHeight + 35;
	} else {
		e.height = e.contentWindow.document.body.scrollHeight;
	}
}

function addEvent(obj, evType, fn){
	if(obj.addEventListener)
	{
	obj.addEventListener(evType, fn,false);
	return true;
	} else if (obj.attachEvent){
	var r = obj.attachEvent("on"+evType, fn);
	return r;
	} else {
	return false;
	}
}

if (document.getElementById && document.createTextNode){
 addEvent(window,'load', doIframe);	
}

