package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Component;
import models.Game;
import models.GameSession;
import models.Round;
import models.Story;
import models.Trick;
import models.User;
import others.RoundTricks;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Games controller that handles game requests
 * 
 * @author mahmoudsakr
 */
@With (Secure.class)
public class Games extends Controller {

	/**
	 * Shows a list of stories assigned to this component to choose from (to
	 * assign to new game)
	 * 
	 * @param cId
	 *            component id
	 */
	@Check ("canStartGame")
	public static void chooseStories(long cId) {
		Component c = Component.findById(cId);
		render(c);
	}

	/**
	 * Starts a new game with the assigned story ids
	 * 
	 * @param stories
	 *            array of story ids
	 */
	public static void startGame(long[] stories) {
		Game game = new Game().save();
		game.init();
		Component component = null;
		User user = Security.getConnected();
		for (long s : stories) {
			Story story = Story.findById(s);
			// story.games.add(game);
			// story.save();
			//			
			if (!user.getPermission(story.componentID.project).canStartGame && !user.isAdmin) {
				forbidden();
			}
			game.stories.add(story);
			component = story.componentID;
		}
		if (component == null) {
			notFound();
		}
		game.component = component;
		game.save();

		// for (Story s : game.stories) {
		// System.out.println(s);
		// System.out.println(s.games);
		// }
		GameSession session = new GameSession().save();
		session.started = new Date().getTime();
		session.user = Security.getConnected();
		session.game = game;
		session.lastClick = new Date().getTime();
		session.save();

		playGame(game.id);
	}

	/**
	 * renders this game (generates current round from information currently
	 * available)
	 * 
	 * @param gameId
	 *            game id
	 */
	@Check ("inGame")
	public static void playGame(long gameId) {
		Game game = Game.findById(gameId);
		List<GameSession> sessions = GameSession.find("game = ?1 and lastClick >= ?2", game, new Date().getTime() - 60000).fetch();
		ArrayList<User> players = new ArrayList<User>();
		GameSession mod = null;
		for (GameSession gs : sessions) {
			if (gs.lastClick + 60000 >= new Date().getTime()) {
				if (mod == null || mod.started > gs.started) {
					mod = gs;
				}
				players.add(gs.user);
			}
		}

		if (game.currentStory == null) {
			game.currentStory = game.stories.get(0);
			game.save();

		}

		Round round = game.getRound();
		if (round == null) {
			round = new Round(Round.find("byGameAndStory", game, game.currentStory).fetch().size()).save();
			round.game = game;
			round.story = game.currentStory;
			round.save();
		}
		List<Round> allRounds = Round.find("game = ?1  order by id desc", game).fetch();

		/******* Start of History of the game *********/
		ArrayList<RoundTricks> rounds = new ArrayList<RoundTricks>();
		for (Round r : allRounds) {
			List<Trick> TricksInRound = Trick.find("byRound", r).fetch();
			rounds.add(new RoundTricks(r, TricksInRound));
		}

		render(game, sessions, round, rounds);
	}

	/**
	 * receives an estimate request from the client
	 * 
	 * @param roundId
	 *            round id
	 * @param estimate
	 *            estimate for this round/story
	 */
	@Check ("canEstimate")
	public static void estimate(long roundId, double estimate) {
		Round round = Round.findById(roundId);
		if (Trick.find("byRoundAndUser", round, Security.getConnected()).first() != null) {
			renderText("You've already played a trick in this round.");
		}
		Trick trick = new Trick().save();
		trick.round = round;
		trick.user = Security.getConnected();
		trick.estimate = estimate;
		trick.save();

		renderText("Trick cast. Wait until others vote!");
	}

