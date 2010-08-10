 $.extend($.gritter.options, { 
	fade_in_speed: 50, // how fast notifications fade in (string or int)
	fade_out_speed: 300, // how fast the notices fade out
	time: 5000 // hang on the screen for...
});

var CURRENT_PROJECT = 0;

function request_accept( id, hash )
{
	$.post('/requests/requestAccept' ,
		    {hash:hash, id:id} ,
		    function(){
				reload('project-requests')
		    })
}
function request_accept2( id, hash )
{
	$.post('/requests/deletionRequestAccept' ,
		  {hash:hash} ,
		    function(){
				reload('project-requests')
		    })
}
function request_ignore( id, hash)
{
	$.post('/requests/requestIgnore' ,
		    {hash:hash} ,
		    function(){
				reload('project-requests')
		    })
		}
function show_comment(id){

	$('#comment_'+id).show();
	}
function do_ignore(id, hash){
	var textValue = $('#text_'+id).val();
	if(textValue == '')
		$.bar({message:'You must Enter a reason why you declined this request!'})
	else
	{
		$.post('/requests/requestIgnore' ,
		    {hash:hash, body:textValue} ,
		    function(){
				reload('project-requests')
		    })
	}
}
function delete_meeting(id)
{
	var confirmation= confirm("Are you sure you want to delete this meeting ?");
	
	if (confirmation) {
		$.post('/Meetings/deleteMeeting', {
			id:id
		}, function(){
			reload('meetings');
			reload('meeting-'+id)
		});
	};		
}

function join_meeting(id)
{
	if(confirm("Join meeting?"))
	$.post('/Meetings/joinMeeting',{meetingID: id}
	,function(flag)
	{
		if(flag)
			{
			$.bar({ message:'You have successfully joined the meeting.' })
		$('#accepts_'+id).hide();
		$('#acceptsMeetings_'+id).hide();
		$('#confirmedd_'+id).show();
		$('#confirmeddMeetings_'+id).show();
			}
		else
			{
			$.bar({
				message : 'Meeting has already ended.'
			});
			$('#accepts_'+id).hide();
			$('#acceptsMeetings_'+id).hide();
			}
	})
}

function confirm_me(id)
{
	if(confirm("Change your status to attending?"))
	$.post('/MeetingAttendances/confirmAttendance',{meetingId: id}
	,function(flag)
	{
		if(flag)
			{
		$.bar({
			message : 'You accepted the invitation.'
		});
		$('#inv_'+id).hide();
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
		$('#confirmed_'+id).show();
		$('#inv_'+id).hide();
		$('#meet_'+id).hide();
		$('#acceptMeetings_'+id).hide();
		$('#declineMeetings_'+id).hide();
		$('#confirmedMeetings_'+id).show();
			}
		else
			{
			$('#inv_'+id).hide();
			$('#accept_'+id).hide();
			$('#decline_'+id).hide();
			$('#meet_'+id).hide();
			$('#acceptMeetings_'+id).hide();
			$('#declineMeetings_'+id).hide();
			
				$.bar({
					message : 'Meeting has already ended.'
				});
			}

	})
}

function requestRole(roleIdd){
	$.post('/projecttasks/requestRole', {id:roleIdd}, function(msg){
		$.bar({message:msg});
		reload('roles')
	});
}

function revokeRole(roleId, baseRole, userId, projectId)
{
	var confirmation;
	if(baseRole)
	{
		confirmation= confirm("Are you sure you want to revoke this role? Notice that by revoking this role the user is no longer a member in this project.");
	}
	else
	{
		confirmation= confirm("Are you sure you want to revoke this role?");
	}
	if (confirmation) 
	{
		$.post('/projectTasks/revokeRole', {roleId:roleId, userId:userId}, 
				function(msg)
				{
			      arr = msg.split('|')
			      if (arr.length > 0 && arr[0]) 
			      {
			    	  $.bar({message: arr[0]});
			      }
			      if (arr.length > 1 && arr[1]) 
			      {
			    	  eval(arr[1]);
			      }
			      if (arr.length > 2 && arr[2]) 
			      {
			    	  eval(arr[2]);
			      }
			    });
	};
}

