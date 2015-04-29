import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.*;
import java.net.*;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class checkpoint {

	LinkedList<Integer> neighbours;
    int check,check1;
    int version,iterator;
    final int maxdelay = 5000;
	LinkedList<String> hostname;
    int[] label_value;
    static int totalnodes;
    LinkedList<String> port;
    LinkedList<Integer> nextHost;
    LinkedList<String> hostWork;
    LinkedList<String> lls,llr,vclock,itr;
    int[] last_label_rcvd, first_label_sent , vectorClock, vclockbackup,last_label_Sent;
    LinkedList<Integer> coverednodes;
    int[] last_checkpoint_rcvd,last_checkpoint_sent;
    static int id;
    static boolean sendappmessages,close;
    int taskid;
    String taskval;
    boolean task;


	public checkpoint(int id)
	{
		this.id = id;
        version = 0;
        iterator = 0;
        task = false;
        taskval= "e";
        taskid = -1;
        check = 0;
        check1 = 0;
        hostname = new LinkedList<>();
        sendappmessages = true;
        vclock = new LinkedList<>();
        itr = new LinkedList<>();
        llr = new LinkedList<>();
        lls = new LinkedList<>();
        coverednodes = new LinkedList<>();
        port = new LinkedList<>();
		neighbours = new LinkedList<>(); 
		nextHost = new LinkedList<>();
		hostWork = new LinkedList<>();
	}

	void startThread() {
        final int numProcesses = neighbours.size();
        final int nos = totalnodes;
        vectorClock = new int[nos]; 
        last_label_Sent = new int[numProcesses];
        last_label_rcvd = new int[numProcesses];
        label_value = new int[numProcesses];
        first_label_sent = new int[numProcesses];
        vclockbackup = new int[nos];
        last_checkpoint_rcvd = new int[numProcesses];
        last_checkpoint_sent = new int[numProcesses];
        new serverThread(this).start();
        new appThread(this).start();
        new appControlThread(this).start();
    }
    class serverThread extends Thread {

        checkpoint server;

        public serverThread(checkpoint main) {
            this.server = main;
            for(int i=0;i<last_label_rcvd.length;i++)
            {
            	last_label_rcvd[i] = -1;
            	first_label_sent[i] = -1;
            	last_checkpoint_rcvd[i] = -1;
                close = false;
                label_value[i] = 0;
            	last_checkpoint_sent[i] = -1;
            }

            for(int j=0;j<vectorClock.length;j++)
            {
                vectorClock[j]=0;
                vclockbackup[j]=0;
            }
        }

        public String sender(String msg, int id) {
            String sentence = msg;
            String modifiedSentence = "";
            //System.out.println(msg);
            try {
                BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));
                 DatagramSocket clientSocket = new DatagramSocket();
                 InetAddress IPAddress = InetAddress.getByName(server.hostname.get(id - 1));
                 byte[] sendData = new byte[1024];
                 byte[] receiveData = new byte[1024];
                 sentence = msg;
                 sendData = sentence.getBytes();
                 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(server.port.get(id - 1)));
                 clientSocket.send(sendPacket);
                 clientSocket.close();
                 /*DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                 clientSocket.receive(receivePacket);
                 modifiedSentence = new String(receivePacket.getData());
                 System.out.println("FROM SERVER:" + modifiedSentence);
                 clientSocket.close();*/
            } catch (IOException e) {

            }
            return modifiedSentence;
        }

        public void updateVC(String vc)
        {
            String vcs[] = vc.split(",");
            for(int i=0;i<totalnodes;i++)
            {
                if(i==id)
                {
                    if(Integer.parseInt(vcs[i])>vectorClock[i])
                    {
                        vectorClock[i] = Integer.parseInt(vcs[i])+1;
                    }
                    else
                    {
                        vectorClock[i] +=1;
                    }
                }
                else
                {
                    vectorClock[i] = Integer.parseInt(vcs[i]);
                }
            }

        }

        public String covered()
        {
        	String cov = "";
        	/*for(int i:neighbours)
        	{
        		cov += i+",";
        	}*/
            for(int i:coverednodes)
            {
                cov += i+",";
            }
        	//cov +=id+1+",";
        	return cov;
        }
        public boolean incovs(int x)
        {
            for(int i:coverednodes)
            {
                if(i==x)
                    return true;
            }
            return false;
        }

        public void addDone(String nodes)
        {
            //System.out.println("nodes"+nodes);
        	String allnodes[] = nodes.split(",");
        	for(int i=0;i<allnodes.length;i++)
        	{
                if(!incovs(Integer.parseInt(allnodes[i])))
        		  coverednodes.add(Integer.parseInt(allnodes[i]));
        	}
            if(!incovs(id+1))
                coverednodes.add(id+1);
        }

        public boolean inDone(int s)
        {
        	for(int i:coverednodes)
        	{
                //System.out.println("i="+i+" s= "+s);
        		if(i==s)
        			return true;
        	}
        	return false;
        }
        public void takecheckpoint()
        {
            String l="";
            String s = "";
            String k = "";
            for(int j=0;j<vectorClock.length;j++)
            {
                k+=vectorClock[j]+",";
                vclockbackup[j] = vectorClock[j];
            }
            for(int i=0;i<last_label_rcvd.length;i++)
            {
                last_checkpoint_rcvd[i] = last_label_rcvd[i];
                l+=last_label_rcvd[i]+",";
                s+=first_label_sent[i]+",";
                last_checkpoint_sent[i] = first_label_sent[i];
                last_label_rcvd[i] = -1;
                first_label_sent[i] = -1; 
            }
            lls.add(s);
            llr.add(l);
            vclock.add(k);
            //System.out.println(vclock.get(vclock.size()-1));
            check =1;
            System.out.println("Taking checkpoint --  - - - - - --  -- - - - - - - -- ");

        }

        public void recover()
        {
            /*if(lls.size()>0)
            {
                String w[] = lls.get(lls.size()-1).split(",");
                String p[] = llr.get(llr.size()-1).split(",");
                for(int i=0;i<last_label_rcvd.length;i++)
                {
                    last_label_rcvd[i] = Integer.parseInt(p[i]);
                    first_label_sent[i] = Integer.parseInt(w[i]);
                    last_label_rcvd[i]=-1;
                }
                lls.remove(lls.size()-1);
                llr.remove(llr.size()-1);
            }
            else{
                for(int i=0;i<last_label_rcvd.length;i++)
                {
                    last_label_rcvd[i] = -1;
                    first_label_sent[i] = -1;;
                    last_label_rcvd[i]=-1;
                }
            }*/
            for(int i=0;i<last_label_rcvd.length;i++)
                last_label_rcvd[i] = -1;
            check =1;
            System.out.println("Before recovering");
            for(int j=0;j<vectorClock.length;j++)
            {
                System.out.println(j+1+" : "+vectorClock[j]);
                vectorClock[j] = vclockbackup[j];
            }

            System.out.println("Taking recovery --  - - - - - --  -- - - - - - - -- ");

            System.out.println("After recovering");
            for(int j=0;j<vectorClock.length;j++)
            {
                System.out.println(j+1+" : "+vectorClock[j]);
            }
        }


        public String floodNetwork(String message)
        {
        	//System.out.println("neigbours"+" "+neighbours);
            int x=0;
        	for(int s:neighbours)
        	{
        		if(!inDone(s))
        		{
        			//System.out.println("sending to "+s);
        			sender(message+";"+last_label_rcvd[x]+";"+iterator+";"+version,s);
        		}
                x++;
        	}
        	return "Test";
        }

        public String floodNetwork1(String message)
        {
            //System.out.println("neigbours"+" "+neighbours);
            int x=0;
            for(int s:neighbours)
            {
                if(!inDone(s))
                {
                    //System.out.println("sending to "+s);
                    sender(message+";"+last_label_Sent[x]+";"+iterator+";"+version,s);
                    System.out.println(message+";"+last_label_Sent[x]+";"+iterator+";"+version);
                }
                x++;
            }
            return "Test";
        }

        public void setLLR(int index,int val)
        {
            last_label_rcvd[index] = val;
        }

        public void printllr()
        {
            for(int i=0;i<last_label_rcvd.length;i++)
            {
                //System.out.println("label check:"+server.neighbours.get(i)+":"+last_label_rcvd[i]+":"+first_label_sent[i]);
            }
        }


        public int getfLS(int x)
        {
            return first_label_sent[x];
        }
        public int getRealIndex(int x)
        {
            int t = 0;
            for(int i:neighbours)
            {
                //System.out.println(i+"i+x "+x);
                if(i==x)
                {
                    return t;
                }
                t +=1;
            }
            return -1;
        }

        public String messageProcessor(DatagramPacket receivePacket, String message, DatagramSocket serverSocket) {
	        //addDone(message.split(";")[1]);
	        //floodNetwork("test");

            String[] messageparts = message.split(";");
            String from = messageparts[0];
            String type = messageparts[1];
            String vc = messageparts[2];
            String reply = "";


            if(Integer.parseInt(type)==Message.APP.id)
            {
                //System.out.println(getRealIndex(Integer.parseInt(from)+1)+" "+from);
                setLLR(getRealIndex(Integer.parseInt(from)+1),Integer.parseInt(messageparts[3]));
                printllr();
                updateVC(vc);
                reply = "OK";
            }

            else if(Integer.parseInt(type)==Message.CHECKPOINT.id)
            {
                System.out.print("vector clock for iteration "+iterator+" :");
                for(int i =0;i<vectorClock.length;i++)
                {
                    System.out.print(vectorClock[i]+" ");
                }
                System.out.println();
                sendappmessages = false;
                try{
                    appControlThread.sleep(100);
                    appThread.sleep(100);
                }
                catch(Exception e){}
                if(from.equals(""+id+""))
                {
                    while(coverednodes.size()>0)
                    {
                        coverednodes.remove(coverednodes.size()-1);
                    }
                    addDone(vc);
                    takecheckpoint();
                    floodNetwork(id+";"+1+";"+covered());
                }
                else
                {
                    while(coverednodes.size()>0)
                    {
                        coverednodes.remove(coverednodes.size()-1);
                    }

                    addDone(vc);
                System.out.println(Integer.parseInt(from)+1 + " "+getRealIndex(Integer.parseInt(from)+1)+" "+neighbours);
                if(getRealIndex(Integer.parseInt(from)+1) != -1)
                {
                if(Integer.parseInt(messageparts[3])>=getfLS(getRealIndex(Integer.parseInt(from)+1))&&getfLS(getRealIndex(Integer.parseInt(from)+1))>-1&&check==0)
                    takecheckpoint();
                coverednodes.add(-1);
                floodNetwork(id+";"+1+";"+covered());
                }
                //version = Integer.parseInt(messageparts[messageparts.length-1])+1;
                try{
                    appControlThread.sleep(100);
                    appThread.sleep(100);
                }
                catch(Exception e){}}
                //iterator ++;
                reply="checkpointinggg";
            }

            else if(Integer.parseInt(type)==Message.STEP.id)
            {
                
            }
            else if(Integer.parseInt(type)==Message.RECOVER.id)
            {
                try{
                    appControlThread.sleep(100);
                    appThread.sleep(100);
                }
                catch(Exception e){}
                if(from.equals(""+id+""))
                {
                    while(coverednodes.size()>0)
                    {
                        coverednodes.remove(coverednodes.size()-1);
                    }
                    addDone(vc);
                    recover();
                    floodNetwork1(id+";"+3+";"+covered());
                }
                if(!from.equals(""+id+""))
                {
                    while(coverednodes.size()>0)
                    {
                        coverednodes.remove(coverednodes.size()-1);
                    }
                    addDone(vc);
                    System.out.println("-----------------here--------------");
                    if(Integer.parseInt(messageparts[3])<last_label_rcvd[getRealIndex( Integer.parseInt(from)+1)]&&check==0)
                        recover();
                    floodNetwork1(id+";"+3+";"+covered());
                }
                
                //iterator ++;
                reply="recover";
                
            }
            else if(Integer.parseInt(type)==Message.VER.id)
            {
                /*if(Integer.parseInt(from)==id+1)
                {}
                else
                    iterator = id;
                try{
                    appControlThread.sleep(100);
                    appThread.sleep(100);
                }
                catch(Exception e){}
                //for(int i1=0;i1<server.hostname.size();i1++)
                    //if(i1!=id)
                        //sender(id+";"+4+";-1;"+iterator,i1+1);*/

                
            }
            else
            {
                System.out.println("Got this message: "+message);
                reply = "ok";
            }
            return reply;
        }

        @Override
        public void run() {
        	
            //if(id==0)
            	//messageProcessor("test1;-1");
            String host = server.hostname.get(id);
            String clientSentence;
            int port = Integer.parseInt(server.port.get(id));
            String replymessage;
            try {
                String port2 = server.port.get(id);
                DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port2));
                byte[] receiveData = new byte[1024];
                byte[] sendData = new byte[1024];
                while (close == false) {
                    System.out.println("Listening....");
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    String sentence = "";
                    sentence = "";
                    sentence = new String( receivePacket.getData(),0, receivePacket.getLength());
                    //System.out.println("RECEIVED: " + sentence);
                    messageProcessor(receivePacket, sentence, serverSocket);
                    InetAddress IPAddress = receivePacket.getAddress();
                      int port1 = receivePacket.getPort();
                      sendData = "ok".getBytes();
                      DatagramPacket sendPacket =
                      new DatagramPacket(sendData, sendData.length, IPAddress, port1);
                      serverSocket.send(sendPacket);
                    //messageProcessor(sentence);
                    //System.out.println("11sent11");
                }
            } catch (IOException e) {

            }
        }

    }
    class State {
        int n;
        public State() {}
        public State(int n) {
            this.n = n;
        }
    }

    enum Message {

        APP(0), CHECKPOINT(1), STEP(2), RECOVER(3), VER(4);

        final int id;

        Message(int id) {
            this.id = id;
        }

        int getMessageId() {
            return id;
        }
    }



    class appThread extends Thread
    {
        checkpoint server;
        public appThread(checkpoint main)
        {
            this.server = main;
        }

        public void setfLS(int index,int labelvalue)
        {
            if(first_label_sent[index]==-1)
                first_label_sent[index] = labelvalue; 

            last_label_Sent[index] = labelvalue;
        }

        public int getlabelvalue(int index)
        {
            label_value[index] +=1;
            return label_value[index];
        }

        public String vectorclock()
        {
            String vcs = "";
            vectorClock[id]+=1;
            for(int i=0;i<vectorClock.length;i++)
            {

                if(i!=vectorClock.length-1)
                    vcs += vectorClock[i]+",";
                else
                    vcs += vectorClock[i];
            }
            return vcs;
        }

        public void sendmessage()
        {
            int id1 = (int) (Math.random() * (neighbours.size()));
            //System.out.println(id1);
            //System.out.println(id);
            int lbval = getlabelvalue(id1);
            String msg = id+";0;"+vectorclock()+";"+lbval;
            setfLS(id1,lbval);
            id1 = neighbours.get(id1);
            //System.out.println(id1+" neighbour "+neighbours);
            //System.out.println("sending...." + msg);
            try {
                BufferedReader inFromUser =
                    new BufferedReader(new InputStreamReader(System.in));
                 DatagramSocket clientSocket = new DatagramSocket();
                 InetAddress IPAddress = InetAddress.getByName(server.hostname.get(id1 - 1));
                 byte[] sendData = new byte[1024];
                 byte[] receiveData = new byte[1024];
                 String sentence = msg;
                 sendData = sentence.getBytes();
                 DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(server.port.get(id1 - 1)));
                 clientSocket.send(sendPacket);
                 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                 clientSocket.receive(receivePacket);
                 String modifiedSentence = new String(receivePacket.getData());
                 //System.out.println("FROM SERVER:" + modifiedSentence);
                 clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            }   
        }

        @Override
        public void run() {
            //System.out.println(nextHost.size()+" hostwork number- - - ----");
            while(iterator<nextHost.size())
            {
                if(true)
                {
                    try{
                    Thread.sleep(1000);

                    }
                    catch(Exception e)
                    {}
                   // System.out.println("Check here:"+sendappmessages);
                    sendmessage();
                }
                else
                {
                    //System.out.println("hereeeeeeeeeee"+sendappmessages);
                }
            }
            
        }
    }


    class appControlThread extends Thread
    {
        checkpoint server;
        int id;
        public appControlThread(checkpoint main)
        {
            this.server = main;
            this.id = main.id;
        }

        public void writetofile()
        {

        }

        @Override
        public void run() {
            int i = 0;
            
            while(iterator<nextHost.size())
            {
                try{
                    appControlThread.sleep(maxdelay);
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
                if(sendappmessages == false)
                {
                    
                    sendappmessages = true;
                    check = 0;
                    check1 = 0;
                    //System.out.println("Check:"+sendappmessages);
                    iterator +=1;
                    System.out.println("Iterator:"+iterator);
                    try{
                    appThread.sleep(maxdelay);
                    //System.out.println("heyyyyyyy------"+sendappmessages+"------");
                    }
                    catch(Exception e)
                    {
                        System.out.println(e);
                    }
                }
                else
                {
                    try{
                    appControlThread.sleep(1000);
                    //System.out.println("heyyyyyyy------"+sendappmessages+"------");
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
                    sendappmessages = false;
                    //System.out.println("Check:"+sendappmessages);
                    //System.out.println(hostWork.size()+" hostwork "+iterator);
                    if(hostWork.size()>iterator)
                    {
                        int x = 0;
                        x = id+1;
                        //System.out.println(nextHost.get(iterator)+" "+x);
                        if(nextHost.get(iterator)==id+1 && hostWork.get(iterator).equals("c"))
                        {

                            String msg = id+";1;"+"-1;100000;"+version+";"+iterator;
                            try {
                            BufferedReader inFromUser =
                                new BufferedReader(new InputStreamReader(System.in));
                            //System.out.println("Taking 1");
                             DatagramSocket clientSocket = new DatagramSocket();
                             InetAddress IPAddress = InetAddress.getByName(server.hostname.get(id));
                             byte[] sendData = new byte[1024];
                             byte[] receiveData = new byte[1024];
                             String sentence = msg;
                             sendData = sentence.getBytes();
                             try{
                                    appControlThread.sleep(500);
                                }
                                catch(Exception e)
                                {
                                    System.out.println(e);
                                }
                             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(server.port.get(id)));
                             clientSocket.send(sendPacket);
                             //System.out.println("Taking 2");
                             try{
                                    appControlThread.sleep(500);
                                }
                                catch(Exception e)
                                {
                                    System.out.println(e);
                                }
                             DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                             clientSocket.receive(receivePacket);
                             String modifiedSentence = new String(receivePacket.getData());
                             clientSocket.close();
                             //iterator++;
                            } catch (IOException e) {
                                System.out.println(e);
                            }  

                        }

                        if(nextHost.get(iterator)==id+1 && hostWork.get(iterator).equals("r"))
                        {
                            String msg = id+";3;"+"-1;100000;"+version+";"+iterator;
                            try {
                            BufferedReader inFromUser =
                                new BufferedReader(new InputStreamReader(System.in));
                            //System.out.println("Taking 1");
                             DatagramSocket clientSocket = new DatagramSocket();
                             InetAddress IPAddress = InetAddress.getByName(server.hostname.get(id));
                             byte[] sendData = new byte[1024];
                             byte[] receiveData = new byte[1024];
                             String sentence = msg;
                             sendData = sentence.getBytes();
                             try{
                                    appControlThread.sleep(500);
                                }
                                catch(Exception e)
                                {
                                    System.out.println(e);
                                }
                             DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Integer.parseInt(server.port.get(id)));
                             clientSocket.send(sendPacket);
                             System.out.println("Taking 2");
                             try{
                                    appControlThread.sleep(500);
                                }
                                catch(Exception e)
                                {
                                    System.out.println(e);
                                }
                             DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                             clientSocket.receive(receivePacket);
                             String modifiedSentence = new String(receivePacket.getData());
                             clientSocket.close();
                             System.out.println("Taking 3");
                            // iterator++;
                        } catch (IOException e) {
                            System.out.println(e);
                        } 


                        
                        }
                        else
                        {
                            System.out.println("no no no");
                        }
                        
                    }
                }
                i++;
            }
            for(int j=0;j<vclock.size();j++)
            {
                System.out.println(vclock.get(j));
            }
             
            
        }
    }





	public static void main(String args[]) {
        File f = new File("config.txt");
        id = Integer.parseInt(args[0]);
        checkpoint main = new checkpoint(id);
        int totalneigbhours = 0 ;
        try {
            Scanner reader = new Scanner(f);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                System.out.println(line + "!");
                if (!line.startsWith("#")) {
                    int numberOfNodes = Integer.parseInt(line);
                    totalnodes = numberOfNodes;
                    break;
                }
            }
            int cntr = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                System.out.println(line + "?");
                if(line.startsWith("$$"))
                {
                	break;
                }
                if (!line.startsWith(" ") && !line.startsWith("#")
                        && !line.equals("") && !line.startsWith("$$") && !line.startsWith("--")) {
                    String[] split = line.split("\t+| +");
                    String host = split[0];
                    String port = split[1];
                    if (id == cntr) {
                        for (int i = 2; i < split.length; i++) {
                            main.neighbours.add(Integer.parseInt(split[i]));
                        }

                    }
                    main.hostname.add(host);
                    main.port.add(port);
                    cntr++;
                }
            }

            while(reader.hasNextLine())
            {
            	String line = reader.nextLine();
            	System.out.println(line + "$");
            	if (!line.startsWith(" ") && !line.startsWith("#")
                        && !line.equals("") && !line.startsWith("$$") && !line.startsWith("--")) {
                    String[] split = line.split("\t+| +");
                    String host = split[0];
                    String type = split[1];
                    main.nextHost.add(Integer.parseInt(host));
                    main.hostWork.add(type);
                }
                if(line.startsWith("--"))
                {
                    break;
                }
            }
            if(main.nextHost.get(0)==id)
            {
                main.task = true;
                main.taskid = id;
                main.taskval = main.hostWork.get(0); 
            }

            main.startThread();


        } catch (FileNotFoundException ex) {
            //Logger.getLogger(Print.class.getName()).log(Level.SEVERE,null,ex);
        }
    }
}