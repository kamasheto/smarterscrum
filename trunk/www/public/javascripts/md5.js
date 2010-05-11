/**
* @author mahmoudsakr
*/

function md5( str, id )
{
	$.post( '/application/md5?str='+str, '', function(data){
		$(id).val(data);
	});
}