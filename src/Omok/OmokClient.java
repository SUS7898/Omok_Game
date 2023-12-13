
package Omok;

import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import java.awt.event.*;
import java.awt.geom.*;
import java.awt.List;


import java.util.concurrent.locks.ReentrantLock;

public class OmokClient extends Frame implements Runnable, ActionListener{
	final String ip  = "localhost"; 
	String id; 
	String index_name; 
		  boolean loggedIn = false;  
		UserDAO dao = new UserDAO();
	    TextArea chatArea=new TextArea("", 1,1,1);    
	    TextField inputField=new TextField("");         
	    TextField nameField=new TextField();          
	    TextField roomNumField=new TextField("0");        
	    Label userLabel=new Label("대기실:  명");
	    List UserList=new List();  
	    Button startBtn=new Button("대국 시작");    
	    Button stopBtn=new Button("기권");        
	    Button watingBtn=new Button("대기방으로");   
	    Button lobbyBtn=new Button("로비");       	  
	  String selectedId;
	  DMDidalog dmDialog;
	   JTextField dmtf;
	   JButton dmBtn;
	    Label popUpLabel=new Label("오목게임 자바", 1);	  
	    OmokBoard board=new OmokBoard(15,30);      
	    BufferedReader reader;                         
	    PrintWriter writer;                                
	    Socket socket;                              
	    int roomNum=-1;                          
	    String userName=null;                        
	    LoginPanel loginPanel = new LoginPanel(100,300); 
	    SignupPanel singPanel = new SignupPanel(100,300);
	  
	  public OmokClient(String title){                         
//	    super(title);
	    setLayout(null);                               
	 add(singPanel);
	 add(loginPanel);
	 disableAllPanels();
     enableLoginPanel(); 
	   
	    chatArea.setEditable(false);
	    popUpLabel.setBounds(10,30,480,30);
	    popUpLabel.setBackground(Color.LIGHT_GRAY);
	    board.setLocation(10,70);
	    add(popUpLabel);
	    add(board);
	    Panel EditPanel=new Panel();
	    
	    EditPanel.setBackground(Color.magenta);
	    EditPanel.setLayout(new GridLayout(3,3));
	    EditPanel.add(new Label("이     름:", 2));EditPanel.add(nameField);
	    EditPanel.add(new Label("방 번호:", 2)); EditPanel.add(roomNumField);
	    EditPanel.add(watingBtn); EditPanel.add(lobbyBtn);
	    watingBtn.setEnabled(false);
	    EditPanel.setBounds(500,30, 250,70);
	    
	    Panel ListPnael=new Panel();
	    ListPnael.setBackground(Color.CYAN);
	    ListPnael.setLayout(new BorderLayout());
	    Panel GameBtnPanel=new Panel();
	    GameBtnPanel.add(startBtn); GameBtnPanel.add(stopBtn);
	    ListPnael.add(userLabel,"North"); ListPnael.add(UserList,"Center"); ListPnael.add(GameBtnPanel,"South");
	    startBtn.setEnabled(false); stopBtn.setEnabled(false);
	    ListPnael.setBounds(500,110,250,180);
	    
	    //DM 기능 리스너
	    UserList.addMouseListener(new MouseAdapter() {
	        @Override
	        public void mouseClicked(MouseEvent e) {
	            
	            if (e.getClickCount() == 2 && UserList.getSelectedIndex() != -1) {	                
	                String selectedItem = UserList.getSelectedItem();
	                selectedId = selectedItem; 
	                dmDialog = new DMDidalog();
	                dmDialog.setBounds(200, 200, 200, 100);
	                dmDialog.setVisible(true);
	            }
	        }
	    });


	    
	    Panel ChatPanel=new Panel();
	    ChatPanel.setLayout(new BorderLayout());
	    ChatPanel.add(chatArea,"Center");
	    ChatPanel.add(inputField, "South");
	    ChatPanel.setBounds(500, 300, 250,250);	    
	    add(EditPanel); add(ListPnael); add(ChatPanel);
	 
 
	    inputField.addActionListener(this);
	    watingBtn.addActionListener(this);
	    lobbyBtn.addActionListener(this);
	    startBtn.addActionListener(this);
	    stopBtn.addActionListener(this);   


	    addWindowListener(new WindowAdapter(){
	       public void windowClosing(WindowEvent we){
	         System.exit(0);
	       }
	    });
	  }
	  class DMDidalog extends JDialog{
		   DMDidalog(){
			   super(OmokClient.this);			   
			   dmtf = new JTextField();
			   dmBtn = new JButton("DM 발송");
			   dmBtn.addActionListener(OmokClient.this);			   
			   add(dmtf);
			   add(dmBtn,BorderLayout.EAST);
		   }
	   }
	 
