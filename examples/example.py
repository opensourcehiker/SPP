import time
'''
Created by opensourcehiker August 12, 2015
@author opensourcehiker
An example script showing how to use the packet module
'''

#We don't have to download the packet module as a library, just put it in the folder that the script is in.
from packet import Packet,PacketData,PacketServer,PacketClient

#There are 3 event methods that are called at specific times
#1. onReceive method to be called when a packet is received. Just pass the packet as an argument.
#When a packet is received the address is attached to the packet. For server side the address is
#in the format the address of a socket is in when you accept a connection:
#address[0] is the IP, address[1] is the header port, and address[2] is the data port.
#Client-side the address is just the host you are connected to.
#2. onConnect method to be called when either a server accepts a client or the client connects
#to the server. You just need to pass the address as an argument. The format is the same for onReceive.
#3. onDisconnect method to be called when a client disconnects or on client-side the connection is broken.
#Just like onConnect, you need to pass the address as an argument. Same format as onConnect.

#Now we are going to create an example packet.
#The Packet class passes the PID(Packet ID) as an argument.
#In this case we will give the packet a PID of 315.
#NOTE: When assigning packet ID's the maximum length is determined by the value packetIDSize
#
packet = Packet(315)
#Now we can add data to the packet. Data in a packet is represented by the PacketData class,
#which passes 2 arguments. The first is the DID(Data ID), and the second is the actual data.
#We'll add one of each supported data type.
'''
Data Type Protocol:
1. String
2. Integer
3. Float
4. Boolean
5. (TODO) File
6. Other (Program uses JSON to convert value to string and back)
'''
#We'll add a string with DID 1
packet.addData(PacketData(1,"Testing 123"))
#We'll add an integer with DID 2
packet.addData(PacketData(2,123))
#We'll add a float with DID 3
packet.addData(PacketData(3,1.23))
#We'll add a boolean with DID 4
packet.addData(PacketData(4,True))
#We'll add a list(other) of different types of data with DID 5
packet.addData(PacketData(5,['321 Testing',123,1.23,True]))

#Two methods that allow us to discern data printed from server vs client
#Normally the server program would be seperate from that of the client though

def clientMsg(msg):
    print "[CLIENT] " + msg
    
def serverMsg(msg):
    print "[SERVER] " + msg
    
#Remember how we need to create 3 seperate methods, one for each event?

#This will be the receive code for the server
#See how we pass the packet as an argument?
def onServerReceive(packet):
    #We'll show how to pick at the return address of the packet for you visual learners'
    address = packet.address
    #Now we'll dissect the address into the IP and port like we explained above
    #First the IP..
    ip = address[0]
    #And the packet header port..
    headerPort = address[1]
    #And the data port..
    dataPort = address[2]
    #Now we'll alert the user a packet has been received
    serverMsg("Packet received from " + ip + " using header port " + str(headerPort) + " and data port " + str(dataPort) + "!")
    #Now we'll alert the user about the info of the packet.'
    #First, we'll tell the user the PID of the packet
    pid = packet.pid
    serverMsg("PID: " + str(pid))
    #Then how many pieces of data it contains..
    dataSize = len(packet.getData())
    serverMsg("The packet has " + str(dataSize) + " pieces of data.")
    #And finally the data itself
    #We'll also display what number the data is
    dataCount = 0
    while dataCount < dataSize:
        #We'll get the data at that point in the list
        data = packet.getData()[dataCount]
        #We'll print a line with the number of the data to make it easier to read
        serverMsg("----" + str(dataCount + 1) + "----")
        #Now we'll print the DID of the data
        did = data.did
        serverMsg("DID: " + str(did))
        #And the data type (remember the data type protocol above?_
        type = data.dataType
        #We'll make it a bit easier for the user to read the data type
        typeString = ''
        if type is 1:
            typeString = 'String'
        elif type is 2:
            typeString = 'Integer'
        elif type is 3:
            typeString = 'Float'
        elif type is 4:
            typeString = 'Boolean'
        elif type is 5:
            #TODO (data type 5 is a file, remember?)
            pass
        else:
            typeString = 'Other'
        serverMsg('Data Type: ' + typeString)
        #And finally the data
        actualData = data.data
        #We'll just show you that the list was reassembled correctly too
        #NOTE: This code printing the contents of the list works ONLY because we know its a list.
        if (typeString != 'Other'):
            serverMsg('Data: ' + str(actualData))
        else:
            #We'll even show you that each piece of data is in the correct position!
            listCount = 0
            while listCount < len(actualData):
                #Now for the data IN the list..
                listData = actualData[listCount]
                #And voila!
                serverMsg(str(listCount + 1) + ": " + str(listData))
                listCount += 1
        dataCount += 1   
    #We'll add an extra-long line at the end to signify the end of the packet.
    serverMsg( "---------------------")      
          
