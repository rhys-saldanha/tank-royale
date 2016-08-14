package net.robocode2.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocket;

import com.google.gson.Gson;

import net.robocode2.game.ModelUpdater;
import net.robocode2.json_schema.BotHandshake;
import net.robocode2.json_schema.GameDefinition;
import net.robocode2.json_schema.NewBattleForBot;
import net.robocode2.json_schema.NewBattleForObserver;
import net.robocode2.json_schema.ObserverHandshake;
import net.robocode2.json_schema.Participant;
import net.robocode2.model.GameState;
import net.robocode2.model.Setup;

public final class GameServer {

	ServerSetup setup;
	ConnectionListener connectionObserver;
	ConnectionHandler connectionHandler;

	ServerState gameState;
	GameDefinition gameDefinition;
	Set<Bot> participants;
	Set<Bot> readyParticipants;

	final Timer readyTimer = new Timer();

	final Gson gson = new Gson();

	ModelUpdater modelUpdater;

	public GameServer() {
		this.setup = new ServerSetup();
		this.connectionObserver = new ConnectionObserver();
		this.connectionHandler = new ConnectionHandler(setup, connectionObserver);
		this.gameState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
	}

	public void start() {
		connectionHandler.start();
	}

	public static void main(String[] args) {
		GameServer server = new GameServer();
		server.start();
	}

	private void prepareGameIfEnoughCandidates() {
		GameAndParticipants gameAndParticipants = selectGameAndParticipants(connectionHandler.getBotConnections());
		if (gameAndParticipants == null) {
			return;
		}

		gameDefinition = gameAndParticipants.gameDefinition;
		participants = gameAndParticipants.participants;

		if (participants.size() > 0) {
			prepareGame();
		}
	}

	private void startGameIfParticipantsReady() {
		if (readyParticipants.size() == participants.size()) {

			readyTimer.cancel();
			readyParticipants.clear();

			startGame();
		}
	}