	  //  액션 이벤트 처리
	  public void actionPerformed(ActionEvent ae){		 
		 Object source = ae.getSource();  
	    if(source==inputField){             // 메시지 입력 상자이면
	      String msg=inputField.getText();
	      if(msg.length()==0)return;
	      try{  
	        writer.println("[MESAAGE]"+msg);
	        inputField.setText("");
	      }catch(Exception ie){}
    }
	    else if(source==dmBtn){             // DM 버튼이면
		      String msg=dmtf.getText();
		      if(msg.length()==0)return;

		      try{  
		    	writer.println("[DM]/" + selectedId + "/" + msg);
		    	chatArea.append("[DM] "+ id + "-> "+selectedId + " : " + msg + "\n");
		        inputField.setText("");
		      }catch(Exception ie){}
	    }
	 	    else if(source==watingBtn){         // 입장하기 버튼이면
	      try{
	 	        if(Integer.parseInt(roomNumField.getText())<1){
	          popUpLabel.setText("방번호가 잘못되었습니다. 1이상");
	          return;
	        }
	          writer.println("[ROOM]"+Integer.parseInt(roomNumField.getText()));
	          chatArea.setText("");
	        }catch(Exception ie){
	          popUpLabel.setText("다시 입력해주세요.");
	        }
	    }
	 
	    else if(source==lobbyBtn){           // 대기실로 버튼이면
	      try{
	        WatingRoom();
	        startBtn.setEnabled(false);
	        stopBtn.setEnabled(false);
	      }catch(Exception e){}
	    }
	 
	    else if(source==startBtn){         
	      try{
	        writer.println("[START]");
	        popUpLabel.setText("준비 완료");
	        startBtn.setEnabled(false);
	      }catch(Exception e){}
	    }
	 
	    else if(source==stopBtn){          
	      try{
	        writer.println("[DROPGAME]");
	        endGame("기권하였습니다.");
	      }catch(Exception e){}
	    }
	  }
	 
	  void WatingRoom(){                  
	    if(userName==null){
	      String name=nameField.getText().trim();
	      index_name = name;
	      userName=name;
	      writer.println("[NAME]"+userName);    
	      nameField.setText(userName);
	      nameField.setEditable(false);
	    }  
	    chatArea.setText("");
	    writer.println("[ROOM]0");
	    popUpLabel.setText("대기실에 입장하셨습니다.");
	    roomNumField.setText("0");
	    watingBtn.setEnabled(true);
	    lobbyBtn.setEnabled(false);
	  }
	 
