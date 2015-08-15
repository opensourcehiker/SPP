package spp.java;

import java.util.ArrayList;
import java.util.List;

public class Packet {
	public int pid;
	public List<PacketData> allData = new ArrayList<PacketData>();
	public Address address;
	
	public Packet(int pid) {
		this.pid = pid;
	}
	
	public void addData(PacketData data) {
		allData.add(data);
	}
	
	public void removeDataByID(int did) {
		List<PacketData> removedData =  new ArrayList<PacketData>();
		for (PacketData packetData : this.allData) {
			if (packetData.did == did) {
				removedData.add(packetData);
			}
		}
		
		for (PacketData data : removedData) {
			this.allData.remove(data);
		}
	}
	
	public List<PacketData> getData() {
		return this.allData;
	}
	
	public PacketData getDataByID(int did) {
		for(PacketData data : this.allData) {
			if (data.did == did) {
				return data;
			}
		}
		return null;
	}
	
}