function deleteRequest(roleIdd){
	if(confirm("Are you sure you want to cancel your request?")){
		$.post('/requests/removeRequestRoleInProject', {roleId:roleIdd}, function(msg){
			$.bar({message:msg});
			reload('roles')
		});
	}
}

function requestToBeReviewer(taskTypeId){
	$.post('/requestreviewers/requestToBeReviewer', {ID:taskTypeId}, function(msg){
		$.bar({message:msg});
		reload('reviewer-requests')
	});
}

function removeRequestToBeReviewer(id){
	$.post('/requestreviewers/removerequest', {taskTypeId:id}, function(msg){
		$.bar({message:msg});
		reload('reviewer-requests')
	});
}

function decline_me(id)
{
	var reno=prompt("Please enter the reason :","");
	while(reno.length==0)
	{
		reno=prompt("Please enter the reason :","");
	}
	$.post('/MeetingAttendances/declineAttendance',{meetingId:id , reason:reno},
	function(flag)
	{
		if(flag)
			{
		$.bar({
			message : 'You Declined the invitation.'
		});
		$('#inv_'+id).hide();
		$('#declined_'+id).show();
		$('#accept_'+id).hide();
		$('#decline_'+id).hide();
		$('#meet_'+id).hide();
		$('#declinedMeetings_'+id).show();
		$('#acceptMeetings_'+id).hide();
		$('#declineMeetings_'+id).hide();
			}
		else
			{
			$.bar({
				message : 'Meeting has already ended.'
			});
			$('#inv_'+id).hide();
			$('#accept_'+id).hide();
			$('#decline_'+id).hide();
			$('#meet_'+id).hide();
			$('#acceptMeetings_'+id).hide();
			$('#declineMeetings_'+id).hide();
			}
	
	}
	);
}


function deleteStory(sId,box,d){
	message = "Are you sure you want to delete this story?";
	if(d)
		message+= " Some stories depend on it.";
	if((confirm(message)))
	{
		$.post('/Storys/delete', {id:sId}, function(data){
			$.bar({message:data});
			// removeMe(box);
		});
	}
}

