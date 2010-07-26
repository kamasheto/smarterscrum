$(function() {
	$('.draggable').live('mouseover', function() {

			$(this).data('init',1);
			$(this).draggable({handle: '.ui-widget-header',cancel: 'img'});
			var h = $(this).height();
			var w = $(this).width();
			$(this).resizable({
				containment: '',
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
	    			$(this).closest('.workspaceContainer').append($(this));
				}
				$(this).draggable({start:function(event,ui){},stop:function(event,ui){}});
				$(this).children().show();
				$(this).removeClass('draggableChild');
				$(this).addClass('draggable');
				if(!$(this).data('loaded')&&$(this).find('.ui-widget-content').first().html()=='')
				{
					load($(this).attr('name')+' .actual',$(this).attr('id'));
					$(this).data('loaded',9);
				}
	
			},
			start:function(event,ui){
				var id= $(this).attr('id');
				var t = $('#'+id+' .ui-widget-header').first().clone();
				$(t).removeClass('ui-widget-header');
				$(t).addClass('clone');
				$(t).insertBefore($(this));
				$(t).attr('id',$(this).attr('id')+"_2");
			}});
		
	});


	$('.min').live('click',function(){
		
		if(!$(this).parent().parent().data('loaded')&&$(this).parent().next().html()=='')
		{
			load($(this).parent().parent().attr('name')+' .actual',$(this).parent().parent().attr('id'));
			$(this).parent().parent().data('loaded',9);
		}
		
			$(this).parent().next().slideToggle(400);
		
	});
	
	$('.revertFrom').live('click',function(){
		var theId =  $(this).parent().parent().attr('id');
		var theSecondId = theId+"_2";
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

	$('#'+el+'_content').load(url,function(){
		$('#'+el+' .min').first().show();
		$('#'+el+'_content').children().show();
		$('#'+el+' .loading').first().hide();
		$('#'+el+'_content').slideDown(400);
magic(el);
	});
}

function loadBox(url,el)
{
	$('#'+el).append('<div style="position:absolute"id="myTemp"></div>');
	
	$('#'+el+' #myTemp').load(url,function(){

		$('#'+el+' #myTemp').attr('id','');
	});
}

function magic(id)
{
	$("#"+id+"_content div[name]").each(function(){	
		if($(this).attr('name')!=null)
		{
		$(this).load($(this).attr('name')+' .ui-widget-header',function(){

			$(this).addClass('ui-widget-content draggableChild');
			var id = $(this).children().first().attr('id');
			$(this).attr('id',id);
			$(this).append('<div id="'+id+'_content" class="ui-widget-content" ></div>');
			
			});
		//alert($(this).attr('class'));
		}
	});		
}