	  public void run(){
	    String msg;                             
	    try{
	    while((msg=reader.readLine())!=null){	 
	        if(msg.startsWith("[STONE]")){     
	          String temp=msg.substring(7);
	          int x=Integer.parseInt(temp.substring(0,temp.indexOf(" ")));
	          int y=Integer.parseInt(temp.substring(temp.indexOf(" ")+1));
	          board.putOpponent(x, y);      
	          board.setEnable(true);         
	        }
	 
	        else if(msg.startsWith("[ROOM]")){    
	          if(!msg.equals("[ROOM]0")){          
	            watingBtn.setEnabled(false);
	            lobbyBtn.setEnabled(true);
	            popUpLabel.setText(msg.substring(6)+"번 방에 입장하셨습니다.");
	            chatArea.append(dao.infoUser(id,index_name)+ '\n');	            
	          }
	          else popUpLabel.setText("대기실에 입장하셨습니다.");	 
	          roomNum=Integer.parseInt(msg.substring(6));     
	          if(board.Playing()){                   
	            board.stopGame();                   
	          }
	        }
	 
	        else if(msg.startsWith("[FULL]")){       
	          popUpLabel.setText("대기방에 비워있는 자리가 없습니다. ");
	        }
	 
	       else if(msg.startsWith("[PLAYERS]")){      
	          nameList(msg.substring(9));
	        }
	 
	        else if(msg.startsWith("[ENTER]")){        
	          UserList.add(msg.substring(7));
	          playersInfo();
	          chatArea.append("["+ msg.substring(7)+"]님이 입장하였습니다.\n" + '\n');
	        }
	        else if(msg.startsWith("[EXIT]")){          
	          UserList.remove(msg.substring(6));            
	          playersInfo();                        
	          chatArea.append("["+msg.substring(6)+
	                                         "]님이 다른 방으로 입장하였습니다.\n");
	          if(roomNum!=0)
	            endGame("상대가 나갔습니다.");
	        }
	 
	        else if(msg.startsWith("[DISCONNECT]")){     
	          UserList.remove(msg.substring(12));
	          playersInfo();
	          chatArea.append("["+msg.substring(12)+"]님이 접속을 끊었습니다.\n");
	          if(roomNum!=0)
	            endGame("상대가 나갔습니다.");
	        }
	 
	        else if(msg.startsWith("[COLOR]")){          
	          String color=msg.substring(7);
	          board.startGame(color);                     
	           dao.updateTotal(id);							
	          if(color.equals("BLACK"))
	            popUpLabel.setText("흑돌을 잡았습니다.");
	          else
	            popUpLabel.setText("백돌을 잡았습니다.");
	          stopBtn.setEnabled(true);                 
	        }
	 
	        else if(msg.startsWith("[DROPGAME]")) {     
	          endGame("상대가 기권하였습니다.");
	          chatArea.append(dao.infoUser(id,index_name + '\n'));
	        }	 
	        else if(msg.startsWith("[WIN]")) {             
	          endGame("이겼습니다.");
	         dao.updateVictory(id);							
	         chatArea.append(dao.infoUser(id,index_name+ '\n'));
	         }	 
	        else if(msg.startsWith("[LOSE]")) {         
	          endGame("졌습니다.");
	          dao.updateDefeat(id); 						
	          chatArea.append(dao.infoUser(id,index_name+ '\n'));
	        }	      
	        else chatArea.append(msg+"\n");
	    }
	    }catch(IOException ie){
	      chatArea.append(ie+"\n");
	    }
	    chatArea.append("접속이 끊겼습니다.");
	  }
	    void endGame(String msg){               
	    popUpLabel.setText(msg);
	    startBtn.setEnabled(false);
	    stopBtn.setEnabled(false);
	 
	    try{
	    	Thread.sleep(2000); 
	    	}catch(Exception e){
	    	
	    }   	 
	    if(board.Playing())board.stopGame();
	    if(UserList.getItemCount()==2)startBtn.setEnabled(true);
	  }
	 
	    void playersInfo(){                
	    int count=UserList.getItemCount();
	    if(roomNum==0)
	      userLabel.setText("대기실: "+count+"명");
	    else userLabel.setText(roomNum+" 번 방: "+count+"명");	   
	    if(count==2 && roomNum!=0)
	      startBtn.setEnabled(true);
	    else startBtn.setEnabled(false);
	  }
	 
	
	    void nameList(String msg){
	    UserList.removeAll();
	    StringTokenizer st=new StringTokenizer(msg, "\t");
	    while(st.hasMoreElements())
	      UserList.add(st.nextToken());
	    playersInfo();
	  }
	 
	    void connect(){                    
	    try{
	      chatArea.append("서버에 연결을 요청합니다.\n");
	      socket=new Socket(ip, 8086);
	      chatArea.append("---연결 성공--.\n");
	      chatArea.append("이름을 입력하고 대기실로 입장하세요.\n");
	      reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	      writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
	      new Thread(this).start();
	      board.setWriter(writer);
	    }catch(Exception e){
	      chatArea.append(e+"\n\n연결 실패..\n");  
	    }
	  }
	
	  class OmokBoard extends Canvas{              
		  ReentrantLock lock = new ReentrantLock(); 
		  public static final int BLACK=1, WHITE=-1;     
		    int[][] arr;                            
		    int size;              
		    int grid;                          
		    String info="게임 중지";            
		    int color=BLACK;                 
		    boolean enable=false;
		    boolean playing=false;      
		    Image img;                 
		    PrintWriter writer;            
		    Graphics gboard, drawStone;     

		  OmokBoard(int s, int c){        
		    this.size=s; this.grid=c;
		    arr=new int[size+2][];         
		    for(int i=0;i<arr.length;i++)
		      arr[i]=new int[size+2];	 
		    setBackground(Color.yellow);        
		    setSize(size*(grid+1)+size, size*(grid+1)+size);   
		    
		    addMouseListener(new MouseAdapter(){
		      public void mousePressed(MouseEvent e){     
		        if(!enable)return;           
		        int x=(int)Math.round(e.getX()/(double)grid);
		        int y=(int)Math.round(e.getY()/(double)grid);
		        if(x==0 || y==0 || x==size+1 || y==size+1)return;		       
		        if(arr[x][y]==BLACK || arr[x][y]==WHITE)return;		       
		        writer.println("[STONE]"+x+" "+y);	 
		        arr[x][y]=color;
		 
		       
		        if(check(new Point(x, y), color)){
		          info="이겼습니다.";
		          writer.println("[WIN]");
		        }
		 
		        else info="상대가 두기를 기다립니다.";
		        repaint();                             
		        enable=false;
		      }
		    });
		  }


