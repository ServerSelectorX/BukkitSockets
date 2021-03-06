package com.mitch528.sockets.Sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.mitch528.sockets.events.ServerSocketAccepted;
import com.mitch528.sockets.events.ServerSocketAcceptedEvent;
import com.mitch528.sockets.events.ServerSocketStarted;
import com.mitch528.sockets.events.ServerSocketStartedEvent;
import com.mitch528.sockets.events.SocketHandlerReadyEvent;
import com.mitch528.sockets.events.SocketHandlerReadyEventListener;

public class Server {

	private int port;

	private int counter = 0;

	private List<SocketHandler> handlers = new ArrayList<SocketHandler>();

	private ServerSocket server;

	private ServerSocketStarted started;
	private ServerSocketAccepted accepted;

	public Server(int port) {
		this.port = port;

		this.started = new ServerSocketStarted();
		this.accepted = new ServerSocketAccepted();
	}

	public void startListening() throws IOException {
		server = new ServerSocket(port);

		started.executeEvent(new ServerSocketStartedEvent(this));

		while (true) {
			Socket sock = server.accept();

			final SocketHandler handler = new SocketHandler(sock, ++counter);

			handler.getReady().addSocketHandlerReadyEventListener(new SocketHandlerReadyEventListener() {

				public void socketHandlerReady(SocketHandlerReadyEvent evt) {
					accepted.executeEvent(new ServerSocketAcceptedEvent(this, handler));
				}

			});

			handler.handleConnection();

			handlers.add(handler);
		}
	}

	public void shutdownAll() throws IOException {
		for (SocketHandler handler : handlers) {
			handler.disconnect();
		}

		handlers.clear();
	}

	public void stopServer() throws IOException {
		if (server != null) server.close();
	}

	public SocketHandler[] getHandlers() {
		return handlers.toArray(new SocketHandler[handlers.size()]);
	}

	public SocketHandler getHandler(int index) {
		return handlers.get(index);
	}

	public ServerSocketStarted getServerSocketStarted() {
		return started;
	}

	public ServerSocketAccepted getSocketAccepted() {
		return accepted;
	}

}