DRAGGING_ELEMENT = null
$(function() {
	$('.dragger').live('mouseover', function(){
		if ($(this).data('init')) return
		$(this).data('init', true)
		$(this).draggable({
			helper: 'clone',
			zIndex: 2700,
			start: function(event, ui) {
				DRAGGING_ELEMENT = $(this).closest('.draggableChild').attr('id')
				if (!DRAGGING_ELEMENT) {
					DRAGGING_ELEMENT = $(this).closest('.draggable').attr('id')
				}
				that = this
				$('.dropper').each(function() {
					$(this).show()
					if ($(this).data('init')) return
					$(this).data('init', true)
					$(this).droppable({
						addClasses: false,
						tolerance: 'touch',
						drop: function(event2, ui2) {
							$(this).attr('src', '/public/images/loading16.gif')
							// alert($(ui.helper).attr('name'))
							$.ajax({
								url: '/ajax/dynamicdrop',
								data: {from: $(that).attr('name'), to: $(this).attr('name')},
								success: function(response) {
									arr = response.split('|')
									if (arr.length > 0 && arr[0]) {
										$.bar({message: arr[0]})
									}	
									if (arr.length > 1 && arr[1]) {
										eval(arr[1])
									}
								}, 
								error: function(response) {
									$.bar({
										message: 'An error occurred. You may not have permission to perform that action.'
									})
								}
							})
						}
					})
				})
			},
			stop: function(element, ui) {
				$('.dragger').hide()
				$('.dropper').hide().attr('src', '/public/images/famfam/arrow_in.png')
				$(ui.helper).remove()
				DRAGGING_ELEMENT = null
			}
		})
	})
	
	
	$('.draggable').live('mouseover', function() {
		if (!DRAGGING_ELEMENT) {
			$(this).children().children('.dragger').show()					
		}
		if ($(this).data('init')) return
		$(this).data('init', 1);
		var con = $(this).closest('.workspaceDraggables').attr('id');
		$(this).draggable( {
			handle : '.ui-widget-header',
			cancel : 'img',
			stack  : '.draggable',
			containment: '#'+con
		});
		$(this).resizable({minWidth:300,containment: '#'+con});
	}).live('mouseout', function() {
		if ($(this).attr('id') != DRAGGING_ELEMENT) {
			$(this).children().children('.dragger').hide()
		}
	});
		
	$(".draggableChild").live('mouseout', function() {
		if ($(this).attr('id') != DRAGGING_ELEMENT) {
			$(this).children().children('.dragger').hide()
		}
	}).live(
			'mouseover',
			function() {
				if (!DRAGGING_ELEMENT) {
					$(this).children().children('.dragger').show()					
				}
				if ($(this).data('init')) return
				$(this).data('init', true)
				$(this).draggable(
						{
							handle : '.ui-widget-header',
							cancel : 'img',
							stop : function(event, ui) {
								is = $(this).hasClass('draggableChild')							
								if (is) {
									var el = $(this);
									var pos = el.position();
									var pos2 = el.closest('.draggable')
											.position();
									el.css( {
										position : "absolute",
										marginLeft : 0,
										marginTop : 0,
										top : pos2.top + pos.top,
										left : pos2.left + pos.left
									});
									$(this).closest('.workspaceDraggables')
											.append($(this));
								}
								$(this).draggable( {start : function(event, ui) {},stop : function(event, ui) {}});
								$(this).children().show();
								$(this).removeClass('draggableChild');
								$(this).addClass('draggable');
								load($(this).attr('name'), $(this).attr('id'),1);
							},
							start : function(event, ui) {
								var id = $(this).attr('id');
								$("#" + id + " .selectedBars").each(function(){$(this).removeClass('selectedBars')});
								$("#" + id + " .selectedBars2").each(function(){$(this).removeClass('selectedBars2')});
								$("#" + id + " .selectedBar").each(function(){$(this).removeClass('selectedBar')});
								$("#" + id + " .selectedBar2").each(function(){$(this).removeClass('selectedBar2')});
								$('#' + id + ' .ui-widget-content').hide();
								var t = $('#' + id + ' .ui-widget-header')
										.first().clone();
								$(t).removeClass('ui-widget-header');
								$(t).addClass('clone');
								$(t).removeClass('selectedBar');
								$(t).removeClass('selectedBar2');
								$(t).insertBefore($(this));
								$(t).attr('id', $(this).attr('id') + "_2");
							}
						});

			});

	$('.draggableChild .ui-widget-header').live(
			'click',
			function() {
				if($(this).next().html()=='')
					load($(this).parent().attr('name'),$(this).parent().attr('id'), 3);

				if($(this).parents().length%4==0)
				{
					$(this).toggleClass('selectedBar');
					$(this).next().toggleClass('selectedBars');
				}
				else
				{
					$(this).toggleClass('selectedBar2');
					$(this).next().toggleClass('selectedBars2');
					
				}
				$(this).next().slideToggle(400);

			});

	$('.min').live(
			'click',
			function() {
				if($(this).parent().next().html()=='')
					load($(this).parent().parent().attr('name'),$(this).parent().parent().attr('id'),2);
					$(this).parent().next().slideToggle(400);
					if($(this).parent().parent().hasClass('draggable'))
					$(this).parent().next().next().toggle();
			});
	$('.refresh').live('click',function(){
		var parent = $(this).parent().parent();
		// better? Lol
		$(parent).find('.actual:first').html('<div class="bar center"><img src="/public/images/loadingMagic.gif"></div>')
		load($(parent).attr('name'), $(parent).attr('id'), 2);
	});
	$('.revertFrom').live('click', function() {
		$(this).parent().parent().data('init', false);
		var url = $(this).parent().parent().attr('name');
		$(this).parent().next().slideUp();
		removeFromDiv(url);
		// CURRENT_OFFSET -= 315
		var theId = $(this).parent().parent().attr('id');
		var theSecondId = theId + "_2";
		if ($('#' + theSecondId).attr('id') == null)
			$('#' + theId).remove();
		else {
			var elHtml = $('#' + theSecondId).html();
			$('#' + theId).removeAttr('style');
			$('#' + theId).removeClass('draggable');
			$('#' + theId).addClass('draggableChild');
			$('#' + theSecondId).replaceWith($('#' + theId));
			$('#' + theId).find('.mainH').first().html(elHtml);
		}
	
		});
	});

		
