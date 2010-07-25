﻿$(function() {
	$('.draggable').live('mouseover', function() {
			$(this).draggable({handle: '.ui-widget-header',cancel: 'img'});
			var h = $(this).height();
			var w = $(this).width();
			$(this).resizable({
				containment: '#container',
				minHeight: h,
				minWidth: w,
				autoHide: true
			});
	});

	
		$(".draggableChild").live('mouseover',function(){
	
		$(this).draggable({handle: '.ui-widget-header',cancel: 'img',stop: function(event, ui){
			var x = $(this).attr('class').split(" ");
			var is = false;
			for(var i =0; i<x.length;i++)
			{
				if(x[i]=='draggableChild')
				is = true;
			}
			if(is)
			{
				var el = $(this);
				var pos = el.position();
				var pos2 = el.closest('.draggable').position();
    			el.css({
    				position: "absolute",
    				marginLeft: 0,
    				marginTop: 0,
    				top: pos2.top+pos.top, 
    				left: pos2.left+pos.left 
    			});		
				$("#container").append($(this));
			}
			//alert($(this).find('.ui-widget-content').children());
			//if($(this).find('.ui-widget-content').children().first().attr('class')=='loading')
			//{//alert('');
				//$(this).find('.ui-widget-content').first().load($(this).attr('name'));
			//}
			$(this).draggable({start:function(event,ui){},stop:function(event,ui){}});
			$(this).children().show();
			$(this).removeClass('draggableChild');
			$(this).addClass('draggable');

		},
		start:function(event,ui){
			//alert('');
			var id= $(this).attr('id');
			var t = $('#'+id+' .ui-widget-header').first().clone();
			$(t).css('background','#333');
			$(t).addClass('clone');
			$(t).insertBefore($(this));
			$(t).attr('id',$(this).attr('id')+"_2");
		}});
	
	
	});


	$('.min').live('click',function(){
		$(this).parent().next().slideToggle();
	});
	
	$('.revertFrom').live('click',function(){
		var theId =  $(this).parent().parent().attr('id');
		var theSecondId = theId+"_2";
		//alert($('#'+theId).attr('id'));
		if($('#'+theSecondId).attr('id')==null)
			$('#'+theId).remove();
		else
		{
		$('#'+theId).removeAttr('style');
		$('#'+theId).removeClass('draggable');
		$('#'+theId).addClass('draggableChild');
		$('#'+theSecondId).replaceWith($('#'+theId));
		}
	});
});
function load(url,el)
{
	$('#'+el+'_content').slideDown(400);
	
	$('#'+el+'_content').load(url,function(){
		$('#'+el+' .min').first().show();
		$('#'+el+'_content').children().show();
		$('#'+el+' .loading').first().hide();

	});
}


function loadBox(url,el)
{
	$('#container').load(url,function(){
		$('#'+el+' .loading').first().hide();
	//	alert($('#'+el+'_content').find('.loading').first().attr('class'));
	});
}