		  public void startGame(String col){     
		    playing=true;
		    if(col.equals("BLACK")){             
		      enable=true; color=BLACK;
		      info="게임 시작... 두세요.";
		    }   
		    else{                              
		      enable=false; color=WHITE;
		      info="게임 시작... 기다리세요.";
		    }
		  }
		  public void stopGame(){             
		    reset();                              
		    writer.println("[STOPGAME]");       
		    enable=false;
		    playing=false;
		  }
		  public void putOpponent(int x, int y){      
		    arr[x][y]=-color;
		    info="상대가 두었습니다. 두세요.";
		    repaint();
		  }
		  public void setEnable(boolean enable){
		    this.enable=enable;
		  }
		  public void setWriter(PrintWriter writer){
		    this.writer=writer;
		  }
		  public boolean Playing(){          
			    return playing;
			  }
		  public void paint(Graphics g){                 
		    if(drawStone==null){                             
		      img=createImage(getWidth(),getHeight());
		      drawStone=img.getGraphics();
		    }
		    drawBoard(g);  
		  }
		  public void reset(){                        
		    for(int i=0;i<arr.length;i++)
		      for(int j=0;j<arr[i].length;j++)
		        arr[i][j]=0;
		    info="게임 중지";
		    repaint();
		  }
		    void drawLine(){                     
		    drawStone.setColor(Color.black);
		    for(int i=1; i<=size;i++){
		      drawStone.drawLine(grid, i*grid, grid*size, i*grid);
		      drawStone.drawLine(i*grid, grid, i*grid , grid*size);
		    }
		  }
		    void drawStone(int x, int y, Color stoneColor, Color borderColor) {
		        Graphics2D g = (Graphics2D) this.drawStone;
		        g.setColor(stoneColor);
		        g.fillOval(x * grid - grid / 2, y * grid - grid / 2, grid, grid);
		        g.setColor(borderColor);
		        g.drawOval(x * grid - grid / 2, y * grid - grid / 2, grid, grid);
		    }

		    void drawStones() {
		        for (int x = 1; x <= size; x++) {
		            for (int y = 1; y <= size; y++) {
		                if (arr[x][y] == BLACK) {
		                    drawStone(x, y, Color.black, Color.white);
		                } else if (arr[x][y] == WHITE) {
		                    drawStone(x, y, Color.white, Color.black);
		                }
		            }
		        }
		    }

		    void drawBoard(Graphics g) {
				lock.lock();  
		        try {
		            drawStone.clearRect(0, 0, getWidth(), getHeight());
		            drawLine();
		            drawStones();
		            drawStone.setColor(Color.red);
		            drawStone.drawString(info, 20, 15);
		            g.drawImage(img, 0, 0, this);
		        } finally {
		            lock.unlock(); 
		        }
		    }
		    				
		    boolean check(Point editPanel, int col){	
		        int[][] directions = {
		            {1, 0}, {-1, 0}, 
		            {0, 1}, {0, -1}, 
		            {1, 1}, {-1, -1}, 
		            {1, -1}, {-1, 1} 
		        };

		        for (int[] d : directions) {
		            if (countStones(editPanel, d[0], d[1], col) >= 5) {
		                return true;
		            }
		        }
		        return false;
		    }

		    int countStones(Point point, int dx, int dy, int col) {
		        int count = 1; 
		        count += countDirection(point, dx, dy, col);
		        count += countDirection(point, -dx, -dy, col); 
		        return count;
		    }

