function doOnLoad() {
		$('.formatDate').each(function(){
			if (!$(this).data('processed')) {
				$(this).data('processed', true)
				$(this).html( formatDate( new Date(getDateFromFormat($(this).html(),'yyyy-MM-dd HH:mm:ss')), 'd MMM, yyyy') );	
			}
		});
		$('.formatTime').each(function(){
			if (!$(this).data('processed')) {
				$(this).data('processed', true)
				$(this).html( formatDate( new Date(Number($(this).html())), 'd MMM, yyyy hh:mma') );	
			}
		});
	    $('div.crudField').each(function(){
				if ($(this).html().trim() == '') {
					$(this).remove();
				}
		    });
}
function parent_message_bar(message) {
	window.parent.$.bar({
		message: message
		});
}
function sprintLoad(el,el2)
{

	var url = $('#theLoadedContent').contents().find('#sprintTable #task_'+el).first().attr('name');
	//alert(url);
	$.ajax({
		async: false,
		url: url,
		success: function(data) {
		var newData = $(data).find('#sprintTable #task_'+el+" #"+el2).first();
			$('#theLoadedContent').contents().find('#sprintTable #task_'+el+" #"+el2).first().replaceWith(newData);
			$('#theLoadedContent').contents().find(".enterEffort").each(function()
					{
						if($(this).html()!='')
							$(this).html(parseInt($(this).html()));
					});

			$('#theLoadedContent').contents().find(".editPOINTS").each(function()
					{
				if($(this).html()!='')
							$(this).html(parseInt($(this).html()));
					});
		}
	})

}

function hide()
{
	window.parent.$('#overlayLoading').show();
	window.parent.$('#theLoadedContent').hide();
	window.parent.$('#getOverlay').hide();
}

$(document).keyup(function(e) {
	if (e.keyCode == 27) { 
		hide()
	}   // esc
})