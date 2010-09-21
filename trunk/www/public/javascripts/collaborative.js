last_update = new Date().getTime();
var ping = function() {
	$.getJSON('/collaborate?lastUpdate=' + last_update + '&currentlyOnline=' + get_online_users(), function(resp) {
		last_update = new Date().getTime()
		
		$(resp.news).each(function(){
			// $.gritter.add({
			// 	title: 'New Notification',
			// 		text: this.actionPerformer.name +' has '+this.actionType+'ed'+ this.resourceType+': <a href = '+this.resourceURL+'>'+this.resourceName+'</a>',
			// 	image: this.importance > 0 ? '/public/images/tick.png' : this.importance < 0 ? '/public/images/cross.png' : '/public/images/error.png',
			// 	sticky: false,
			// 	time: '10000'
			// })
		})
		
		str = '';
		$(resp.online_users).each(function() {
			isAdmin = this.isAdmin ? 'systemAdmin' : '';
			str += '<span class="online-user '+isAdmin+'" user_id="'+this.id+'">' + this.name + '</span>, ';
		});
		
		$('#online_users_list').html(str.substring(0,str.length-2));
		
		
		$(resp.updates).each(function() {
			eval(this.javascript)
		})
		
		// ping again
		ping()
	})
}

function get_online_users() {
	list = ''
	$('.online-user').each(function() {
		list += $(this).attr('user_id') + ','
	})
	return list
}