		    int countDirection(Point start, int dx, int dy, int col) {
		        int count = 0;
		        int x = start.x;
		        int y = start.y;

		        while (true) {
		            x += dx;
		            y += dy;
		            if (x < 0 || y < 0 || x >= size || y >= size || arr[x][y] != col) {
		                break;
		            }
		            count++;
		        }
		        return count;
		    }

		}  
	class LoginPanel extends JPanel { 
	      JLabel useridLabel = new JLabel("아이디:");
	      JTextField useridField = new JTextField(20); 
	      JLabel passwordLabel = new JLabel("비밀번호:");
	      JPasswordField passwordField = new JPasswordField(20); 
	      JButton loginButton = new JButton("로그인");
	      JButton toggleButton = new JButton("회원가입으로 전환"); 
	    UserDAO dao = new UserDAO();
	    public LoginPanel(int x, int y) {
	        setLayout(new FlowLayout()); 
	        add(useridLabel);
	        add(useridField);
	        add(passwordLabel);
	        add(passwordField);
	        add(loginButton);
	        add(toggleButton); 
	        toggleButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent ae) {	                
	                goToSignup(); 
	            }
	        });

	        loginButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent ae) {	
	                String userid = useridField.getText();
	                String password = new String(passwordField.getPassword()); 
	                if(dao.loginUser(userid, password)) {
	                	setEnabled(false);
	                	id = useridField.getText();
	                	goToGame();
	                }else {
	                	 JOptionPane.showMessageDialog(null, "다시 입력해주세요.", "로그인 실패", JOptionPane.INFORMATION_MESSAGE);
	                }
	            }
	        }     
	   
	        		
	        		);

	        setBounds(x, y, 250, 100);
	    }
	}
	  
	class SignupPanel extends JPanel {
//	      JLabel signupLabel = new JLabel("회원가입");
	      JLabel usernameLabel = new JLabel("이름:");
	      JTextField usernameField = new JTextField(20); 
	      JLabel useridLabel = new JLabel("아이디:");
	      JTextField useridField = new JTextField(20); 
	      JLabel passwordLabel = new JLabel("비밀번호:");
	      JPasswordField passwordField = new JPasswordField(20); 
	      JButton signupButton = new JButton("회원가입");
	      JButton toggleButton = new JButton("로그인으로 전환"); 
	    UserDAO dao = new UserDAO();
	    public SignupPanel(int x, int y) {
	        setLayout(new FlowLayout()); 	        
//	        add(signupLabel);
	        add(usernameLabel);
	        add(usernameField);
	        add(useridLabel);
	        add(useridField);
	        add(passwordLabel);
	        add(passwordField);
	        add(signupButton);
	        add(toggleButton);
	     
	        signupButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent ae) {	     
	            	String name = usernameField.getText();
	                String userid = useridField.getText();
	                String password = new String(passwordField.getPassword()); // 패스워드 필드의 내용을 문자열로 변환
	               
	                if (dao.createUser(name,userid, password)) {	                    
	                    JOptionPane.showMessageDialog(null, "회원가입이 완료되었습니다.", "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
	                } else {	                 
	                    JOptionPane.showMessageDialog(null, "회원가입에 실패하였습니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
	                }
	            }
	        });
	       
	        toggleButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent ae) {	                 
	                goToLogin(); 
	            }
	        });
	        setBounds(x, y, 250, 150); 
	    }
	}
	
	
	  public static void main(String[] args){
	    OmokClient client=new OmokClient("오목 게임");
	    client.setSize(760,560);
	    client.setVisible(true);
	    client.connect();
	    client.enableLoginPanel(); 	    
  }
	 
	    void disableAllPanels() {		    
		  	loginPanel.setVisible(false);
		  	singPanel.setVisible(false);
		    board.setVisible(false);
		    nameField.setEnabled(false);
		    roomNumField.setEnabled(false);
		    watingBtn.setEnabled(false);
		    lobbyBtn.setEnabled(false);
		    startBtn.setEnabled(false);
		    stopBtn.setEnabled(false);
		    inputField.setEnabled(false);
		    UserList.setEnabled(false);
		}

		  void enableLoginPanel() {		   
			loginPanel.setVisible(true);
		   
		}
		  void disableLoginPanel() {		 
			loginPanel.setVisible(false);
		  
		}

		  void enableGamePanels() {		
		    board.setVisible(true);
		    nameField.setEnabled(true);
		    roomNumField.setEnabled(true);
		    watingBtn.setEnabled(true);
		    lobbyBtn.setEnabled(true);
		    startBtn.setEnabled(true);
		    stopBtn.setEnabled(true);
		    inputField.setEnabled(true);
		    UserList.setEnabled(true);
		}

		  void disableGamePanels() {	
		    board.setVisible(false);
		    nameField.setEnabled(false);
		    roomNumField.setEnabled(false);
		    watingBtn.setEnabled(false);
		    lobbyBtn.setEnabled(false);
		    startBtn.setEnabled(false);
		    stopBtn.setEnabled(false);
		    inputField.setEnabled(false);
		    UserList.setEnabled(false);
		}

	
		public void goToGame() {
		    disableLoginPanel();
		    enableGamePanels();
		}

	
		  void goToLogin() {
		    disableGamePanels();
		    singPanel.setVisible(false);
		    enableLoginPanel();
		}

		  void goToSignup() {
			disableLoginPanel();
			singPanel.setVisible(true);
		}


	}