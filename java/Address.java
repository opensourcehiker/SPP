package spp.java;

public class Address {
	public String host;
	public int headerPort;
	public int dataPort;
	
	public Address(String host, int headerPort, int dataPort) {
		this.host = host;
		this.headerPort = headerPort;
		this.dataPort = dataPort;
	}
	
	public Address(String host) {
		this.host = host;
	}

}
