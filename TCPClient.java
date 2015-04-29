import java.io.*;
import java.net.*;
import java.util.Scanner;


class TCPClient
{
   static int port1 = 0;
   static int id =-1;
   public static void main(String[] args)
   {
    String message = "";
      try
      {
        port1 = Integer.parseInt(args[0]);
        message = "0;9;2;3;4;5";
         BufferedReader inFromUser =
            new BufferedReader(new InputStreamReader(System.in));
         DatagramSocket clientSocket = new DatagramSocket();
         InetAddress IPAddress = InetAddress.getByName("localhost");
         byte[] sendData = new byte[1024];
         byte[] receiveData = new byte[1024];
         String sentence = message;
         sendData = sentence.getBytes();
         DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port1);
         clientSocket.send(sendPacket);
         DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
         clientSocket.receive(receivePacket);
         String modifiedSentence = new String(receivePacket.getData());
         System.out.println("FROM SERVER:" + modifiedSentence);
         clientSocket.close();
      }
      catch(IOException e)
      {
         System.out.println("Error");
      }
   }
}



/*import java.io.*;
import java.net.*;

class TCPClient
{
 public static void main(String args[]) throws Exception
 {
    String sentence;
    String modifiedSentence;
    BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
    Socket clientSocket = new Socket("localhost", Integer.parseInt(args[0]));
    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
    System.out.println("sent");
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    sentence = "0;3;0;0;0";  
    outToServer.writeBytes(sentence + '\n');
    modifiedSentence = inFromServer.readLine();
    System.out.println("FROM SERVER: " + modifiedSentence);
    clientSocket.close();
 }
}*/