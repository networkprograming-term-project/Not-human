package client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class NetworkClient extends JFrame {
    private JPanel contentPane;
    private JTextField txtInput;
    private String UserName;
    private JButton btnSend;
    private JButton btnReady; // 준비 버튼
    JScrollPane scrollPane;
    private JTextArea textArea;
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
    private Socket socket; // 연결소켓
    private InputStream is;
    private OutputStream os;   
    private DataInputStream dis;
    private DataOutputStream dos;
    private JLabel lblUserName;
    
    // 서버로 부터 받아올 게임 상태 메시지 (모든 정보를 담고있음);
    private volatile String serverGameStateMSG;
    
    // 현재 실행 중인 게임 화면 참조
    private InGameViewRunner gameRunner = null;
    
	/**
	 * Create the frame.
	 */
	public NetworkClient(String username, String ip_addr, String port_no) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 392, 502);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 352, 340);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		txtInput = new JTextField();
		txtInput.setBounds(91, 365, 185, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		btnSend = new JButton("Send");
		btnSend.setBounds(288, 364, 76, 40);
		contentPane.add(btnSend);
		
		// Ready버튼 모두가 준비되면 자동 시작
		btnReady = new JButton("Ready?");
		btnReady.setBounds(35, 415, 300, 40);
		btnReady.setBackground(Color.ORANGE); // 버튼 색상 추가
		contentPane.add(btnReady);
		
		lblUserName = new JLabel("Name");
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(12, 364, 67, 40);
		contentPane.add(lblUserName);
		setVisible(true);
	
		AppendText("User " + username + " connecting " + ip_addr + " " + port_no + "\n");
		UserName = username;
		lblUserName.setText(username + ">");
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			AppendText("서버에 연결되었습니다.\n");
			
			// 스트림 초기화
			is = socket.getInputStream();
			os = socket.getOutputStream();
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			
			// 1. 입장 로그인 메시지 전송
			SendMessage("/login " + UserName);
			
			// 2. 수신 스레드 시작
			ListenNetwork net = new ListenNetwork();
			net.start();
			
			// 3. 이벤트 리스너 등록
			Myaction myaction = new Myaction();
			txtInput.addActionListener(myaction);
			btnSend.addActionListener(myaction);
			
			// 준비 버튼 이벤트 리스너 추가
			ReadyAction readyaction = new ReadyAction();
			btnReady.addActionListener(readyaction);
			
			// 준비 버튼 이벤트 리스너 추가
			txtInput.requestFocus();
		} catch(NumberFormatException |IOException e){
			e.printStackTrace();
			AppendText("connect errror");
		}	
    }

    // Server Message를 수신해서 화면에 표시
    class ListenNetwork extends Thread {
    	@Override
    	public void run() {
    		while(true) {
    			try {
    				String msg = dis.readUTF();
    				
    				// 리셋 명령 처리
                    if(msg.equals("/RESET")) {
                        // 게임 화면 닫기
                        if(gameRunner != null) {
                            gameRunner.dispose();
                            gameRunner = null;
                        }
                        
                        // 대기실 화면 복구
                        setVisible(true);
                        
                        // 버튼 초기화
                        btnReady.setText("Ready?");
                        btnReady.setBackground(Color.ORANGE);
                        btnReady.setEnabled(true);
                        
                        AppendText("[시스템] 대기실로 복귀했습니다.\n");
                        continue; // 다음 루프로
                    }
    				
    				// 게임 실행 명령어 수신시 게임뷰 실행
                    if(isVisible()) {
        				if(msg.equals("\n\n[GM] --> 모두 준비되어 게임을 시작하겠습니다. <--")) {
        					// [수정] runner 객체 저장
        					gameRunner = new InGameViewRunner(NetworkClient.this);
        					setVisible(false);
        				}
        				AppendText(msg);
    				} 
                    // 인게임 로직 (게임 시작 후)
                    else { 
    					serverGameStateMSG = msg;
    				}
    			} catch(IOException e) {
    				AppendText("dis.read() error");
                    try {
                        dos.close();
                        dis.close();
                        socket.close();
                        break;
                    } catch(Exception ee) {
                    	break;
                    }
    			} 
    		}
    	}
    }

	// 메시지를 입력 후 Send 버튼 또는  keyboard enter key를 치면 서버로(다른 사용자에게) 전송
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == txtInput || e.getSource() == btnSend) {
				String msg = "";
				msg += String.format("[%s] %s\n", UserName, txtInput.getText());
				SendMessage(msg);
				txtInput.setText("");
				txtInput.requestFocus();
				
				if(msg.contains("/exit")) {
					System.exit(0);
				}
			}
		}
	}
	
	// 준비버튼 이벤트리스너
	class ReadyAction implements ActionListener 
	{
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == btnReady) {
				JButton btn = (JButton)e.getSource();
				if(btn.getText().equals("Ready?"))
					btn.setText("Im ready!");
					btn.setBackground(Color.GREEN);
					btn.setEnabled(false);
					String msg = "";
					// READY 명령어 전송
					msg = String.format("/READY [%s]", UserName);
					SendMessage(msg);
			}
		}
	}

    // 화면에 출력
    public void AppendText(String msg) {
        textArea.append(msg);
        textArea.setCaretPosition(textArea.getText().length());
    }


    // Server에게 network로 전송
    public void SendMessage(String msg) {
       try {
    	   dos.writeUTF(msg);   
       } catch(IOException e) {
    	   AppendText("dos.write() error");
    	   try {
        	   dos.close();
        	   dis.close();
        	   socket.close();
           } catch(IOException e1) {
        	   e1.printStackTrace();
           }
       }
    }
    
    public String getServerGameStateMSG() {	return this.serverGameStateMSG; }
    public String getUserName() { return this.UserName; }
}
