package spp.java;

public class Settings {
	
	//Due to the fact that python requires the receiving end of a connection to
		//know the size of the data coming through (in the socket.recv method), we set the maximum size of some things here.

		//If you see in the example (in the examples folder), each newly created packet has a PID, or Packet ID.
		//Setting the Packet ID size to 1 allows you to have up to 9 different types of packets.
		//Setting the Packet ID size to 2 allows you to have up to 99 different types of packets.
		//PID 3 gives you 999, and so on. Make sense?
		public int packetIDSize = 3;
		//Each piece of data in a packet has a DID, or Data ID. Having ID's for everything allows you
		//to define what a specific packet contains, or in the case of data, what the data is used for.
		//The maximum size of the DID essentially does the same thing the packet ID maximum size.
		//Setting the Data ID size to 1 allows you to have up to 9 different meanings for data.
		//Setting the Data ID size to 2 allows you to have up to 99 different meanings for data.
		//DID 3 gives you 999, and so on. Now does it make sense?
		public int dataIDSize = 3;
		//In SPP, the receiving end needs to know how many pieces of data are in an incoming packet.
		//Setting the data count size to 1 allows you to have at a maximum 9 pieces of data in a packet.
		//Setting the data count size to 2 allows you to have at a maximum 99 pieces of data in a packet.
		//Can you guess what setting it to 3 does?
		public int dataCountSize = 3;
		//This represents how big the data being sent over can be.
		//We set it to default by 4, allowing for the data to be a maximum of 9999 characters.
		//Setting it to 1 meanings for a max of 9 characters, 2 is 99 characters and so on.
		public int dataSizeLength = 4;
		//SPP uses two seperate connections. One for all the information about a packet and its data (headers)
		//and another to send the data so there's no confusion between the two.
		//This is the header port
		public int headerPort = 25560;
		//This is the data port
		public int dataPort = 25570;
		//This is the host that the server is running on.
		public String host = "localhost";
		
		public static Settings settings = new Settings();

}
