package net.robocode2.server;

import com.google.gson.Gson;
import lombok.val;
import net.robocode2.Server;
import net.robocode2.engine.ModelUpdater;
import net.robocode2.mappers.*;
import net.robocode2.model.BotIntent;
import net.robocode2.model.GameSetup;
import net.robocode2.model.*;
import net.robocode2.schema.*;
import org.java_websocket.WebSocket;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.round;

public final class GameServer {

  private static Logger logger = LoggerFactory.getLogger(GameServer.class);

  private ConnHandler connHandler;

  private RunningState runningState;
  private GameSetup gameSetup;

  private Set<WebSocket> participants;
  private Set<WebSocket> readyParticipants;

  private final Map<WebSocket, Integer> participantIds = new HashMap<>();

  private final Map<WebSocket, BotIntent> botIntents = new ConcurrentHashMap<>();

  private Timer readyTimer;
  private Timer turnTimer;

  private ModelUpdater modelUpdater;

  private final Gson gson = new Gson();

  public GameServer() {
    val serverSetup = new ServerSetup();
    val connListener = new GameServerConnListener();
    this.connHandler = new ConnHandler(serverSetup, connListener);
    this.runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
  }

  public static void main(String[] args) {
    GameServer server = new GameServer();
    server.start();
  }

  public void start() {
    logger.info("Starting server on port " + Server.getPort());
    connHandler.start();
  }

  private void startGameIfParticipantsReady() {
    if (readyParticipants.size() == participants.size()) {

      readyTimer.stop();
      readyParticipants.clear();
      botIntents.clear();

      startGame();
    }
  }