	private void prepareGame() {
		System.out.println("#### PREPARE GAME #####");

		gameState = ServerState.WAIT_FOR_READY_PARTICIPANTS;

		// Send NewBattle to all participant bots to get them started

		NewBattleForBot ngb = new NewBattleForBot();
		ngb.setMessageType(NewBattleForBot.MessageType.NEW_BATTLE_FOR_BOT);
		ngb.setGameType(gameDefinition.getGameType());
		ngb.setArenaWidth(gameDefinition.getArenaWidth());
		ngb.setArenaHeight(gameDefinition.getArenaHeight());
		ngb.setNumberOfRounds(gameDefinition.getNumberOfRounds());
		ngb.setMinNumberOfParticipants(gameDefinition.getMinNumberOfParticipants());
		ngb.setMaxNumberOfParticipants(gameDefinition.getMaxNumberOfParticipants());
		ngb.setTurnTimeout(gameDefinition.getTurnTimeout());
		ngb.setReadyTimeout(gameDefinition.getReadyTimeout());

		int participantId = 1;
		for (Bot participant : participants) {
			participant.setId(participantId);
			ngb.setMyId(participantId);

			String msg = gson.toJson(ngb);
			send(participant.getConnection(), msg);

			participantId++;
		}

		readyParticipants = new HashSet<Bot>();

		// Start 'ready' timer

		readyTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				onReadyTimeout();
			}

		}, gameDefinition.getReadyTimeout());
	}

	// Should be moved to a "strategy" class
	private GameAndParticipants selectGameAndParticipants(Map<WebSocket, BotHandshake> candidateBots) {

		Map<String, Set<Bot>> candidateBotsPerGameType = new HashMap<>();

		Set<String> gameTypes = getGameTypes();

		// Generate a map over potential participants per game type
		Iterator<Entry<WebSocket, BotHandshake>> iterator = candidateBots.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<WebSocket, BotHandshake> entry = iterator.next();
			BotHandshake botHandshake = entry.getValue();
			List<String> availGameTypes = new ArrayList<>(botHandshake.getGameTypes());
			availGameTypes.retainAll(gameTypes);

			for (String gameType : availGameTypes) {
				Set<Bot> candidates = candidateBotsPerGameType.get(gameType);
				if (candidates == null) {
					candidates = new HashSet<>();
					candidateBotsPerGameType.put(gameType, candidates);
				}
				candidates.add(new Bot(entry.getKey(), entry.getValue()));
			}
		}

		// Run through the list of games and see if anyone has enough
		// participants to start the game
		Set<GameDefinition> games = setup.getGames();
		for (Entry<String, Set<Bot>> entry : candidateBotsPerGameType.entrySet()) {
			GameDefinition game = games.stream().filter(g -> g.getGameType().equalsIgnoreCase(entry.getKey())).findAny()
					.orElse(null);

			Set<Bot> participants = entry.getValue();
			int count = participants.size();
			if (count >= game.getMinNumberOfParticipants() && count <= game.getMaxNumberOfParticipants()) {
				// enough participants
				GameAndParticipants gameAndParticipants = new GameAndParticipants();
				gameAndParticipants.gameDefinition = game;
				gameAndParticipants.participants = participants;
				return gameAndParticipants;
			}
		}

		return null; // not enough participants
	}

	private void startGame() {
		System.out.println("#### START GAME #####");

		gameState = ServerState.GAME_RUNNING;

		Set<Integer> participantIds = new HashSet<>();
		for (Bot bot : participants) {
			participantIds.add(bot.getId());
		}

		// Send NewBattle to all participant observers to get them started

		NewBattleForObserver ngo = new NewBattleForObserver();
		ngo.setMessageType(NewBattleForObserver.MessageType.NEW_BATTLE_FOR_OBSERVER);
		ngo.setGameType(gameDefinition.getGameType());
		ngo.setArenaWidth(gameDefinition.getArenaWidth());
		ngo.setArenaHeight(gameDefinition.getArenaHeight());
		ngo.setNumberOfRounds(gameDefinition.getNumberOfRounds());
		ngo.setMinNumberOfParticipants(gameDefinition.getMinNumberOfParticipants());
		ngo.setMaxNumberOfParticipants(gameDefinition.getMaxNumberOfParticipants());
		ngo.setTurnTimeout(gameDefinition.getTurnTimeout());
		ngo.setReadyTimeout(gameDefinition.getReadyTimeout());

		List<Participant> list = new ArrayList<>();

		for (Bot bot : participants) {
			Participant p = new Participant();
			BotHandshake h = bot.getHandshake();
			p.setAuthor(h.getAuthor());
			p.setCountryCode(h.getCountryCode());
			p.setGameTypes(h.getGameTypes());
			p.setId(bot.getId());
			p.setName(h.getName());
			p.setProgrammingLanguage(h.getProgrammingLanguage());
			p.setVersion(h.getVersion());
			list.add(p);
		}
		ngo.setParticipants(list);

		String msg = gson.toJson(ngo);

		for (Entry<WebSocket, ObserverHandshake> entry : connectionHandler.getObserverConnections().entrySet()) {
			WebSocket observer = entry.getKey();
			send(observer, msg);
		}

		Setup setup = new Setup(gameDefinition.getGameType(), gameDefinition.getArenaWidth(), gameDefinition.getArenaHeight(),
				gameDefinition.getNumberOfRounds(), gameDefinition.getTurnTimeout(), gameDefinition.getReadyTimeout(), participantIds);

		modelUpdater = new ModelUpdater(setup);

		updateGameState();
	}

	private void updateGameState() {
		GameState gameState = modelUpdater.update();

		// TODO: Send game state as 'tick' to participants

		// TODO: Game state must be delayed for observers
	}

	private Set<String> getGameTypes() {
		Set<String> gameTypes = new HashSet<>();
		for (GameDefinition gameDef : setup.getGames()) {
			gameTypes.add(gameDef.getGameType());
		}
		return gameTypes;
	}

	private void onReadyTimeout() {
		System.out.println("#### READY TIMEOUT #####");

		if (readyParticipants.size() >= gameDefinition.getMinNumberOfParticipants()) {
			// Start the game with the participants that are ready
			participants = readyParticipants;
			startGame();

		} else {
			// Not enough participants -> prepare another game
			gameState = ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
			prepareGameIfEnoughCandidates();
		}
	}

	private static void send(WebSocket conn, String message) {
		System.out.println("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

		conn.send(message);
	}

	private class ConnectionObserver implements ConnectionListener {

		@Override
		public void onException(Exception exception) {
			exception.printStackTrace();
		}

		@Override
		public void onBotJoined(Bot bot) {
			if (gameState == ServerState.WAIT_FOR_PARTICIPANTS_TO_JOIN) {
				prepareGameIfEnoughCandidates();
			}
		}

		@Override
		public void onBotLeft(Bot bot) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverJoined(Observer observer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onObserverLeft(Observer observer) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onBotReady(Bot bot) {
			if (gameState == ServerState.WAIT_FOR_READY_PARTICIPANTS) {
				readyParticipants.add(bot);
				startGameIfParticipantsReady();
			}
		}
	}

	private class GameAndParticipants {
		GameDefinition gameDefinition;
		Set<Bot> participants;
	}
}
