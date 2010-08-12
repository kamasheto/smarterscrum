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