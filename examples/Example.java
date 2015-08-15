package spp.java.example;
import java.util.List;

import spp.java.*;

//There are 3 event methods that are called at specific times
//1. onReceive method to be called when a packet is received. Just pass the packet as an argument.
//When a packet is received the address is attached to the packet. For server side the address is
//in the format the address of a socket is in when you accept a connection:
//address[0] is the IP, address[1] is the header port, and address[2] is the data port.
//Client-side the address is just the host you are connected to.
//2. onConnect method to be called when either a server accepts a client or the client connects
//to the server. You just need to pass the address as an argument. The format is the same for onReceive.
//3. onDisconnect method to be called when a client disconnects or on client-side the connection is broken.
//Just like onConnect, you need to pass the address as an argument. Same format as onConnect.

/**
*An example script showing how to use the packet module created by opensourcehiker August 13, 2015
*@author opensourcehiker
*/
public class Example {
	
	//Used for the server onConnect method.
	Address clientAddress;
	
	//The main execution method
	public static void main(String[] args) {
		//We'll just start the example..
		new Example();
	}
	
	//Two methods that allow us to discern data printed from server vs client
	//Normally the server program would be seperate from that of the client though

	public void clientMsg(String msg) {
		System.out.println("[CLIENT] " + msg);
	}
	
	public void serverMsg(String msg) {
		System.out.println("[SERVER] " + msg);
	}
	
	//Remember how we need to create 3 seperate methods, one for each event?
			
	//This will be the receive code for the server
	//See how we pass the packet as an argument?
	
	@SuppressWarnings("unchecked")
	public void onServerReceive(Packet packet) {
		//We'll show how to pick at the return address of the packet for you visual learners'
	    Address address = packet.address;
	    //Now we'll dissect the address into the IP and port like we explained above
	    //First the IP..
	    String ip = address.host;
	    //And the packet header port..
	    int headerPort = address.headerPort;
	    //And the data port..
	    int dataPort = address.dataPort;
	    //Now we'll alert the user a packet has been received
	    serverMsg("Packet received from " + ip + " using header port " + Integer.toString(headerPort) + " and data port " + Integer.toString(dataPort) + "!");
	    //Now we'll alert the user about the info of the packet.'
	    //First, we'll tell the user the PID of the packet
	    int pid = packet.pid;
	    serverMsg("PID: " + Integer.toString(pid));
	    //Then how many pieces of data it contains..
	    int dataSize = packet.getData().size();
	    serverMsg("The packet has " + Integer.toString(dataSize) + " pieces of data.");
	    //And finally the data itself
	    //We'll also display what number the data is
	    for (int dataCount = 0; dataCount < dataSize; dataCount++) {
	    	//We'll get the data at that point in the list
	        PacketData data = packet.getData().get(dataCount);
	        //We'll print a line with the number of the data to make it easier to read
	        serverMsg("----" + Integer.toString(dataCount + 1) + "----");
	        //Now we'll print the DID of the data
	        int did = data.did;
	        serverMsg("DID: " + Integer.toString(did));
	        //And the data type (remember the data type protocol above?_
	        int type = data.dataType;
	        //We'll make it a bit easier for the user to read the data type
	        String typeString = null;
	        switch (type) {
	        case 1:
	        	typeString = "String";
	        	break;
	        case 2:
	        	typeString = "Integer";
	        	break;
	        case 3:
	        	typeString = "Double";
	        	break;
	        case 4:
	        	typeString = "Boolean";
	        	break;
	        case 5:
	        	//TODO (data type 5 is a file, remember?)
	        	break;
	        case 6:
	        	typeString = "Other";
	        	break;
	        }
	        serverMsg("Data Type: " + typeString);
	        //And finally the data
	        Object actualData = data.data;
	       //We'll just show you that the list was reassembled correctly too
	        //NOTE: This code printing the contents of the list works ONLY because we know its a list.
	        if (typeString != "Other") {
	        	serverMsg("Data: " + actualData.toString());
	        }else{
	        	//We'll even show you that each piece of data is in the correct position!
	        	for (int listCount = 0; listCount < ((List<Object>) actualData).size(); listCount++) {
	        		 //Now for the data IN the list..
	                 Object listData = ((List<Object>) actualData).get(listCount);
	                 //And voila!
	                 serverMsg(Integer.toString(listCount + 1) + ": " + listData.toString());
	        	}
	        }
	    }
	    //We'll add an extra-long line at the end to signify the end of the packet.
	    serverMsg("---------------------");      
	}
	
	//This will be the connect code for the server
	//See how we pass the address as an argument?
	//NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)

	public void onServerConnect(Address address) {
		//First we'll save the address for later..
		this.clientAddress = address;
		//We'll just print that we got a connection
	    serverMsg("New connection from " + address.host + ".");
	}
	
	//This will be the disconnect code for the server
	//See how we pass the address as an argument?
	//NOTE{ Remember that the address argument for the server is different from the client's (don't remember? read above)
	public void onServerDisconnect(Address address) {
	    //We'll just print that we disconnected from the client(READ BELOW THOUGH)
	    //Although one might use the disconnect event/method if say they used the address of each client as a key to another piece of data
	    //about the client in a dictionary, where on disconnect they would remove the client's address w/ its value from the dictionary
	    serverMsg("Disconnected from " + address.host + ".");
	}
	
