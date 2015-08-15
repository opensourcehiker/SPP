package spp.java;

import java.io.File;

public class PacketData {
	
	public int did;
	public Object data;
	public int dataType;
	
	public PacketData(int did, Object data) {
		this.did = did;
		this.data = data;
		
		if (data instanceof String) {
			this.dataType = 1;
		}else if (data instanceof Integer) {
			this.dataType = 2;
		}else if (data instanceof Double) {
			this.dataType = 3;
		}else if (data instanceof Boolean) {
			this.dataType = 4;
		}else if (data instanceof File) {
			this.dataType = 5;
		}else{
			this.dataType = 6;
		}

	 }

}