	/**
	 * informs server that user is still connected
	 * 
	 * @param gameId
	 *            game id
	 * @param roundId
	 *            round id
	 */
	public static void connect(long gameId, long roundId) {
		// System.out.println("-------- START");
		// System.out.println("Galaal");
		Game game = Game.findById(gameId);
		User user = Security.getConnected();
		Round current = Round.findById(roundId);
		// System.out.println(round);
		GameSession session = GameSession.find("byUserAndGame", user, game).first();
		// System.out.println(rounds.size() + " " + rounds.get(0));
		if (session == null) {
			// System.out.println("Galaaaaal");
			session = new GameSession().save();
			session.started = new Date().getTime();
			session.user = user;
			session.game = game;
		}
		session.lastClick = new Date().getTime();
		session.save();

		// if (round.isDone()) {
		// System.out.println("here");
		// Round newround = new Round().save();
		// newround.game = game;
		// newround.story = game.currentStory;
		// newround.save();
		//
		// System.out.println(newround);
		// }
		// System.out.println(game.getRound());
		String response = "0";
		String roundInfo = "";
		if (game.getRound() != null && game.getRound().isDone()) {
			response = "1";
			roundInfo = game.getRound().info();
		}
		// if (response.equals("0"))
		// suspend("2s");

		int isModerator = 0;
		if (response.equals("1")) {
			if (game.getModerator() == user)
				isModerator = 1;
		}

		int rightRound = 0;
		if (current != game.getRound()) {
			rightRound = 1;
		}
		// remember to keep the order of the strings responded in order
		// 3ashan matbawazosh sho3`l ba3d
		// isModerator is at index 1
		if (roundInfo.length() < 1) {
			roundInfo = "null";
		}
		response += "," + isModerator + "," + rightRound + "," + game.getRound().id + "," + roundInfo + ",";

		Round round = game.getRound();
		List<Trick> tricks = Trick.find("byRound", round).fetch();
		// String response = "";
		boolean roundDone = round.isDone();
		for (int i = 0; i < tricks.size(); i++) {
			response += tricks.get(i).user.name + " " + (roundDone ? tricks.get(i).estimate + "" : "done") + "|";
			// if (tricks.size() - 1 != i) {
			// } else
			// response += tricks.get(i).estimate;
		}
		response = response.substring(0, response.length() - 1);

		renderText(response);
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
	public static void nextRound(long gameId, double estimate, boolean playAgain) {
		Game game = Game.findById(gameId);
		if (!playAgain) {
			Story story = game.getRound().story;
			story.estimate = estimate;
			story.save();
			boolean gameOver = false;
			if (game.stories.indexOf(game.currentStory) == game.stories.size() - 1) {
				gameOver = true;
			}

			game.save();
			if (gameOver)
				gameOver(game.id);
			game.currentStory = game.stories.get(game.stories.indexOf(game.currentStory) + 1);
			game.save();
		}
		Round round = new Round(Round.find("byGameAndStory", game, game.currentStory).fetch().size()).save();
		round.game = game;
		round.story = game.currentStory;
		round.save();
		playGame(gameId);
	}

	/**
	 * generates summary of game
	 * 
	 * @author monayri
	 * @param gameId
	 *            game id
	 */
	public static void gameOver(long gameId) {
		Game game = Game.findById(gameId);
		Component component = game.component;
		List<Story> stories = game.stories;
		List<Round> allRounds = Round.find("game = ?1  order by id ", game).fetch();

		ArrayList<RoundTricks> rounds = new ArrayList<RoundTricks>();
		for (Round r : allRounds) {
			List<Trick> TricksInRound = Trick.find("byRound", r).fetch();
			rounds.add(new RoundTricks(r, TricksInRound));
		}
		render(stories, rounds, component);
	}

	/**
	 * choose game from components - shows a list of available games to choose
	 * from
	 * 
	 * @param cId
	 *            component id
	 */
	public static void chooseGame(long cId) {
		Component component = Component.findById(cId);
		List<Game> games = Game.find("byComponent", component).fetch();
		List<Game> finalGames = new ArrayList<Game>();
		List<User> moderators = new ArrayList<User>();
		for (Game g : games) {
			if (!g.getRound().isDone()) {
				finalGames.add(g);
				moderators.add(g.getModerator());
			}
		}
		render(finalGames, moderators, component);
	}
}
