import sys
import socket
import json
import uuid
from threading import Thread
'''
@author opensourcehiker
Python module for SPP protocol
Change the values below to fit your situation
'''

#Due to the fact that python requires the receiving end of a connection to
#know the size of the data coming through (in the socket.recv method), we set the maximum size of some things here.

#If you see in the example (in the examples folder), each newly created packet has a PID, or Packet ID.
#Setting the Packet ID size to 1 allows you to have up to 9 different types of packets.
#Setting the Packet ID size to 2 allows you to have up to 99 different types of packets.
#PID 3 gives you 999, and so on. Make sense?
packetIDSize = 3
#Each piece of data in a packet has a DID, or Data ID. Having ID's for everything allows you
#to define what a specific packet contains, or in the case of data, what the data is used for.
#The maximum size of the DID essentially does the same thing the packet ID maximum size.
#Setting the Data ID size to 1 allows you to have up to 9 different meanings for data.
#Setting the Data ID size to 2 allows you to have up to 99 different meanings for data.
#DID 3 gives you 999, and so on. Now does it make sense?
dataIDSize = 3
#In SPP, the receiving end needs to know how many pieces of data are in an incoming packet.
#Setting the data count size to 1 allows you to have at a maximum 9 pieces of data in a packet.
#Setting the data count size to 2 allows you to have at a maximum 99 pieces of data in a packet.
#Can you guess what setting it to 3 does?
dataCountSize = 3
#This represents how big the data being sent over can be.
#We set it to default by 4, allowing for the data to be a maximum of 9999 characters.
#Setting it to 1 meanings for a max of 9 characters, 2 is 99 characters and so on.
dataSizeLength = 4
#SPP uses two seperate connections. One for all the information about a packet and its data (headers)
#and another to send the data so there's no confusion between the two.
#This is the header port
headerPort = 25560
#This is the data port
dataPort = 25570
#This is the host that the server is running on.
host = 'localhost'

class Packet():
    def __init__(self,pid):
        self.pid = pid
        self.id = pid
        self.allData = []
        
    def addData(self,data):
        self.allData.append(data)
        
    def removeDataByID(self,did):
        removedData = []
        for packetData in self.allData:
            if (packetData.did == did):
                removedData.append(packetData)
        for remove in removedData:
            self.allData.remove(remove)
            
    def getData(self):
        return self.allData
    
    def getDataByID(self,did):
        for data in self.allData:
            if (data.did == did):
                return data
        return None

class PacketData():
    def __init__(self,did,data):
        self.did = did
        self.data = data
        self.dataType = 0
        if type(data) is str:
            self.dataType = 1
        elif type(data) is int:
            self.dataType = 2
        elif type(data) is float:
            self.dataType = 3
        elif type(data) is bool:
            self.dataType = 4
        elif type(data) is file:
            self.dataType = 5
        else:
            self.dataType = 6
            
