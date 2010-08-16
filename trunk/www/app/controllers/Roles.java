package controllers;

import java.util.List;

import models.Permission;
import models.Project;
import models.Role;
import models.User;
import models.Update;
import models.Log;
import play.db.jpa.JPASupport;
import play.exceptions.TemplateNotFoundException;
import play.i18n.Messages;
import play.mvc.With;

/**
 * CRUD controller to handle all roles/permissions
 * 
 * @author mahmoudsakr
 */
@With( Secure.class )
public class Roles extends SmartCRUD
{

	/**
	 * Sets the base role to a certian project and then apply the new base role
	 * to all members in the project.
	 * 
	 * @param id
	 *            which is the id of the role.
	 */
	public static void setBaseRole( long id )
	{
		Role role = Role.findById( id );
		Security.check( role.project, "manageRoles" );
		List<Role> roles = role.project == null ? Role.find( "byProjectIsNull" ).<Role> fetch() : Role.find( "byProject", role.project ).<Role> fetch();
		Role newBaseRole = null;
		for( Role r : roles )
		{
			r.baseRole = r == role;
			if( r.baseRole )
			{
				newBaseRole = r;
			}
			r.save();
		}

		if( newBaseRole.project != null )
		{
			// we're in a project
			for( User user : newBaseRole.project.users )
			{
				user.addRole( newBaseRole );
				user.save();
			}
		}
		Log.addUserLog("Set new base role", role, role.project);
		Update.update(role.project, "reload('roles')");
	}

	/**
	 * returns a role with all its permissions to be used in the views
	 * 
	 * @param id
	 *            which is the role id
	 */
	public static void getPermissions( long id )
	{
		if( id == 0 )
			renderJSON( new Object() );
		Role r = Role.findById( id );
		Security.check( r.project, "manageRoles" );
		r.project = null;
		renderJSON( r );
	}

	/**
	 * Overrides CRUD.show() that gives the view of editing a role.
	 * 
	 * @param id
	 *            the role id
	 */
	public static void show( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Project project = ((Role) object).project;
		List<Role> roles = null;
		if( project == null )
		{
			Security.check( Security.getConnected().isAdmin );
			roles = Role.find( "byProjectIsNull" ).fetch();
		}
		else
		{
			Security.check( project, "editRole" );
		}
		try
		{
			Role x = (Role) object;
			render( type, object, x, roles, project );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/show.html", type, object );
		}
	}

	/**
	 * Overrides CRUD.save() that is called by Roles.show() in order to save the
	 * changes done in the role
	 * 
	 * @param id
	 *            the role id
	 */
	public static void save( String id ) throws Exception
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Role role = (Role) object;
		Project p = role.project;
		if( role.project == null )
		{
			Security.check( Security.getConnected().isAdmin );
		}
		else
		{
			Security.check( role.project, "editRoles" );
		}

		validation.valid( object.edit( "object", params ) );
		Security.check( p == role.project );

		List<Role> dups;
		if( role.project == null )
		{
			dups = Role.find( "select r from Role r where r.id != ? and LCASE(r.name) = ? and r.project is null", role.id, role.name.toLowerCase() ).fetch();
		}
		else
		{
			dups = Role.find( "select r from Role r where r.id != ? and LCASE(r.name) = ? and r.project = ?", role.id, role.name.toLowerCase(), role.project ).fetch();
		}
		if( dups.size() > 0 )
		{
			validation.addError( "Name", "That name already exists" );
		}

		if( validation.hasErrors() )
		{
			flash.error( validation.errors().toString() );
			redirect( "/admin/roles/" + id );
		}
		object.save();
		Log.addUserLog("Edited role", role, role.project);
		flash.success( Messages.get( "crud.saved", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			Update.update(role.project, "reload('roles')");
			Application.overlayKiller( "", "" );
		}
		redirect( "/admin/roles/" + role.id );
	}

