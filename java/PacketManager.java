package spp.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONValue;

public abstract class PacketManager{
	
	public Socket headerSocket;
	public Socket dataSocket;
	public Address address;
	public ReceiveThread receiveThread;
	
	public PacketManager(Socket headerSocket, Socket dataSocket, Address address) {
		this.headerSocket = headerSocket;
		this.dataSocket = dataSocket;
		this.address = address;
		this.receiveThread = new ReceiveThread(this.headerSocket,this.dataSocket,address) {

			public void onReceive(Packet packet) {
				getInstance().onReceive(packet);
			}

			public void onDisconnect(Address address) {
				getInstance().onDisconnect(address);
			}
			
		};
	}
	
	public void send(Packet packet) {
		PrintWriter outHead = null;
		PrintWriter outData = null;
		try {
			outHead = new PrintWriter(this.headerSocket.getOutputStream(), true);
			outData = new PrintWriter(this.dataSocket.getOutputStream(), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			String pid = Integer.toString(packet.pid);
			int pidLength = pid.length();
			for (int i = 0; i < (Settings.settings.packetIDSize - pidLength); i++) {
				pid = pid + 'n';
			}
			outHead.println(pid);
			String dataCount = Integer.toString(packet.getData().size());
			for (int i = 0; i < (Settings.settings.dataCountSize - Integer.toString(packet.getData().size()).length()); i++) {
				dataCount = dataCount + 'n';
			}
			outHead.println(dataCount);
			for (PacketData packetData : packet.getData()) {
				Object data = packetData.data;
				int dataType = packetData.dataType;
				
				String did = Integer.toString(packetData.did);
				int didLength = did.length();
				for (int i = 0; i < Settings.settings.dataIDSize - didLength; i++) {
					did = did + 'n';
				}
				outHead.println(did);
				outHead.println(Integer.toString(dataType));
				int dataSize = 0;
				switch (dataType) {
				case 1:
					dataSize = data.toString().length();
					break;
				case 2:
					dataSize = ((Integer) data).toString().length();
					break;
				case 3:
					dataSize = ((Double) data).toString().length();
					break;
				case 4:
					dataSize = 1;
					if (((Boolean) data) == true) {
						data = 0;
					}else{
						data = 1;
					}
					break;
				case 5:
					//Pass for now until file sending/receiving protocol is implemented
					break;
				case 6:
					data = JSONValue.toJSONString(data);
					dataSize = ((String) data).length();
					break;
				}
				String dataSizeString = Integer.toString(dataSize);
				int dataLength = dataSizeString.length();
				for (int i = 0; i < (Settings.settings.dataSizeLength - dataLength); i++) {
					dataSizeString = dataSizeString + 'n';
				}
				outHead.println(dataSizeString);
				outData.println(data.toString());
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void safeDisconnect() {
		try {
		this.headerSocket.close();
		this.dataSocket.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private PacketManager getInstance() {
		return this;
	}
	
	public abstract void onReceive(Packet packet);
	public abstract void onDisconnect(Address address);

}
