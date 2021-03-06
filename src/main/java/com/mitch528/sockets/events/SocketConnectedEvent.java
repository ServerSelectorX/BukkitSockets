package com.mitch528.sockets.events;

import java.util.EventObject;

import com.mitch528.sockets.Sockets.SocketHandler;

@SuppressWarnings("serial")
public class SocketConnectedEvent extends EventObject
{
	
	private int id;
	
	private SocketHandler handler;
	
	public SocketConnectedEvent(Object source, SocketHandler handler, int id)
	{
		
		super(source);
		
		this.id = id;
		this.handler = handler;
		
	}
	
	public SocketHandler getHandler()
	{
		return handler;
	}
	
	public int getID()
	{
		return id;
	}
	
}