	/**
	 * Overrides CRUD.blank() that gives the view of creating a new model
	 * instance (a Role in this case).
	 * 
	 * @param id
	 *            which is the project id which the role will be associated
	 *            with.
	 */
	public static void blank( long id )
	{
		Project project = Project.<Project> findById( id );
		List<Role> roles = null;
		if( id != 0 )
			Security.check( project, "canCreateRole" );
		else
		{
			Security.check( Security.getConnected().isAdmin );
			roles = Role.find( "byProjectIsNull" ).fetch();
		}

		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		try
		{
			render( project, type, roles );
		}
		catch( TemplateNotFoundException e )
		{
			render( "CRUD/blank.html", type );
		}
	}

	/**
	 * Overrides CRUD.create() which is called in blank() view to create a new
	 * model instance (a Role).
	 * 
	 * @param id
	 *            the project id which the role will be associated with.
	 * @throws Exception
	 */
	public static void create( long id ) throws Exception
	{
		Project project = Project.<Project> findById( id );
		if( id == 0 )
		{
			Security.check( Security.getConnected().isAdmin );
			params.remove( "object.project@id" );
		}
		else
		{
			Security.check( project, "createRole" );
		}
		ObjectType type = ObjectType.get( getControllerClass() );
		JPASupport object = type.entityClass.newInstance();
		validation.valid( object.edit( "object", params ) );
		Role role = (Role) object;
		List<Role> dups;
		if( role.project == null )
		{
			dups = Role.find( "select r from Role r where LCASE(r.name) = ?1 and r.project is null", role.name.toLowerCase() ).fetch();
		}
		else
		{
			dups = Role.find( "select r from Role r where LCASE(r.name) = ?1 and r.project = ?2", role.name.toLowerCase(), role.project ).fetch();
		}
		if( dups.size() > 0 )
		{
			validation.addError( "Name", "That name already exists" );
		}
		if( validation.hasErrors() )
		{
			params.flash();
			flash.error( validation.errors().toString() );
			redirect( "/admin/roles/new?id=" + id );
		}
		object.save();
		Log.addUserLog("Added new role", role, role.project);
		flash.success( Messages.get( "crud.created", type.modelName, object.getEntityId() ) );
		if( params.get( "_save" ) != null )
		{
			Update.update(role.project, "reload('roles')");
			Application.overlayKiller( "", "" );
		}
		if( params.get( "_saveAndAddAnother" ) != null )
		{
			redirect( "/admin/roles/new?id=" + id );
		}
		redirect( "/admin/roles/" + role.id );
	}

	/**
	 * Overrides CRUD.delete() which deletes a role by flipping the deletion
	 * marker to be true
	 * 
	 * @param id
	 *            the role id.
	 */
	public static void delete( String id )
	{
		ObjectType type = ObjectType.get( getControllerClass() );
		notFoundIfNull( type );
		JPASupport object = type.findById( id );
		Role role = (Role) object;
		Security.check( role.project, "deleteRole" );
		if( !role.baseRole )
		{
			try
			{
				// first remove from projects
				role.project.roles.remove( role );
				role.project.save();

				// then remove from users
				for( User user : role.users )
				{
					user.roles.remove( role );
					user.save();
				}

				Log.addUserLog("Deleted role: " + role.name, role.project);
				role.delete();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		Update.update(role.project, "reload('roles', 'role-"+id+"')");
	}

	public static void list()
	{
		forbidden();
	}

	/**
	 * removes a permisson from a role
	 * 
	 * @param roleId
	 *            the Role id.
	 * @param permId
	 *            the Permission id.
	 */
	public static void removePermission( long roleId, long permId )
	{
		Role role = Role.findById( roleId );
		Security.check( Security.getConnected().in( role.project ).can( "editRole" ) );
		role.permissions.remove( Permission.<Permission> findById( permId ) );
		role.save();
		Log.addUserLog("Edited role", role, role.project);
	}
}
