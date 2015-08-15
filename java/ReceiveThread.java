package spp.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.UUID;

import org.json.simple.JSONValue;

public abstract class ReceiveThread implements Runnable{
	
	public Socket headerSocket;
	public Socket dataSocket;
	public Address address;
	private Thread thread;
	
	public ReceiveThread(Socket headerSocket, Socket dataSocket, Address address) {
		this.headerSocket = headerSocket;
		this.dataSocket = dataSocket;
		this.address = address;
		thread = new Thread(this,UUID.randomUUID().toString());
		thread.start();
	}
	@SuppressWarnings("deprecation")
	public void run() {
		BufferedReader inHead = null;
		BufferedReader inData = null;
		try {
			inHead = new BufferedReader(new InputStreamReader(headerSocket.getInputStream()));
			inData = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			try {
				int pid = Integer.parseInt(inHead.readLine());
				Packet packet = new Packet(pid);
				int dataCount = Integer.parseInt(inHead.readLine().replaceAll("n", ""));
				for (int i = 0; i < dataCount; i++) {
					int did = Integer.parseInt(inHead.readLine().replaceAll("n", ""));
					int dataType = Integer.parseInt(inHead.readLine());
					inHead.readLine();
					String dataString = inData.readLine();
					Object data = null;
					switch (dataType) {
					case 1:
						data = dataString;
						break;
					case 2:
						data = Integer.parseInt(dataString);
						break;
					case 3:
						data = Double.parseDouble(dataString);
						break;
					case 4:
						if (Integer.parseInt(dataString) == 0) {
							data = true;
						}else{
							data = false;
						}
						break;
					case 5:
						//Pass for now until file sending/receiving protocol is implemented.
						break;
					case 6:
						data = JSONValue.parse(dataString);
						break;
					}
					
					PacketData packetData = new PacketData(did,data);
					packet.addData(packetData);
				}
				packet.address = this.address;
				this.onReceive(packet);
			}catch(Exception e) {
				this.onDisconnect(this.address);
				break;
			}
		}
			
	}
	
	public abstract void onReceive(Packet packet);
	public abstract void onDisconnect(Address address);

}