class PacketManager():
    
    global packetIDSize
    global dataIDSize
    global dataCountSize
    global dataSizeLength
    global headerPort
    global dataPort
    global host
    
    def __init__(self,headerSock,dataSock,address,onReceive,onDisconnect,lang):
        self.onReceive = onReceive
        self.onDisconnect = onDisconnect
        self.headerSock = headerSock
        self.dataSock = dataSock
        self.address = address
        self.lang = lang
        self.receiveThread = self.ReceiveThread(headerSock,dataSock,address,onReceive,onDisconnect,lang)
        self.receiveThread.setName(uuid.uuid4())
        self.receiveThread.start()
    
    class ReceiveThread(Thread):
        
        def __init__(self,headerSock,dataSock,address,onReceive,onDisconnect,lang):
            self.onReceive = onReceive
            self.onDisconnect = onDisconnect
            self.address= address
            self.lang = lang
            self.headerSock = headerSock
            self.dataSock = dataSock
            Thread.__init__(self)   
            
        def run(self):
            while True:
                try:
                    pid = None
                    dataCount = None
                    if self.lang == 1:
                        pid = int(str(self.headerSock.recv(packetIDSize + 2))[:-1].replace('n',''))
                        dataCount = int(str(self.headerSock.recv(dataCountSize + 2))[:-1].replace('n', ''))
                    else:
                        pid = int(str(self.headerSock.recv(packetIDSize)).replace('n',''))
                        dataCount = int(str(self.headerSock.recv(dataCountSize)).replace('n', ''))
                    
                    packet = Packet(pid)
                    
                    i = 0
                    while i < dataCount:
                        did = None
                        dataType = None
                        dataSize = None
                        data = None
                        if self.lang == 1:
                            did = int(str(self.headerSock.recv(dataIDSize + 2))[:-1].replace('n',''))
                            dataType = int(str(self.headerSock.recv(3))[:-1])
                            dataSize = int(str(self.headerSock.recv(dataSizeLength + 2))[:-1].replace('n', ''))
                            data = str(self.dataSock.recv(dataSize + 2))[:-1]
                        else:
                            did = int(str(self.headerSock.recv(dataIDSize)).replace('n',''))
                            dataType = int(self.headerSock.recv(1))
                            dataSize = int(str(self.headerSock.recv(dataSizeLength)).replace('n', ''))
                            data = self.dataSock.recv(dataSize)
                        if dataType is 1:
                            data = str(data)
                        elif dataType is 2:
                            data = int(str(data))
                        elif dataType is 3:
                            data = float(str(data))
                        elif dataType is 4:
                            if int(str(data)) is 0:
                                data = True
                            else:
                                data = False
                        elif dataType is 5:
                            '''Pass for now until file sending/receiving protocol is implemented'''
                            pass
                        else:
                            data = json.loads(str(data))
                        
                        packetData = PacketData(did,data)
                        packet.addData(packetData)
                        i = i + 1
                    packet.address = self.address
                    self.onReceive(packet)
                except:
                    self.onDisconnect(self.address)
                    break
                
    #Safely disconnect the server by first closing the sockets            
    def safeDisconnect(self):
        self.headerSock.close()
        self.dataSock.close()
        
    def send(self,packet):
            i = 0
            pid = str(packet.pid)
            pidLength = len(pid)
            while i < (packetIDSize - pidLength):
                pid = pid + 'n'
                i = i + 1
            def string(msg):
                sentBytes = 0
                newMessage = str(msg)
                if self.lang == 1:
                    newMessage = newMessage + '\n'
                while sentBytes < len(newMessage):
                    sent = self.headerSock.send(str(newMessage)[sentBytes:])
                    if sent == 0:
                        raise RuntimeError("Header socket connection broken")
                    sentBytes = sentBytes + sent
                    
            def dataPiece(msg):
                sentBytes = 0
                newMessage = str(msg)
                if self.lang == 1:
                    newMessage = newMessage + '\n'
                while sentBytes < len(newMessage):
                    sent = self.dataSock.send(str(newMessage)[sentBytes:])
                    if sent == 0:
                        raise RuntimeError("Data socket connection broken")
                    sentBytes = sentBytes + sent
                    
            string(pid)
            i = 0
            dataCount = str(len(packet.getData()))
            while i < (dataCountSize - len(str(len(packet.getData())))):
                dataCount = dataCount + 'n'
                i = i + 1
          
            string(dataCount)
            for packetData in packet.getData():
                data = packetData.data
                dataType= packetData.dataType
                
                i = 0
                did = str(packetData.did)
                didLength = len(did)
                while i < (dataIDSize - didLength):
                    did = did + 'n'
                    i = i + 1
                string(did)
               
                string(dataType)
                dataSize = 0
                if dataType is 1:
                    dataSize = len(data)
                elif dataType is 2:
                    dataSize = len(str(data))
                elif dataType is 3:
                    dataSize = len(str(data))
                elif dataType is 4:
                    dataSize = 1
                    if data is True:
                        data = 0
                    else:
                        data = 1
                elif dataType is 5:
                    '''Pass for now until file sending/receiving protocol is implemented'''
                    pass
                else:
                    data = json.dumps(data)
                    dataSize = len(data)
                i = 0
                dataSize = str(dataSize)
                dataLength = len(dataSize)
                while i < (dataSizeLength - dataLength):
                    dataSize = dataSize + 'n'
                    i = i + 1
                
                string(dataSize)
                dataPiece(str(data))
            
class PacketServer(Thread):
    
    global headerPort
    global dataPort
    global host
    
    def __init__(self,onReceive,onConnect,onDisconnect):
        self.onReceive = onReceive
        self.onConnect = onConnect
        self.onDisconnectMethod = onDisconnect
        self.onDisconnect = self.onServerDisconnect
        self.managers = []
        self.headerSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.headerSock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.headerSock.bind((host, headerPort))
        self.headerSock.listen(5)
        self.dataSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.dataSock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.dataSock.bind((host, dataPort))
        self.dataSock.listen(5)
        Thread.__init__(self)
        self.setName(uuid.uuid4())
        self.start()
        
    def onServerDisconnect(self,address):
        self.onDisconnectMethod(address)
        removeManager = None
        for manager in self.managers:
            if (manager.address == address):
                removeManager = manager
        if removeManager is not None:
            self.managers.remove(removeManager)
    def run(self):
        while True:
            (headerClient, headerAddress) = self.headerSock.accept()
            (dataClient, dataAddress) = self.dataSock.accept()
            if (str(headerAddress[0]) == str(dataAddress[0])):
                headerClient.send(str(0))
                lang = int(str(headerClient.recv(1)))
                fullAddress = [headerAddress[0],headerAddress[1],dataAddress[1]]
                self.managers.append(PacketManager(headerClient,dataClient,fullAddress,self.onReceive,self.onDisconnect,lang))
                self.onConnect(fullAddress)
                                     
    def sendAll(self,packet):
        for manager in self.managers:
            manager.send(packet)
            
    def send(self,address,packet):   
        for manager in self.managers:
            if (str(manager.address[0]) == str(address[0])):
                manager.send(packet)
                
    def safeDisconnect(self):
        self.headerSock.close()
        self.dataSock.close()
        for manager in self.managers:
            manager.safeDisconnect()
        
class PacketClient():
    
    global headerPort
    global dataPort
    
    def __init__(self,address,onReceive,onConnect,onDisconnect):
        headerSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        headerSock.connect((address,headerPort))
        dataSock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        dataSock.connect((address,dataPort))
        lang = int(str(headerSock.recv(1)))
        headerSock.send(str(0))
        self.manager = PacketManager(headerSock,dataSock,address,onReceive,onDisconnect,lang)
        onConnect(address)
        
    def send(self,packet):
        self.manager.send(packet)
        
    def safeDisconnect(self):
        self.manager.safeDisconnect()
