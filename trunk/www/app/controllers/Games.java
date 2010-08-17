package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Component;
import models.Game;
import models.GameSession;
import models.Project;
import models.Round;
import models.Task;
import models.Trick;
import models.User;
import others.RoundTricks;
import play.mvc.With;

/**
 * Games controller that handles game requests
 * 
 * @author mahmoudsakr
 */
@With( Secure.class )
public class Games extends SmartController
{
	/**
	 * Shows a list of stories assigned to this component to choose from (to
	 * assign to new game)
	 * 
	 * @param cId
	 *            component id
	 */
	public static void chooseStories( long cId )
	{
		Component c = Component.findById( cId );
		if( c.deleted )
			notFound();
		Security.check( Security.getConnected().in( c.project ).can( "startGame" ) );
		render( c );
	}

	/**
	 * Starts a new game with the assigned story ids
	 * 
	 * @param stories
	 *            array of story ids
	 */
	public static void startGame( long[] tasks )
	{
		Game game = new Game().save();

		// game.init(); moved down by Amr Hany in order to have project id in
		// the chatroom
		Component component = null;
		User user = Security.getConnected();
		for( long s : tasks )
		{
			Task task = Task.findById( s );
			if( !user.in( task.project ).can( "startGame" ) )
			{
				forbidden();
			}
			game.tasks.add( task );
			component = task.component;
		}
		if( component == null )
		{
			notFound();
		}
		game.component = component;
		game.save();
		game.init();

		GameSession session = new GameSession().save();
		session.started = new Date().getTime();
		session.user = Security.getConnected();
		session.game = game;
		session.lastClick = new Date().getTime();
		session.save();
		playGame( game.id );
	}

	/**
	 * renders this game (generates current round from information currently
	 * available)
	 * 
	 * @param gameId
	 *            game id
	 */
	public static void playGame( long gameId )
	{
		Game game = Game.findById( gameId );
		Security.check( game.component.componentUsers.contains( Security.getConnected() ) );
		List<GameSession> sessions = GameSession.find( "game = ?1 and lastClick >= ?2", game, new Date().getTime() - 60000 ).fetch();
		ArrayList<User> players = new ArrayList<User>();
		GameSession mod = null;
		for( GameSession gs : sessions )
		{
			if( gs.lastClick + 60000 >= new Date().getTime() )
			{
				if( mod == null || mod.started > gs.started )
				{
					mod = gs;
				}
				players.add( gs.user );
			}
		}

		if( game.currentTask == null )
		{
			game.currentTask = game.tasks.get( 0 );
			game.save();

		}

		Round round = game.getRound();
		if( round == null )
		{
			round = new Round( Round.find( "byGameAndTask", game, game.currentTask ).fetch().size() ).save();
			round.game = game;
			round.task = game.currentTask;
			round.save();
		}
		List<Round> allRounds = Round.find( "game = ?1  order by id desc", game ).fetch();

		/******* Start of History of the game *********/
		ArrayList<RoundTricks> rounds = new ArrayList<RoundTricks>();
		for( Round r : allRounds )
		{
			List<Trick> TricksInRound = Trick.find( "byRound", r ).fetch();
			rounds.add( new RoundTricks( r, TricksInRound ) );
		}

		render( game, sessions, round, rounds );
	}

	/**
	 * receives an estimate request from the client
	 * 
	 * @param roundId
	 *            round id
	 * @param estimate
	 *            estimate for this round/story
	 */
	public static void estimate( long roundId, double estimate )
	{
		Round round = Round.findById( roundId );
		Security.check( round.game.component.componentUsers.contains( Security.getConnected() ) );
		if( Trick.find( "byRoundAndUser", round, Security.getConnected() ).first() != null )
		{
			renderText( "You've already played a trick in this round." );
		}
		Trick trick = new Trick().save();
		trick.round = round;
		trick.user = Security.getConnected();
		trick.estimate = estimate;
		trick.save();

		renderText( "Trick cast. Wait until others vote!" );
	}

