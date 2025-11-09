package server;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ServerManager extends JFrame{
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;
    private ServerSocket socket; // 서버소켓
    private Socket client_socket; // accept() 에서 생성된 client 소켓, AcceptServer에서 지역변수로 선언해도 됩니다. 
    private Vector<UserService> UserVec = new Vector<>(); // 연결된 사용자를 저장할 벡터, ArrayList와 같이 동적 배열을 만들어주는 컬렉션 객체

    public static void main(String[] args) {   // 스윙 비주얼 디자이너를 이용해 GUI를 만들면 자동으로 생성되는 main 함수
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                	ServerManager frame = new ServerManager();      // JavaChatServer 클래스의 객체 생성
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ServerManager() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 338, 386);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 300, 244);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(12, 264, 87, 26);
        contentPane.add(lblNewLabel);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");
        txtPortNumber.setBounds(111, 264, 199, 26);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnServerStart = new JButton("Server Start");
        
        // 서버 스타트 버튼
        btnServerStart.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		try {
        			// 소켓 버퍼 생성
        			socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
        		}catch(NumberFormatException | IOException e1){
        			e1.printStackTrace();
        		}
        		AppendText("Chat Server Running");
        		btnServerStart.setText("Chat Server Running");
        		btnServerStart.setEnabled(false);
        		txtPortNumber.setEnabled(false);
        		
        		// 클라이언트 접속을 담당하는 스레드 실행
        		AcceptServer accept_server = new AcceptServer();
        		accept_server.start();
        	}
        });
        btnServerStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnServerStart);
    }

    
    // 새로운 참가자 accept() 하고 user thread를 새로 생성한다. 한번 만들어서 계속 사용하는 스레드
    class AcceptServer extends Thread {
        public void run() {
            while(true) {
            	try {
            		AppendText("Waiting clients ...");
            		client_socket = socket.accept();
            		AppendText("새로운 참가자 from " + client_socket);
            		
            		// 유저를 관리하는 스레드
            		UserService userService = new UserService(client_socket);
            		UserVec.add(userService);
            		AppendText("사용자 입장. 현재 참가자 수 " + UserVec.size());
            		
            		//유저 관리 스레드 실행
            		userService.start();
            	} catch(IOException e) {
            		AppendText("!!!! accept 에러 발생... !!!!");
            	}
            }
        }
    }

    //JtextArea에 문자열을 출력해 주는 기능을 수행하는 맴버 함수
    public void AppendText(String str) {
    	textArea.append(str + "\n");
    	int pos = textArea.getText().length();
    	textArea.setCaretPosition(pos);
    }

    
    // User 당 생성되는 Thread, 유저의 수만큼 스레스 생성
    // 이 UserService 스레드는 '소켓 객체'를 이용해서 실제 특정 유저와 메시지를 주고 받는 기능을 수행하는 스레드
    // 이 스레드 클래스의 run() 메소드 안의 dis.readUTF()에서 대기하다가 메시지가 들어오면 -> Write All로 전체 접속한 사용자한테 전송(단톡방) 
    class UserService extends Thread {
    	private InputStream is;
    	private OutputStream os;
    	private DataInputStream dis;
    	private DataOutputStream dos;
    	private Socket client_socket;
    	private Vector <UserService> user_vc;
    	private String UserName = "";
    	
    	
        public UserService(Socket client_socket) {
        	this.client_socket = client_socket;
        	user_vc = UserVec;
        	
        	try {
        		is = client_socket.getInputStream(); 
        		os = client_socket.getOutputStream(); 
        		dis = new DataInputStream(is);	// 입력 버퍼
        		dos = new DataOutputStream(os);	// 출력 버퍼
        		
        		
        		// 오브젝트를 주고 받게 되면 이부분 아마 수정 필요할 듯 
        		String dat = dis.readUTF();
        		String[] args = dat.split(" ");
        		UserName += args[1].trim();
        		
        		AppendText("새로운 참가자 " + UserName + "입장");
        		WriteOne("새로운 참가자 " + UserName + "입장\n");
        		String msg = "[" + UserName + "]님이 입장 하였습니다.\n"; 
        		WriteAll(msg);
        		
        	} catch(IOException e) {
        		AppendText("UserService Error");
        	}
        }


        public void logout() {
        	UserVec.removeElement(this);
        	String br_msg = "["+UserName+"]님이 퇴장 하였습니다.\n";
        	WriteAll(br_msg);
        	AppendText("사용자 퇴장. 현재 참가자 수 " + UserVec.size());
        }
        
        // 클라이언트로 메시지 전송(자신)
        public void WriteOne(String msg) {
        	try {
        		dos.writeUTF(msg);
        	}catch(IOException e) {
        		AppendText("dos.writeError");
        		try {
        			dos.close();
        			dis.close();
        			client_socket.close();
        		} catch(IOException e1) {
        			e1.printStackTrace();
        		}
        	}
        }

        
        //모든 다중 클라이언트에게 순차적으로 채팅 메시지 전달
        public void WriteAll(String str) { 
        	for(UserService user : UserVec){
        		user.WriteOne(str);
        	}
        }
        
        
        // 메시지 수신
        public void run() {
        	while(true) {
        		try {
        			String msg = dis.readUTF();
        			msg = msg.trim();
        			AppendText(msg);
        			
        			WriteAll(msg + "\n");
        		} catch(IOException e) {
        			AppendText("user.readUTF() ERROR");
        			try {
        				dis.close();
            			dos.close();
            			client_socket.close();
            			logout();
            			break;
        			} catch(IOException e1) {
        				break;
            		}
        			
        		}
        		
        	}
        }
    }
}
