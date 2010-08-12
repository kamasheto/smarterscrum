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

function sprintLoad(el)
{
	var url = $('#theLoadedContent').contents().find('#sprintTable #task_'+el).first().attr('name');
	alert(url);
	$('#theLoadedContent').contents().find('body:first').append('<div id="sprintTemp" style="display:none"><div/>');
	$('#theLoadedContent').contents().find('#sprintTemp:first').load(url+' #sprintTable #task_'+el,function(){
		$('#theLoadedContent').contents().find('#sprintTable #task_'+el).first().replaceWith($('#theLoadedContent').contents().find('#sprintTemp:first').html());
		$('#theLoadedContent').contents().find('#sprintTemp:first').remove();
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
	});
}