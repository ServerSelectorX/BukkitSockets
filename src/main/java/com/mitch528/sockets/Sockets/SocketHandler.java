package com.mitch528.sockets.Sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.mitch528.sockets.events.MessageReceived;
import com.mitch528.sockets.events.MessageReceivedEvent;
import com.mitch528.sockets.events.SocketConnected;
import com.mitch528.sockets.events.SocketConnectedEvent;
import com.mitch528.sockets.events.SocketDisconnected;
import com.mitch528.sockets.events.SocketDisconnectedEvent;
import com.mitch528.sockets.events.SocketHandlerReady;
import com.mitch528.sockets.events.SocketHandlerReadyEvent;

public class SocketHandler {

	private Socket sock;

	private int bytesReceived = 0;
	private int messageSize = -1;

	private byte[] buffer = new byte[4];

	private InputStream in;
	private OutputStream out;

	private SocketConnected connected;
	private SocketDisconnected disconnected;
	private MessageReceived message;
	private SocketHandlerReady ready;

	private String hostName;

	private int id;

	public SocketHandler() {
		this.disconnected = new SocketDisconnected();
		this.message = new MessageReceived();
		this.connected = new SocketConnected();
		this.ready = new SocketHandlerReady();
	}

	public SocketHandler(Socket sock, int id) {
		this.sock = sock;
		this.id = id;

		this.connected = new SocketConnected();
		this.disconnected = new SocketDisconnected();
		this.message = new MessageReceived();
		this.ready = new SocketHandlerReady();
	}

	public void handleConnection() throws IOException {
		if (sock == null) {
			disconnect();
			return;
		}

		this.hostName = sock.getInetAddress().getCanonicalHostName();

		in = sock.getInputStream();
		out = sock.getOutputStream();

		if (in == null || out == null) {
			disconnect();
			return;
		}
		
		ready.executeEvent(new SocketHandlerReadyEvent(this, this));
		connected.executeEvent(new SocketConnectedEvent(this, this, id));
		
		startReading();
	}

	public void sendMessage(String message) throws IOException {
		if (sock.isConnected() && !sock.isClosed())
			writeToStream(message);
	}

	private void startReading() throws IOException {
		if (!sock.isConnected() || sock.isClosed()) {
			disconnect();
			return;
		}

		buffer = new byte[buffer.length - bytesReceived];

		if (bytesReceived == -1) { // end of stream
			disconnect();
			return;
		}

		bytesReceived += in.read(buffer);

		if (messageSize == -1) { // still reading size of data
			if (bytesReceived == 4) { // received size information
				messageSize = ByteBuffer.wrap(buffer).getInt(0);

				if (messageSize < 0) {
					throw new IllegalStateException("Message size is < 0");
				}

				buffer = new byte[messageSize];

				bytesReceived = 0;
			}

			if (messageSize != 0) { // need more data
				startReading();
			}
		} else {
			if (bytesReceived == messageSize) { // message body received
				StringBuffer sb = new StringBuffer();
				sb.append(new String(buffer));

				message.executeEvent(new MessageReceivedEvent(this, id, sb.toString()));

				// reset
				bytesReceived = 0;
				messageSize = -1;
				buffer = new byte[4];

				startReading(); // start reading again
			} else { // need more data
				startReading();
			}
		}
	}

	private void writeToStream(String message) throws IOException {

		if (!sock.isConnected() || sock.isClosed() || out == null)
			return;

		byte[] sizeinfo = new byte[4];

		byte[] data = message.getBytes();

		ByteBuffer bb = ByteBuffer.allocate(sizeinfo.length);
		bb.putInt(message.getBytes().length);

		out.write(bb.array());
		out.write(data);
		out.flush();
	}

	public void disconnect() throws IOException {
		System.out.println("Client disconnecting");

		sock.shutdownInput();
		sock.shutdownOutput();

		sock.close();

		disconnected.executeEvent(new SocketDisconnectedEvent(this, id));
	}

	public void setSocket(Socket sock) {
		this.sock = sock;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getHostName() {
		return hostName;
	}

	public SocketConnected getConnected() {
		return connected;
	}

	public SocketDisconnected getDisconnected() {
		return disconnected;
	}

	public MessageReceived getMessage() {
		return message;
	}

	public Socket getSocket() {
		return sock;
	}

	public SocketHandlerReady getReady() {
		return ready;
	}

}