	//This will be the receive code for the client
	//See how we pass the packet as an argument?
	@SuppressWarnings("unchecked")
	public void onClientReceive(Packet packet) {
		//For the purpose of this example, we'll just do what the server does
	    //The address attached to a received packet for the client is just the server address like we explained above
	    Address address = packet.address;
	    //Now we'll alert the user a packet has been received
	    clientMsg("Packet received from " + address.host + "!");
	    //Now we'll alert the user about the info of the packet.'
	    //First, we'll tell the user the PID of the packet
	    int pid = packet.pid;
	    clientMsg("PID: " + Integer.toString(pid));
	    //Then how many pieces of data it contains..
	    int dataSize = packet.getData().size();
	    clientMsg("The packet has " + Integer.toString(dataSize) + " pieces of data.");
	    //And finally the data itself
	    //We'll also display what number the data is
	    for (int dataCount = 0; dataCount < dataSize; dataCount++) {
	    	//We'll get the data at that point in the list
	        PacketData data = packet.getData().get(dataCount);
	        //We'll print a line with the number of the data to make it easier to read
	        clientMsg("----" + Integer.toString(dataCount + 1) + "----");
	        //Now we'll print the DID of the data
	        int did = data.did;
	        clientMsg("DID: " + Integer.toString(did));
	        //And the data type (remember the data type protocol above?_
	        int type = data.dataType;
	        //We'll make it a bit easier for the user to read the data type
	        String typeString = null;
	        switch (type) {
	        case 1:
	        	typeString = "String";
	        	break;
	        case 2:
	        	typeString = "Integer";
	        	break;
	        case 3:
	        	typeString = "Double";
	        	break;
	        case 4:
	        	typeString = "Boolean";
	        	break;
	        case 5:
	        	//TODO (data type 5 is a file, remember?)
	        	break;
	        case 6:
	        	typeString = "Other";
	        	break;
	        }
	        clientMsg("Data Type: " + typeString);
	        //And finally the data
	        Object actualData = data.data;
	       //We'll just show you that the list was reassembled correctly too
	        //NOTE: This code printing the contents of the list works ONLY because we know its a list.
	        if (typeString != "Other") {
	        	clientMsg("Data: " + actualData.toString());
	        }else{
	        	//We'll even show you that each piece of data is in the correct position!
	        	for (int listCount = 0; listCount < ((List<Object>) actualData).size(); listCount++) {
	        		 //Now for the data IN the list..
	                 Object listData = ((List<Object>) actualData).get(listCount);
	                 //And voila!
	                 clientMsg(Integer.toString(listCount + 1) + ": " + listData.toString());
	        	}
	        }
	    }
	    //We'll add an extra-long line at the end to signify the end of the packet.
	    clientMsg("---------------------");      
	}
	
	//This will be the connect code for the client
	//See how we pass the address as an argument?
	//NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)

	public void onClientConnect(Address address) {
	    //We'll just print that we got a connection
	    clientMsg("Connected to " + address.host + ".");
	}
	
	//This will be the disconnect code for the client
	//See how we pass the address as an argument?
	//NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)
	public void onClientDisconnect(Address address) {
	    //We'll just print that we disconnected from the server
	    clientMsg("Disconnected from server.");
	    
	}
	
	public Example() {
		
		//Now we are going to create an example packet.
		//The Packet class passes the PID(Packet ID) as an argument.
		//In this case we will give the packet a PID of 315.
		//NOTE: When assigning packet ID's the maximum length is determined by the value packetIDSize
		
		Packet packet = new Packet(315);
		//Now we can add data to the packet. Data in a packet is represented by the PacketData class,
		//which passes 2 arguments. The first is the DID(Data ID), and the second is the actual data.
		//We'll add one of each supported data type.
		/*
		Data Type Protocol:
		1. String
		2. Integer
		3. Double
		4. Boolean
		5. (TODO) File
		6. Other (Program uses JSON to convert value to string and back)
		*/
		packet.addData(new PacketData(1,"Testing 123"));
		//We'll add an integer with DID 2
		packet.addData(new PacketData(2,123));
		//We'll add a float with DID 3
		packet.addData(new PacketData(3,1.23));
		//We'll add a boolean with DID 4
		packet.addData(new PacketData(4,true));
		//We'll add a list(other) of different types of data with DID 5
		packet.addData(new PacketData(5,new Object[]{"321 Testing",123,1.23,true}));
	
		//Now we'll start the server w/ the event methods we created..
		PacketServer server = new PacketServer() {

			public void onReceive(Packet packet) {
				onServerReceive(packet);
			}

			public void onConnect(Address address) {
				onServerConnect(address);
				
			}

			public void onDisconnect(Address address) {
				onServerDisconnect(address);
				
			}
			
		};
		//As well as the client w/ the event methods we created (and the address to connect to..
		PacketClient client = new PacketClient("localhost") {

			public void onReceive(Packet packet) {
				onClientReceive(packet);
			}

			public void onConnect(Address address) {
				onClientConnect(address);
				
			}

			public void onDisconnect(Address address) {
				onClientDisconnect(address);
				
			}
			
		};

		//Now all that's left to do is send the packet we created earlier
		//We'll send the same packet from server to client and vice verca
		//We'll start with the client
		client.send(packet);
		//We'll delay a second or else the log gets mixed up(remove the delay and you'll see)
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//And then the server.
		//NOTE: For the purpose of the tutorial, we'll send the packet to the client specifically
		//by using the address we saved above(remember?) but you can send a packet to all clients with server.sendAll(packet)  
		server.send(clientAddress, packet);
		//Hope that example helped!
	}
	
}