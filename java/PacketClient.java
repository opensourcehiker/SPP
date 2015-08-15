package spp.java;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class PacketClient {
	
	public Socket headerSocket;
	public Socket dataSocket;
	public PacketManager manager;
	
	public PacketClient(String address) {
		DataOutputStream outHead = null;
		InputStreamReader inHead = null;
		try {
		this.headerSocket = new Socket(address,Settings.settings.headerPort);
		this.headerSocket.setReuseAddress(true);
		this.dataSocket = new Socket(address,Settings.settings.dataPort);
		this.dataSocket.setReuseAddress(true);
		outHead = new DataOutputStream(this.headerSocket.getOutputStream());
		inHead = new InputStreamReader(headerSocket.getInputStream());
		char[] test = new char[1];
		inHead.read(test);
		outHead.writeBytes(Integer.toString(1));
		outHead.flush();
		}catch (IOException e) {
			e.printStackTrace();
		}
		Address fullAddress = new Address(address,Settings.settings.headerPort,Settings.settings.dataPort);
		manager = new PacketManager(this.headerSocket,this.dataSocket,fullAddress) {

			public void onReceive(Packet packet) {
				getInstance().onReceive(packet);
			}

			public void onDisconnect(Address address) {
				getInstance().onDisconnect(address);
			}
			
		};
		this.onConnect(fullAddress);
		
	}
	
	public abstract void onReceive(Packet packet);
	public abstract void onConnect(Address address);
	public abstract void onDisconnect(Address address);
	
	
	public void send(Packet packet) {
		this.manager.send(packet);
	}
	
	public void safeDisconnect() {
		this.manager.safeDisconnect();
	}
	
	private PacketClient getInstance() {
		return this;
	}
}