  private void prepareGame() {
    logger.debug("Preparing game");

    runningState = RunningState.WAIT_FOR_READY_PARTICIPANTS;

    participantIds.clear();

    // Send NewBattle to all participant bots to get them started

    GameStartedEventForBot gameStartedForBot = new GameStartedEventForBot();
    gameStartedForBot.setType(GameStartedEventForBot.Type.GAME_STARTED_EVENT_FOR_BOT);
    gameStartedForBot.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));

    int id = 1;
    for (WebSocket conn : participants) {
      participantIds.put(conn, id);
      gameStartedForBot.setMyId(id++);

      String msg = gson.toJson(gameStartedForBot);
      send(conn, msg);
    }

    readyParticipants = new HashSet<>();

    // Start 'bot-ready' timeout timer
    readyTimer = new Timer(gameSetup.getReadyTimeout(), this::onReadyTimeout);
    readyTimer.start();
  }

  private void startGame() {
    logger.debug("Starting game");

    runningState = RunningState.GAME_RUNNING;

    List<Participant> participantList = new ArrayList<>();
    for (WebSocket conn : participants) {
      BotHandshake h = connHandler.getBotHandshakes().get(conn);
      Participant p = new Participant();
      p.setId(participantIds.get(conn));
      p.setAuthor(h.getAuthor());
      p.setCountryCode(h.getCountryCode());
      p.setGameTypes(h.getGameTypes());
      p.setName(h.getName());
      p.setProgrammingLang(h.getProgrammingLang());
      p.setVersion(h.getVersion());
      participantList.add(p);
    }

    // Send GameStarted to all participant observers to get them started
    if (connHandler.getObserverAndControllerConnections().size() > 0) {
      GameStartedEventForObserver gameStartedForObserver = new GameStartedEventForObserver();
      gameStartedForObserver.setType(
          GameStartedEventForObserver.Type.GAME_STARTED_EVENT_FOR_OBSERVER);
      gameStartedForObserver.setGameSetup(GameSetupToGameSetupMapper.map(gameSetup));
      gameStartedForObserver.setParticipants(participantList);
      sendMessageToObservers(gson.toJson(gameStartedForObserver));
    }

    // Prepare model update

    modelUpdater = ModelUpdater.create(gameSetup, new HashSet<>(participantIds.values()));

    // Restart turn timeout timer
    turnTimer = new Timer(gameSetup.getTurnTimeout(), this::onTurnTimeout);
    turnTimer.start();
  }

  private void startGame(
      net.robocode2.schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
    this.gameSetup = GameSetupToGameSetupMapper.map(gameSetup);
    participants = connHandler.getBotConnections(botAddresses);
    if (participants.size() > 0) {
      prepareGame();
    }
  }

  private void abortGame() {
    logger.info("Aborting game");

    runningState = RunningState.GAME_STOPPED;

    GameAbortedEventForObserver abortedEvent = new GameAbortedEventForObserver();
    abortedEvent.setType(GameAbortedEventForObserver.Type.GAME_ABORTED_EVENT_FOR_OBSERVER);
    sendMessageToObservers(gson.toJson(abortedEvent));

    // No score is generated for aborted games
  }

  private List<BotResultsForBot> getResultsForBots() {
    List<BotResultsForBot> botResultsList = new ArrayList<>();

    this.modelUpdater
        .getResults()
        .forEach(
            score -> {
              BotResultsForBot botResults = new BotResultsForBot();
              botResultsList.add(botResults);

              botResults.setId(score.getBotId());
              botResults.setSurvival((int) round(score.getSurvival()));
              botResults.setLastSurvivorBonus((int) round(score.getLastSurvivorBonus()));
              botResults.setBulletDamage((int) round(score.getBulletDamage()));
              botResults.setBulletKillBonus((int) round(score.getBulletKillBonus()));
              botResults.setRamDamage((int) round(score.getRamDamage()));
              botResults.setRamKillBonus((int) round(score.getRamKillBonus()));
              botResults.setTotalScore((int) round(score.getTotalScore()));
              botResults.setFirstPlaces(score.getFirstPlaces());
              botResults.setSecondPlaces(score.getSecondPlaces());
              botResults.setThirdPlaces(score.getThirdPlaces());
            });

    int rank = 1;
    for (BotResultsForBot botResult : botResultsList) {
      botResult.setRank(rank++);
    }

    return botResultsList;
  }

  private List<BotResultsForObserver> getResultsForObservers() {
    List<BotResultsForObserver> botResultsList = new ArrayList<>();

    this.modelUpdater
        .getResults()
        .forEach(
            score -> {
              BotResultsForObserver botResults = new BotResultsForObserver();
              botResultsList.add(botResults);

              botResults.setId(score.getBotId());
              botResults.setSurvival((int) round(score.getSurvival()));
              botResults.setLastSurvivorBonus((int) round(score.getLastSurvivorBonus()));
              botResults.setBulletDamage((int) round(score.getBulletDamage()));
              botResults.setBulletKillBonus((int) round(score.getBulletKillBonus()));
              botResults.setRamDamage((int) round(score.getRamDamage()));
              botResults.setRamKillBonus((int) round(score.getRamKillBonus()));
              botResults.setTotalScore((int) round(score.getTotalScore()));
              botResults.setFirstPlaces(score.getFirstPlaces());
              botResults.setSecondPlaces(score.getSecondPlaces());
              botResults.setThirdPlaces(score.getThirdPlaces());

              WebSocket conn = null;
              for (Entry<WebSocket, Integer> entry : participantIds.entrySet()) {
                if (entry.getValue().equals(score.getBotId())) {
                  conn = entry.getKey();
                  break;
                }
              }

              BotHandshake botHandshake = connHandler.getBotHandshakes().get(conn);
              botResults.setName(botHandshake.getName());
              botResults.setVersion(botHandshake.getVersion());
            });

    int rank = 1;
    for (BotResultsForObserver botResult : botResultsList) {
      botResult.setRank(rank++);
    }

    return botResultsList;
  }

  private void pauseGame() {
    logger.info("Pausing game");

    GamePausedEventForObserver pausedEvent = new GamePausedEventForObserver();
    pausedEvent.setType(GamePausedEventForObserver.Type.GAME_PAUSED_EVENT_FOR_OBSERVER);
    sendMessageToObservers(gson.toJson(pausedEvent));

    runningState = RunningState.GAME_PAUSED;
  }

  private void resumeGame() {
    logger.info("Resuming game");

    GameResumedEventForObserver resumedEvent = new GameResumedEventForObserver();
    resumedEvent.setType(GameResumedEventForObserver.Type.GAME_RESUMED_EVENT_FOR_OBSERVER);
    sendMessageToObservers(gson.toJson(resumedEvent));

    if (runningState == RunningState.GAME_PAUSED) {
      runningState = RunningState.GAME_RUNNING;
    }
  }

  private GameState updateGameState() {
    Map<Integer /* BotId */, BotIntent> mappedBotIntents = new HashMap<>();

    for (Entry<WebSocket, BotIntent> entry : botIntents.entrySet()) {
      int botId = participantIds.get(entry.getKey());
      mappedBotIntents.put(botId, entry.getValue());
    }

    return modelUpdater.update(Collections.unmodifiableMap(mappedBotIntents));
  }

  private void onReadyTimeout() {
    logger.debug("Ready timeout");

    if (readyParticipants.size() >= gameSetup.getMinNumberOfParticipants()) {
      // Start the game with the participants that are ready
      participants = readyParticipants;
      startGame();

    } else {
      // Not enough participants -> prepare another game
      runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN;
    }
  }

  private void onTurnTimeout() {
    turnTimer.stop();

    logger.debug("Turn timeout");

    // Send SkippedTurnEvents to all bots that skipped a turn, i.e. where the server did not receive a bot intent
    // before the turn ended.
    participantIds.forEach((conn, id) -> {
      if (botIntents.get(conn) == null) {
        SkippedTurnEvent skippedTurnEvent = new SkippedTurnEvent();
        skippedTurnEvent.setType(Message.Type.SKIPPED_TURN_EVENT);
        skippedTurnEvent.setTurnNumber(modelUpdater.getTurnNumber());

        send(conn, gson.toJson(skippedTurnEvent));
      }
    });

    nextTurnTick();
  }

  synchronized
  private void nextTurnTick() {
    // Stop turn timeout timer
    turnTimer.stop();

    if (runningState == RunningState.GAME_PAUSED) {
      return;
    }

    logger.debug("Updating game state");

    if (runningState != RunningState.GAME_STOPPED) {
      // Update game state
      GameState gameState = updateGameState();

      if (gameState.isGameEnded()) {
        runningState = RunningState.GAME_STOPPED;

        logger.info("Game ended");

        modelUpdater.calculatePlacements();

        // End game for bots
        GameEndedEventForBot endEventForBot = new GameEndedEventForBot();
        endEventForBot.setType(GameEndedEventForObserver.Type.GAME_ENDED_EVENT_FOR_BOT);
        endEventForBot.setNumberOfRounds(modelUpdater.getNumberOfRounds());
        endEventForBot.setResults(getResultsForBots());
        sendMessageToBots(gson.toJson(endEventForBot));

        // End game for observers
        GameEndedEventForObserver endEventForObserver = new GameEndedEventForObserver();
        endEventForObserver.setType(GameEndedEventForObserver.Type.GAME_ENDED_EVENT_FOR_OBSERVER);
        endEventForObserver.setNumberOfRounds(modelUpdater.getNumberOfRounds());
        endEventForObserver.setResults(getResultsForObservers()); // Use the stored score!
        sendMessageToObservers(gson.toJson(endEventForObserver));

        // runningState = RunningState.WAIT_FOR_PARTICIPANTS_TO_JOIN; // TODO: Correct?
      } else {
        // Clear bot intents
        botIntents.clear();

        // Send tick

        Round round = gameState.getLastRound();
        if (round != null) {
          Turn turn = round.getLastTurn();
          if (turn != null) {
            // Send game state as 'game tick' to participants
            for (WebSocket conn : participants) {
              TickEventForBot gameTickForBot =
                  TurnToGameTickForBotMapper.map(round, turn, participantIds.get(conn));
              if (gameTickForBot != null) { // Bot alive?
                String msg = gson.toJson(gameTickForBot);
                send(conn, msg);
              }
            }
            TickEventForObserver gameTickForObserver =
                TurnToGameTickForObserverMapper.map(round, turn);
            sendMessageToObservers(gson.toJson(gameTickForObserver));
          }
        }
        // Restart turn timeout timer
        turnTimer.start();
      }
    }
  }

  private static void send(WebSocket conn, String message) {
    logger.debug("Sending to: " + conn.getRemoteSocketAddress() + ", message: " + message);

    try {
      conn.send(message);
    } catch (WebsocketNotConnectedException ignore) {
      // Bot cannot receive events and send new intents.
    }
  }

  private void updateBotIntent(WebSocket conn, net.robocode2.schema.BotIntent intent) {
    if (!participants.contains(conn)) {
      return;
    }
    BotIntent botIntent = botIntents.get(conn);
    if (botIntent == null) {
      botIntent = BotIntent.builder().build();
    }
    botIntent = botIntent.update(BotIntentToBotIntentMapper.map(intent));
    botIntents.put(conn, botIntent);

    // Prepare next turn if all participant bots delivered their intents
    if (botIntents.size() == participants.size()) {
//      nextTurnTick();     // FIXME: Temporarily uncommented
    }
  }

  private String createBotListUpdateMessage() {
    BotListUpdate botListUpdate = new BotListUpdate();
    botListUpdate.setType(Message.Type.BOT_LIST_UPDATE);
    List<BotInfo> bots = new ArrayList<>();
    botListUpdate.setBots(bots);

    Set<WebSocket> botConnections = connHandler.getBotConnections();
    for (WebSocket conn : botConnections) {
      InetSocketAddress address = conn.getRemoteSocketAddress();
      BotInfo botInfo =
          BotHandshakeToBotInfoMapper.map(
              connHandler.getBotHandshakes().get(conn), address.getHostString(), address.getPort());
      bots.add(botInfo);
    }
    return gson.toJson(botListUpdate);
  }

  private void sendMessageToBots(String msg) {
    connHandler.getBotConnections().forEach(conn -> send(conn, msg));
  }

  private void sendMessageToObservers(String msg) {
    connHandler.getObserverAndControllerConnections().forEach(conn -> send(conn, msg));
  }

  private void sendBotListUpdateToObservers() {
    sendMessageToObservers(createBotListUpdateMessage());
  }

  private class GameServerConnListener implements ConnListener {

    @Override
    public void onException(Exception exception) {
      exception.printStackTrace();
    }

    @Override
    public void onBotJoined(WebSocket conn, BotHandshake bot) {
      sendBotListUpdateToObservers();
    }

    @Override
    public void onBotLeft(WebSocket conn) {
      // If a bot leaves while in a game, make sure to reset all intent values to zeroes
      BotIntent intent = botIntents.get(conn);
      if (intent != null) {
        botIntents.put(conn, BotIntent.builder().build().zerofied());
      }

      sendBotListUpdateToObservers();
    }

    @Override
    public void onObserverJoined(WebSocket conn, ObserverHandshake bot) {
      String msg = createBotListUpdateMessage();
      send(conn, msg);
    }

    @Override
    public void onObserverLeft(WebSocket conn) {
      // Do nothing
    }

    @Override
    public void onControllerJoined(WebSocket conn, ControllerHandshake bot) {
      String msg = createBotListUpdateMessage();
      send(conn, msg);
    }

    @Override
    public void onControllerLeft(WebSocket conn) {
      // Do nothing
    }

    @Override
    public void onBotReady(WebSocket conn) {
      if (runningState == RunningState.WAIT_FOR_READY_PARTICIPANTS) {
        readyParticipants.add(conn);
        startGameIfParticipantsReady();
      }
    }

    @Override
    public void onBotIntent(WebSocket conn, net.robocode2.schema.BotIntent intent) {
      updateBotIntent(conn, intent);
    }

    @Override
    public void onStartGame(
        net.robocode2.schema.GameSetup gameSetup, Collection<BotAddress> botAddresses) {
      startGame(gameSetup, botAddresses);
    }

    @Override
    public void onAbortGame() {
      abortGame();
    }

    @Override
    public void onPauseGame() {
      pauseGame();
    }

    @Override
    public void onResumeGame() {
      resumeGame();
    }
  }
}
