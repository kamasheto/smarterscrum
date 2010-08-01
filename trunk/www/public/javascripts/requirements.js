function doOnLoad() {
	
	$('.dim').live('mouseover', function() {
		$(this).click(function() {	
		})
	})
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
	
	    $("a").tipTip({delay:0});
	    $("td").tipTip({delay:0});
	    $("span").tipTip({delay:0});
	 $("div").tipTip({delay:0});
	 $("img").tipTip({delay:0});
	    $('div.crudField').each(function(){
				if ($(this).html().trim() == '') {
					$(this).remove();
				}
		    });
}