#This will be the connect code for the server
#See how we pass the address as an argument?
#NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)

#First we'll save the address for later..
clientAddress = None

def onServerConnect(address):
    global clientAddress
    clientAddress = address
    #We'll just print that we got a connection
    serverMsg("New connection from " + address[0] + ".")
    
#This will be the disconnect code for the server
#See how we pass the address as an argument?
#NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)
def onServerDisconnect(address):
    #We'll just print that we disconnected from the client(READ BELOW THOUGH)
    #Although one might use the disconnect event/method if say they used the address of each client as a key to another piece of data
    #about the client in a dictionary, where on disconnect they would remove the client's address w/ its value from the dictionary
    serverMsg("Disconnected from " + address[0] + ".")

#This will be the receive code for the client
#See how we pass the packet as an argument?
def onClientReceive(packet):
    #For the purpose of this example, we'll just do what the server does
     #The address attached to a received packet for the client is just the server address like we explained above
    address = packet.address
    #Now we'll alert the user a packet has been received
    clientMsg("Packet received from " + address + "!")
    #Now we'll alert the user about the info of the packet.'
    #First, we'll tell the user the PID of the packet
    pid = packet.pid
    clientMsg("PID: " + str(pid))
    #Then how many pieces of data it contains..
    dataSize = len(packet.getData())
    clientMsg("The packet has " + str(dataSize) + " pieces of data.")
    #And finally the data itself
    #We'll also display what number the data is
    dataCount = 0
    while dataCount < dataSize:
        #We'll get the data at that point in the list
        data = packet.getData()[dataCount]
        #We'll print a line with the number of the data to make it easier to read
        clientMsg("----" + str(dataCount + 1) + "----")
        #Now we'll print the DID of the data
        did = data.did
        clientMsg("DID: " + str(did))
        #And the data type (remember the data type protocol above?_
        type = data.dataType
        #We'll make it a bit easier for the user to read the data type
        typeString = ''
        if type is 1:
            typeString = 'String'
        elif type is 2:
            typeString = 'Integer'
        elif type is 3:
            typeString = 'Float'
        elif type is 4:
            typeString = 'Boolean'
        elif type is 5:
            #TODO (data type 5 is a file, remember?)
            pass
        else:
            typeString = 'Other'
        clientMsg('Data Type: ' + typeString)
        #And finally the data
        actualData = data.data
        #We'll just show you that the list was reassembled correctly too
        #NOTE: This code printing the contents of the list works ONLY because we know its a list.
        if (typeString != 'Other'):
            clientMsg('Data: ' + str(actualData))
        else:
            #We'll even show you that each piece of data is in the correct position!
            listCount = 0
            while listCount < len(actualData):
                #Now for the data IN the list..
                listData = actualData[listCount]
                #And voila!
                #TODO: Run example program/READ log to see :P
                clientMsg(str(listCount + 1) + ": " + str(listData))
                listCount += 1
        dataCount += 1   
    #We'll add an extra-long line at the end to signify the end of the packet.
    clientMsg( "---------------------")     

#This will be the connect code for the client
#See how we pass the address as an argument?
#NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)


def onClientConnect(address):
    #We'll just print that we got a connection
    clientMsg("Connected to " + address + ".")
    
#This will be the disconnect code for the client
#See how we pass the address as an argument?
#NOTE: Remember that the address argument for the server is different from the client's (don't remember? read above)
def onClientDisconnect(address):
    #We'll just print that we disconnected from the server
    clientMsg("Disconnected from server.")

#Now we'll start the server w/ the event methods we created..
server = PacketServer(onServerReceive,onServerConnect,onServerDisconnect)
#As well as the client w/ the event methods we created (and the address to connect to..
client = PacketClient('localhost',onClientReceive,onClientConnect,onClientDisconnect)

#Now all that's left to do is send the packet we created earlier
#We'll send the same packet from server to client and vice verca
#We'll start with the client
client.send(packet)
#We'll delay a second or else the log gets mixed up(remove the delay and you'll see)
time.sleep(1)
#And then the server.
#NOTE: For the purpose of the tutorial, we'll send the packet to the client specifically
#by using the address we saved above(remember?) but you can send a packet to all clients with server.sendAll(packet)  
server.send(clientAddress, packet)

#Hope that example helped!