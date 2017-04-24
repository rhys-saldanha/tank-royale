package net.robocode2.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

import net.robocode2.json_schema.controller.commands.Command;
import net.robocode2.json_schema.controller.commands.ListBots;
import net.robocode2.json_schema.messages.ControllerHandshake;

public class ControllerClient1 extends WebSocketClient {

	final Gson gson = new Gson();

	static final String TYPE = "type";

	public ControllerClient1(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public ControllerClient1(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("onOpen()");

		ControllerHandshake handshake = new ControllerHandshake();
		handshake.setType(ControllerHandshake.Type.CONTROLLER_HANDSHAKE);
		handshake.setName("Controller name");
		handshake.setVersion("0.1");
		handshake.setAuthor("Author name");

		String msg = gson.toJson(handshake);
		send(msg);

		ListBots command = new ListBots();
		command.setType(Command.Type.LIST_BOTS);

		msg = gson.toJson(command);
		send(msg);
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("onClose(), code: " + code + ", reason: " + reason);
	}

	@Override
	public void onMessage(String message) {
		System.out.println("onMessage(): " + message);
	}

	@Override
	public void onError(Exception ex) {
		System.err.println("onError():" + ex);
	}

	public static void main(String[] args) throws URISyntaxException {
		WebSocketClient client = new ControllerClient1(new URI("ws://localhost:50000"), new Draft_10());
		client.connect();
	}

	@Override
	public void send(String message) {
		System.out.println("Sending: " + message);

		super.send(message);
	}
}