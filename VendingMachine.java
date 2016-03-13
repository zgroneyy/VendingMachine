/*
	CS421 Computer Networks Programming Assignment I
	Dr. Cem Tekin
	Due: 03-13-2016, Sunday
	Özgür Öney 21101821
	Mert Ege Şener 21100947
	Client-server based, Java implemented program that simulates a vending machine scenario. 
*/
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class VendingMachine {

    public static void main(String args[]) throws Exception
    {
			int port=0;
			String[][] data;
			String IP="";
            //If entered command is like "java VendingMachine [<IP_address>] <port_number>" , indicates client
			if(args.length==2)
			{
				IP=args[0];
				port=Integer.parseInt(args[1]);
			}
			//Else if entered command is like "java VendingMachine <port_number>", indicates server
			else if(args.length==1)
				port=Integer.parseInt(args[0]);
			
			//If there is no IP address, we should work with server routines
			if(IP.equals(""))
			{
				//Get file name
				String fileName = "item_list.txt";
            	int linecount = 0; 
				BufferedReader file = new BufferedReader(new FileReader(fileName));
				String line;
				//Get number of lines in our .txt file, therefore size of future array that contains data will be determined. 
				while ((line = file.readLine()) != null) {
					linecount++;
				}
				//To prevent confusion of readers, since first operation (counting) is ended up with file, file reader is closed
				file.close();
				
				//Create data array to keep information in .txt file
				String itemlist[][] = new String[linecount][3];
				BufferedReader file2 = new BufferedReader(new FileReader(fileName));

				int i=0;
				//Here, values are inserted into above-defined array
				//NOTE: It is assumed that items in the .txt file are separated /w TAB character. 
				//Just after while loop below, itemlist[][] is the 2D array where we keep the data of .txt file
				while ((line = file2.readLine()) != null) {
					System.out.println(line);
					String[] tempitemlist = line.split("\\t"); 
				
					itemlist[i][0] = tempitemlist[0];
					itemlist[i][1] = tempitemlist[1];
					itemlist[i++][2] = tempitemlist[2];
				
				}
				file2.close();
				
				//We have created server-side database
				System.out.println("Database created. You may use the program now...");
				//Now, Server socket is defined in a way that it will use port entered by user. 
				ServerSocket connectionSocket = new ServerSocket(port);
            	//Now Server is looking for contacts
            	
            	while (true) {
            	
            		System.out.println("Listening\n");
            		boolean connectionCont=true;
					Socket acceptionSocket = connectionSocket.accept(); //letting people connect
					System.out.println("Client has connected. ");
					//Defining readers and outputs where simply shows data flow from client & to client.
					BufferedReader inFromClient = new BufferedReader(new InputStreamReader(acceptionSocket.getInputStream()));
					DataOutputStream outToClient = new DataOutputStream(acceptionSocket.getOutputStream());
					//Try to do necessities of connection as long as client is forced to connect
					try {
						while(connectionCont)
						{
							//Read what client wrote
							String request_string = inFromClient.readLine();
							
							//If client sends data of GET ITEM LIST
							if(request_string.equals("GET ITEM LIST"))
							{
								//Go through the itemlist array, where we keep the data of items
								//Send them one by one
								for(int ii=0; ii<linecount; ii++)
								{
									outToClient.writeBytes(itemlist[ii][0] + " " + itemlist[ii][1] + " " + itemlist[ii][2] + "\r\n");
								
								}
								outToClient.writeBytes("\r\n");
								outToClient.flush();
							}
							//If client sends data of GET ITEM
							else if(request_string.equals("GET ITEM"))
							{
								//User will enter a number, which indicates item's ID
								String item= inFromClient.readLine();
								//Dummy array to be used in ID matching as well as extracting number after delivery.
								String tempo[]= item.split(" ");
								boolean match = false;
								for(int ii=0; ii<linecount; ii++)
								{
									//You find the element that user's looking for
									if(tempo[0].equals(itemlist[ii][0]))
									{
										//You should sign match as true
										match = true;
										//If there is sufficient items, no problem in delivery. 
										//Extract number of desired elements from database, return SUCCESS
										if (Integer.parseInt(tempo[1]) <= Integer.parseInt(itemlist[ii][2])) {
											itemlist[ii][2] = ""+ (Integer.parseInt(itemlist[ii][2]) - Integer.parseInt(tempo[1]));
											outToClient.writeBytes("SUCCESS\r\n\r\n");
											outToClient.flush();
											break;
										}
										//There is no sufficient elements to deliver, return OUT OF STOCK
										else {
											outToClient.writeBytes("OUT OF STOCK\r\n\r\n");
											outToClient.flush();
											break;
										}
										
									}
								}
								//Item that user is looking for is not available in our database, there for no delivery is possible. 
								if (!match) {
									outToClient.writeBytes("OUT OF STOCK\r\n\r\n");
									outToClient.flush();
								}
							}
							//inFromClient.readLine();
						}
					}
					//Whenever user wants to leave, simply let it go and throw exception. 
					catch (Exception e) {
						System.out.println("Connection closed by client.");
					}
            	}
			}
			//Means that IP is not empty string, which indicates that this is client. 
			else
			{
				//Create socket for client with given IP & port number.
				Socket clientSocket = new Socket(IP, port);
				//Create I/O operations of client, which is data comin' from Server and data goes to server. 
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
				Scanner scan = new Scanner(System.in);
				//Arraylist created to keep balance, what did a client buy? 
				ArrayList<String[]> summary = new ArrayList<String[]>();
				System.out.println("Connection established.");
				//While there is a connection
				while(true) {
					//User is forced to choose between these 3 every time
					System.out.print("Choose a message type (GET ITEM (L)IST, (G)ET ITEM, (Q)UIT):");
					char input = scan.nextLine().charAt(0);
					//Send server "GET ITEM LIST", which results in server's all-item answer
					if ( input == 'L' ) {
						outToServer.writeBytes("GET ITEM LIST\r\n");
						//Basic control for data comin out of server.
						boolean cont = true;
						while ( cont ) {
							
							String response = inFromServer.readLine();
							//If server has send nothing, or finished everything, no need to wait anymore
							if ( response.equals("") ) {
								cont = false;
							}
							//As long as we have something to send
							else {
								System.out.println(response);
							}
						}
					}
					//Send server GET ITEM command
					else if ( input == 'G' ) {
						//Client will send its data /w a newline as well as there will be a space between itemID and item#
						String[] temp = scan.nextLine().split(" ");
						outToServer.writeBytes("GET ITEM\r\n" + temp[0] + " " + temp[1] + "\r\n");
						String response = inFromServer.readLine();
						inFromServer.readLine();
						//Indicates that VendingMachine can satisfy the request
						if (response.equals("SUCCESS")) {
							boolean match = false;
							//Increase the number of items in specific ID items in summary array. 
							for ( int i = 0; i < summary.size(); i++)
								if ( summary.get(i)[0].equals(temp[0]) ) {
									summary.get(i)[1] =  ""+(Integer.parseInt(summary.get(i)[1]) + Integer.parseInt(temp[1]));
									match = true;
								}
							
							if (!match) {
								summary.add(temp);
							}
							//Return SUCCESS response
							System.out.println("The response is:\n" + response);
						}
						//Return OUT OF STOCK response
						else {
							System.out.println("The response is:\n" + response);
						}
					}
					//Client now indicates that it wants to quit
					else if ( input == 'Q') {
						//Simply print the summary
						for ( int i = 0; i < summary.size(); i++)
							System.out.println(summary.get(i)[0] + " " + summary.get(i)[1]);
						//Say goodbye
						clientSocket.close();
						break;
					}
					//If client select something rather than G, L or Q
					else {
						//do nothing
					}
					
				}
				
			}
        }
}
        