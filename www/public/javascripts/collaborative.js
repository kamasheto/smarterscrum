var ping = function() {
	$.getJSON('/collaborate', function(resp) {
		$(resp.news).each(function(){
			$.gritter.add({
				title: 'New Notification',
					text: '<a href = "/show/user?id='+this.receiver.id+'">'+ this.actionPerformer.name +'</a> has '+this.actionType+'ed'+ this.resourceType+': <a href = '+this.resourceURL+'>'+this.resourceName+'</a>',
				image: this.importance > 0 ? '/public/images/tick.png' : this.importance < 0 ? '/public/images/cross.png' : '/public/images/error.png',
				sticky: false,
				time: '10000'
			})
		})
		
		str = '';
		$(resp.online_users).each(function() {
			if (this.isAdmin) {
				this.name = '<span class="isAdmin">' + this.name + '</span>';
			}
			str += '<a href="/show/user?id='+this.id+'">' + this.name + '</a> • ';
		});
		
		$('#onlineUsers').html(str.substring(0,str.length-3));
		
		setTimeout('ping()', 2000)
	})
}