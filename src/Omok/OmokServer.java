package Omok;

import java.net.*;

import java.io.*;

import java.util.*;

public class OmokServer{
   ServerSocket server;
   Manager manager=new Manager();    
   Random rnd= new Random();     
  public OmokServer(){}
  void startServer(){                         
    try{
      server=new ServerSocket(8086);
      System.out.println("서버소켓이 생성되었습니다.");
      while(true){   
        Socket socket=server.accept();     
        Omok_Thread ot=new Omok_Thread(socket);
        ot.start();
        manager.add(ot);
        System.out.println("접속자 수: "+manager.size());
      }
    }catch(Exception e){
      System.out.println(e);
    }
  }
  public static void main(String[] args){
    OmokServer server=new OmokServer();
    server.startServer();
  }
 
 
  class Omok_Thread extends Thread{
      int roomNumber=-1;       
      String userName=null;       
      Socket socket;              
      boolean ready=false; 
      BufferedReader reader;      
      PrintWriter writer;           
    Omok_Thread(Socket socket){      
      this.socket=socket;
    }
    Socket getSocket(){               
      return socket;
    }
    int getRoomNumber(){              
      return roomNumber;
    }
    String getUserName(){              
      return userName;
    }
    boolean isReady(){                
      return ready;
    }
    public void run(){
      try{
    	  reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
         writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
 
        String msg;                    
 
        while((msg=reader.readLine())!=null){
 
     
          if(msg.startsWith("[NAME]")){
            userName=msg.substring(6);         
          }
 
       
          else if(msg.startsWith("[ROOM]")){
            int roomNum=Integer.parseInt(msg.substring(6));
            if( !manager.isFull(roomNum)){                      
             if(roomNumber!=-1)
                manager.sendToOthers(this, "[EXIT]"+userName);        
              roomNumber=roomNum;           
              writer.println(msg);            
              writer.println(manager.getNickNamesInRoom(roomNumber));            
              manager.sendToOthers(this, "[ENTER]"+userName);            }
            else writer.println("[FULL]");        
          }
 
     
          else if(roomNumber>=1 && msg.startsWith("[STONE]"))
            manager.sendToOthers(this, msg);     
          else if(msg.startsWith("[MESAAGE]"))
            manager.sendToRoom(roomNumber,
                              "["+userName+"]: "+msg.substring(9));          
          else if (msg.startsWith("[DM]")) {
        	    String[] parts = msg.split("/", 3);   
        	    if (parts.length >= 3) {
        	        String targetUserName = parts[1];  
        	        String directMessage = parts[2];          	     
        	        manager.DM(targetUserName, "[" + userName + " -> " + targetUserName + "]: " + directMessage);
        	    }
        	}         
          else if(msg.startsWith("[START]")){
            ready=true;   
            if(manager.isReady(roomNumber)){
              int a=rnd.nextInt(2);
              if(a==0){
                writer.println("[COLOR]BLACK");
                manager.sendToOthers(this, "[COLOR]WHITE");
              }
              else{
                writer.println("[COLOR]WHITE");
                manager.sendToOthers(this, "[COLOR]BLACK");
              }
            }
          } 
            else if(msg.startsWith("[STOPGAME]"))
            ready=false;         
          else if(msg.startsWith("[DROPGAME]")){
            ready=false;             
            manager.sendToOthers(this, "[DROPGAME]");
          }
 
         
          else if(msg.startsWith("[WIN]")){
            ready=false;           
            writer.println("[WIN]");          
            manager.sendToOthers(this, "[LOSE]");
          }  
        }
      }catch(Exception e){
      }finally{
        try{
          manager.remove(this);
          if(reader!=null) reader.close();
          if(writer!=null) writer.close();
          if(socket!=null) socket.close();
          reader=null; writer=null; socket=null;
          System.out.println(userName+"님이 접속을 끊었습니다.");
          System.out.println("접속자 수: "+manager.size());        
          manager.sendToRoom(roomNumber,"[DISCONNECT]"+userName);
        }catch(Exception e){}
      }
    }
  }
  class Manager extends Vector{       
    Manager(){}
    void add(Omok_Thread ot){            
      super.add(ot);
    }
    void remove(Omok_Thread ot){        
       super.remove(ot);
    }
    Omok_Thread getOT(int i){            
      return (Omok_Thread)elementAt(i);
    }
    Socket getSocket(int i){              
      return getOT(i).getSocket();
    }
 
    void DM(String userName, String msg) {
        for (int i = 0; i < size(); i++) {
            Omok_Thread ot = getOT(i);
            if (ot.getUserName().equals(userName)) {
                try {
                    PrintWriter pw = new PrintWriter(ot.getSocket().getOutputStream(), true);
                    pw.println(msg);
                    break;  
                } catch (Exception e) {
                     
                }
            }
        }
    } 
    void sendMessage(int i, String msg){
      try{
        PrintWriter pw= new PrintWriter(getSocket(i).getOutputStream(), true);
        pw.println(msg);
      }catch(Exception e){}  
    }
    int getRoomNumber(int i){            
      return getOT(i).getRoomNumber();
    }
    synchronized boolean isFull(int roomNum){   
      if(roomNum==0)return false;      
      int count=0;
      for(int i=0;i<size();i++)
        if(roomNum==getRoomNumber(i))count++;
      if(count>=2)return true;
      return false;
    }  
    void sendToRoom(int roomNum, String msg){
      for(int i=0;i<size();i++)
        if(roomNum==getRoomNumber(i))
          sendMessage(i, msg);
    }
     void sendToOthers(Omok_Thread ot, String msg){
      for(int i=0;i<size();i++)
        if(getRoomNumber(i)==ot.getRoomNumber() && getOT(i)!=ot)
          sendMessage(i, msg);
    }
     synchronized boolean isReady(int roomNum){
      int count=0;
      for(int i=0;i<size();i++)
        if(roomNum==getRoomNumber(i) && getOT(i).isReady())
          count++;
      if(count==2)return true;
      return false;
    }
     String getNickNamesInRoom(int roomNum){
      StringBuffer sb=new StringBuffer("[PLAYERS]");
      for(int i=0;i<size();i++)
        if(roomNum==getRoomNumber(i))
          sb.append(getOT(i).getUserName()+"\t");
      return sb.toString();
    }
  }
}

