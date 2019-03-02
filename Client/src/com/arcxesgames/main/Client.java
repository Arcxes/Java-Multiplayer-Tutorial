package com.arcxesgames.main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import com.arcxesgames.packets.RemoveConnectionPacket;

public class Client implements Runnable{
	
	private String host;
	private int port;
	
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	private boolean running = false;
	private EventListener listener;
	
	public Client(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public void connect() {
		try {
			socket = new Socket(host,port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			listener = new EventListener();
			new Thread(this).start();
		}catch(ConnectException e) {
			System.out.println("Unable to connect to the server");
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			running = false;
			RemoveConnectionPacket packet = new RemoveConnectionPacket();
			sendObject(packet);
			in.close();
			out.close();
			socket.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendObject(Object packet) {
		try {
			out.writeObject(packet);
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			running = true;
			
			while(running) {
				try {
					Object data = in.readObject();
					listener.received(data);
				}catch(ClassNotFoundException e) {
					e.printStackTrace();
				}catch(SocketException e) {
					close();
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

}