	/**
	 * informs server that user is still connected
	 * 
	 * @param gameId
	 *            game id
	 * @param roundId
	 *            round id
	 */
	public static void connect( long gameId, long roundId )
	{
		Game game = Game.findById( gameId );
		User user = Security.getConnected();
		Security.check( game.component.componentUsers.contains( user ) );
		Round current = Round.findById( roundId );
		GameSession session = GameSession.find( "byUserAndGame", user, game ).first();
		if( session == null )
		{
			session = new GameSession().save();
			session.started = new Date().getTime();
			session.user = user;
			session.game = game;
		}
		session.lastClick = new Date().getTime();
		session.save();

		String response = "0";
		String roundInfo = "null";
		if( game.getRound() != null && game.getRound().isDone() )
		{
			response = "1";
			roundInfo = game.getRound().info();
		}

		int isModerator = response.equals( "1" ) && game.getModerator() == user ? 1 : 0;

		int rightRound = current == game.getRound() ? 0 : 1;

		response += "," + isModerator + "," + rightRound + "," + game.getRound().id + "," + roundInfo + ",";
		Round round = game.getRound();
		List<Trick> tricks = Trick.find( "byRound", round ).fetch();
		boolean roundDone = round.isDone();
		for( int i = 0; i < tricks.size(); i++ )
		{
			response += tricks.get( i ).user.name + " " + (roundDone ? tricks.get( i ).estimate + "" : "done") + "|";
		}
		response = response.substring( 0, response.length() - 1 );

		renderText( response );
	}

	/**
	 * Generates next round for the game, with the estimate. Uses playAgain to
	 * figure whether or not to play the same story again or not.
	 * 
	 * @author galalaly
	 * @param gameId
	 *            game id
	 * @param estimate
	 *            estimate
	 * @param playAgain
	 *            play again
	 */
	public static void nextRound( long gameId, double estimate, boolean playAgain )
	{
		Game game = Game.findById( gameId );
		Security.check( game.component.componentUsers.contains( Security.getConnected() ) );

		if( !playAgain )
		{
			Task task = game.getRound().task;
			task.estimationPoints = estimate;
			task.save();
			boolean gameOver = false;
			if( game.tasks.indexOf( game.currentTask ) == game.tasks.size() - 1 )
			{
				gameOver = true;
			}

			game.save();
			if( gameOver )
				gameOver( game.id );
			game.currentTask = game.tasks.get( game.tasks.indexOf( game.currentTask ) + 1 );
			game.save();
		}
		Round round = new Round( Round.find( "byGameAndTask", game, game.currentTask ).fetch().size() ).save();
		round.game = game;
		round.task = game.currentTask;
		round.save();
		playGame( gameId );
	}

	/**
	 * generates summary of game
	 * 
	 * @author monayri
	 * @param gameId
	 *            game id
	 */
	public static void gameOver( long gameId )
	{
		Game game = Game.findById( gameId );
		Component component = game.component;
		Security.check( component.componentUsers.contains( Security.getConnected() ) );
		List<Task> stories = game.tasks;
		List<Round> allRounds = Round.find( "game = ?1  order by id ", game ).fetch();

		ArrayList<RoundTricks> rounds = new ArrayList<RoundTricks>();
		for( Round r : allRounds )
		{
			List<Trick> TricksInRound = Trick.find( "byRound", r ).fetch();
			rounds.add( new RoundTricks( r, TricksInRound ) );
		}
		render( stories, rounds, component );
	}

	/**
	 * choose game from components - shows a list of available games to choose
	 * from
	 * 
	 * @param cId
	 *            component id
	 */
	public static void chooseGame( long cId )
	{
		Component component = Component.findById( cId );
		Security.check( component.componentUsers.contains( Security.getConnected() ) );
		List<Game> games = Game.find( "byComponent", component ).fetch();
		List<Game> finalGames = new ArrayList<Game>();
		List<User> moderators = new ArrayList<User>();
		for( Game g : games )
		{
			if( !g.getRound().isDone() )
			{
				finalGames.add( g );
				moderators.add( g.getModerator() );
			}
		}
		render( finalGames, moderators, component );
	}

	public static void userGameComponents( long projectId )
	{
		Project project = Project.findById( projectId );
		List<Component> userComponents = Security.getConnected().components;
		List<Component> components = new ArrayList<Component>();
		for( Component c : userComponents )
		{
			if( !c.deleted )
			{
				if( c.project.id == projectId )
				{
					components.add( c );
				}
			}
		}
		render( components, projectId );
	}
}