function removeFromDiv(url)
{
	myDivs = $.grep(myDivs, function(value) {
	    return value != url;
	});
	var url2 = url+' .actual';
	myDivs = $.grep(myDivs, function(value) {		
	    return value != url2;
	});	
}

randomId = 100	
function load(url, el, n) {
	$('#' + el + '_content').html('<div class="bar center"><img src="/public/images/loadingMagic.gif"></div>');
	// if ($.inArray(url,myDivs) == -1 || n == 2) {
	$.ajax({
		url: url,
		success: function(data) {
			if (n != 3) {
				$('#'+el+'_header').html($(data).find('.mainH:first').html());
			} 
			$('#'+el+'_content').html($(data).find('.actual:first').html());
			if (n == 1) {
				$('#'+ el).append('<div class="filter" id="'+el+'_filter"></div>');
				$('#'+ el + '_filter').html($(data).find('.filter:first').html());
				$('#'+ el + '_filter').find('input:first').attr('name', 'filter_textBox_'+el);
			}
			magic(el)
			$('#' + el + '_content').slideDown(400);
		},
		error: function(data) {
			$.bar({message: 'An error has occured. Please try again.'})
		}
	})
	// }
	if(n==1){	
		myDivs.push(url);
	}
}

// CURRENT_OFFSET = 10
function loadBox(url, el, classes) 
{
	if($.inArray(url,myDivs)==-1) {
		$.post(url, function(data) {

			element = $(data).filter('div:first')
			element.attr('name', url)
			element.addClass(classes)
			element.css('z-index','4');
			element.find('.bar:first').remove()
			element.find('.actual:first').show()
			$('#'+el).append(element)
			magic(element.attr('id'))
		})
	}
}

function magic(id) {
	doOnLoad();
	smart_pagination(id,1);
	hideFilterLinks(id);
	$("#" + id + "_content div[name]").each(
	function() 
	{
						if($(this).attr('class')=='overlay')
						{
							var id2 = "ui" +num;
							num++;
							var head = '<div id="'+id2+'_header" class="ui-widget-header"><a href="#" onclick="overlayOpen(\''+$(this).attr('name')+'\')"><span class="ui-icon ui-icon-extlink"></span></a>' + $(this).html()+ '</div>';
							$(this).html(head);
							$(this).attr('id', id2);
							
						}
		else
		{
						var url = $(this).attr('name');
			if($.inArray(url,myDivs)==-1)
			{
						var id2 = "ui" +num++;
						var head = '<div id="'+id2+'_header" class="ui-widget-header mainH"><span class="revertFrom"><span class="ui-icon ui-icon-circle-close"></span><span class="refresh ui-icon ui-icon-refresh"></span></span>' + $(this).html() + '</div>';
						$(this).html(head);
						$(this).addClass('draggableChild');

						$(this).attr('id', id2);
				$(this).append('<div id="' + id2 + '_content" class="ui-widget-content actual" ></div>');
			} else {	
				var id2 = 						
				$($(this).closest('.workspaceDraggables').find('div[name='+$(this).attr('name')+']')).first().attr('id')+'_2';
							var head = '<div id="'+id2+'_header" class="ui-widget-header mainH">' + $(this).html() + '</div>';
							$(this).html(head);
							$(this).addClass('clone');

							$(this).attr('id', id2);	
						}
						
						}
					});

}
var myDivs = new Array();
var num =1;
function deleteTheTask(tId, box){
	
	if((confirm('Are you sure you want to delete the Task ?')))
	{
	
	$.post('/tasks/delete', {id:tId}, function(data){ $.bar({message:data}
	);
	// removeMe(box);
	;});
	}
}

function set_attended(id)
{
	if(confirm("change status to attended??"))
	$.post('/meetingAttendances/setConfirmed',{id: id}
	,function()
	{
		$('#reason_span_'+id).hide();
		$('#attendance_of_user_'+id).html("attended");
	})
}

