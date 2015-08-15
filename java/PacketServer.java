package spp.java;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class PacketServer implements Runnable {
	
	public List<PacketManager> managers = new ArrayList<PacketManager>();
	public ServerSocket headerSocket;
	public ServerSocket dataSocket;
	private Thread thread;
	
	public PacketServer() {
		try {
		this.headerSocket = new ServerSocket(Settings.settings.headerPort);
		this.headerSocket.setReuseAddress(true);
		this.dataSocket = new ServerSocket(Settings.settings.dataPort);
		this.dataSocket.setReuseAddress(true);
		}catch (IOException e) {
			e.printStackTrace();
		}
		thread = new Thread(this,UUID.randomUUID().toString());
		thread.start();
		}
	
	public void run() {
		while (true) {
			Socket headerSocket = null;
			Socket dataSocket = null;
			DataOutputStream outHead = null;
			InputStreamReader inHead = null;
			try {
			headerSocket = this.headerSocket.accept();
			dataSocket = this.dataSocket.accept();
			outHead = new DataOutputStream(headerSocket.getOutputStream());
			inHead = new InputStreamReader(headerSocket.getInputStream());
			outHead.writeBytes(Integer.toString(1));
			outHead.flush();
			inHead.read();
			}catch (IOException e) {
				e.printStackTrace();
			}
			String headerAddress = headerSocket.getRemoteSocketAddress().toString().split(":")[0].replaceAll("/","");
			String dataAddress = dataSocket.getRemoteSocketAddress().toString().split(":")[0].replaceAll("/","");
			int headerPort = headerSocket.getPort();
			int dataPort = dataSocket.getPort();
			if (headerAddress.equalsIgnoreCase(dataAddress)) {
				Address fullAddress = new Address(headerAddress,headerPort,dataPort);
				this.managers.add(new PacketManager(headerSocket,dataSocket,fullAddress) {

					public void onReceive(Packet packet) {
						getInstance().onReceive(packet);
					}

					public void onDisconnect(Address address) {
						getInstance().onServerDisconnect(address);
					}
					
				});
				this.onConnect(fullAddress);
			}
		}
	}
	
	public void sendAll(Packet packet) {
		for (PacketManager manager : this.managers) {
			manager.send(packet);
		}
	}
	
	public void send(Address address, Packet packet) {
		for (PacketManager manager : this.managers) {
			if (manager.address.host == address.host) {
				manager.send(packet);
			}
		}
	}
	
	public abstract void onReceive(Packet packet);
	public abstract void onConnect(Address address);
	public abstract void onDisconnect(Address address);
	
	private void onServerDisconnect(Address address) {
		this.onDisconnect(address);
		PacketManager removeManager = null;
		for (PacketManager manager : this.managers) {
			if (manager.address == address) {
				removeManager = manager;
			}
		}
		this.managers.remove(removeManager);
	}
	
	public void safeDisconnect() {
		try {
		this.headerSocket.close();
		this.dataSocket.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
		for (PacketManager manager : this.managers) {
			manager.safeDisconnect();
		}
	}
	
	private PacketServer getInstance() {
		return this;
	}
	
	
}
