# Java-Chat-Server
Java Chat Server

About:
----------------------------------------------

This is simple network-based multi threaded chat application written in Java, using the Java Socket API.

The server starts and listens for connections on a particular port passed as a command line parameter. The client starts and tries to connect to the server using an IP address and port, both passed as command line parameters.

Once a connection is made, the server requests a user name from the client and stores that name in a list. If another client tries to connect, their user name will be checked against the list and refused if it is a duplicate of an existing client.

The server uses a thread pool (20 max connections) that assigns a handler to each of the clients. Once a client types a message, the handler distributes the message to all other clients. The server also can communicate to all users from the console.

If the client quits the chat, the socket is closed at both the client and server side. The server then removes the user name from the list and continues to function with the rest of the clients.

The server can terminate the connection with one or more clients which will cause the client side program to exit. In addition, the server can shutdown by closing all active sockets and terminate the program.

On the client side, the program runs with two threads, one for transmitting and one for receiving messages. This way, there are no blockages between input and output operations as these operations are performed in parallel.

If the client is unable to reach the server, the message "Could not connect to host..." is displayed and the application exits.

If the network connection is lost during the session, both client and server will close the socket on their end. The Client application will display the following message "the connection to the server on port:.... has finished" and then exit.

\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


Instructions:
---------------------------------------------

I have added 2 clients in order to show that the chat server can facilitate communication between two or more users at the same time.

Server
Once compiled, to start the server in console mode use one of the following commands:

java ChatServer
java ChatServer portNumber

If the port number is not specified, the application will use the default port: 59001
To disconnect a particular client use: \q name
where name is the user name if the client
To shutdown server use: \q


Client
Once compiled, to start the client in console mode use one of the following commands:

java ChatClient
java ChatClient hostName portNumber

If the hostName and port number are not specified, the application will use the default host: localhost and default port number: 59001
To quit the client program use: \q


References
----------------------------------------------

https://stackoverflow.com/questions/33853189/multithreaded-client-server-chat-application-in-java

https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/