function set_didnot_attend(id)
{
	var reno=prompt("Please enter the reason :","");
	while(reno.length==0)
	{
		reno=prompt("Please enter the reason :","");
	}
	
		$.post('/meetingAttendances/setDeclined',{id:id , reason:reno},
		function()
		{
			$('#reason_span_'+id).show();
			$('#attendance_of_user_'+id).html("did not attend");
			$('#reason_of_user_'+id).html(reno);
				
	})
	
}
searching_projects = null
function search_projects() {
	if ($('#project_search_text').val() == '') {
		$('#projects_search_results').html('')
		$('#search_results_h3').hide()
		return
	}
	$('#projects_search_results').html('<img src="/public/images/loadingMagic.gif">')
	if (searching_projects) {
		searching_projects.abort()
	}
	searching_projects = $.post('/ajax/projects', {query: $('#project_search_text').val(), notMine: true}, function(projects) {
		results = ''
		$(projects).each(function() {
			results += '<div id="project-search-result-'+this.id+'"><a onclick="showProjectWorkspace('+this.id+')">'+this.name+'</a></div>'
		})
		if (!$(projects).length) {
			results += '<div><a href="#" title="No results">--</a></div>'
		}
		$('#search_results_h3').show()
		$('#projects_search_results').html(results)
	})
}

function show(id) {
	if (!id) {
		$('#workspaces').hide()
		$('#normal').show()
		$('.project-button').removeClass('selectedADiv')
	} else if ($('.workspace-'+id).length) {
		CURRENT_PROJECT = id;
		// make sure we have that thingie first
		// DO NOT load workspace here, it might have been removed on purpose!
		$('#normal').hide();
		$('#workspaces').show()
		$('.workspace').hide()
		$('.workspace-' + id).show()
		$('.project-button').removeClass('selectedADiv')
		$('#project-button-'+id).addClass('selectedADiv')	
	}
}
function closeWindows(el)
{
	$('#workspace-'+el+' div[name]').each(function(){
		removeFromDiv($(this).attr('name'));
		$(this).remove();
	});
	$('#chatContainer_'+el).hide();
}

function close_workspace(project_id) {
	$('#project-button-'+project_id).fadeOut(function() {
		$(this).remove()
	})
	closeWindows(project_id)
	$('.workspace-'+project_id).remove()
	show(0)
}


function showProjectWorkspace(project_id) {
	if ($('.workspace-'+project_id).length) {
		// workspace already loaded, just show it instead
		$('#top_header_projects_pane').slideUp()
		show(project_id)
		return
	}
	$('#workspaces').append('<div class="workspace workspace-'+project_id+'"><div style="width:32px;margin:auto;margin-top:200px;"><img src="/public/images/loadingMagic.gif"></div></div>')
	show(project_id)
	// return
	$.ajax({
		async: false,
		url: '/show/workspace',
		data: {id: project_id},
		success: function(data) {
			$('#project-tabs').append('<a class="aDIV topCornersRounded selectedADiv project-button " id="project-button-'+project_id+'" href="#" onclick="show('+project_id+')" style="width: 120px !important" title="">'+$(data).find('.project_name_in_header').html()+' <span class="right ui-icon2 ui-icon-circle-close" onclick="close_workspace('+project_id+')"> </span></a>');
			$('.workspace-' + project_id).html(data)
			$('#top_header_projects_pane').slideUp()
		}
	})
}

function reload() {
	// don't reload the same box more than once, just making sure nobody is
	// abusing our system
	reloaded = new Array()
	for (var i = 0; i < arguments.length; i++) {
		sel = '.reload-' + arguments[i]
		if ($.inArray(sel, reloaded) != -1) {
			continue
		}
		reloaded.push(sel)
		div = $(sel, '#workspace-' + CURRENT_PROJECT)
		div.each(function() {
			url = div.attr('name')
			removeFromDiv(url)
			// alert('Reloading: ' + url)
			exit = false
			$.ajax({
				url: url,
				async: false,
				error: function() {
					exit = true
				}
			})
			if (exit) {
				div.remove()
				return
			}
			load(url, div.attr('id'), 3)	
		})
	}
}
function deleteTheComponent(cId, box){
	if((confirm('Are you sure you want to delete the component?')))
	{
		$.post('/components/delete', {id:cId}, function(data){ 
				$.bar({message:data});
				// removeMe(box);
				reload('components', 'component-'+cId)
			});
	}
}