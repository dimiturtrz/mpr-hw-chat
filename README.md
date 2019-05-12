clumsy chat client with multicast

the repo is standart eclipse project - you download it, you start it - ordinary java should work too - the file with the main function is ChatClient

before each message you are informed you can send "TEXT, VIDEO or IMAGE" message
after this you either type a message or give path to the file
since it's tested on localhost i left in the loopback address - for "real usage" however from different machines and ip addresses one should uncomment the line in the constructor
sent files are named simply "receivedFile" - to view them add their extension
