package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.*;

public class NetworkClient extends JFrame {
    private JPanel contentPane;
    
    // UI 컴포넌트
    private JPanel pnlUserList; // 상단 유저 목록 패널
    private JTextArea textArea; // 채팅창
    private JTextField txtInput; // 입력창
    private JButton btnSend; // 전송 버튼
    private JButton btnReady; // 준비 버튼
    
    // 통신 관련
    private Socket socket; 
    private DataInputStream dis;
    private DataOutputStream dos;
    
    private String UserName;
    private static final int PORT = 30000;
    
    // 게임 상태 및 화면
    private volatile String serverGameStateMSG;
    private InGameViewRunner gameRunner = null;
    
    // 유저 관리용 (이름 -> UI컴포넌트 매핑)
    private Map<String, JPanel> userCardMap = new ConcurrentHashMap<>();
    
    // 유저 카드 배경색 (파스텔 톤)
    private final Color[] cardColors = {
        new Color(255, 179, 186), // Red
        new Color(255, 223, 186), // Orange
        new Color(255, 255, 186), // Yellow
        new Color(186, 255, 201), // Green
        new Color(186, 225, 255), // Blue
        new Color(225, 186, 255), // Purple
        new Color(200, 200, 200)  // Grey
    };

	public NetworkClient(String username, String ip_addr, String port_no) {
		this.UserName = username;
		
		setTitle("Not Human - 대기실");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600); 
		
		// 메인 패널 설정 
		contentPane = new JPanel();
		contentPane.setBackground(new Color(240, 240, 240));
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		contentPane.setLayout(null); // 절대 좌표 사용

		// ---------------------------------------------------------
		// 1. 상단: 유저 목록 패널 (프로필 카드 영역)
		pnlUserList = new JPanel();
		pnlUserList.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15)); // 왼쪽 정렬, 간격 15
		pnlUserList.setBackground(new Color(255, 255, 255)); // 흰색 배경
		pnlUserList.setBorder(new LineBorder(new Color(200, 200, 200), 2)); // 테두리
		pnlUserList.setBounds(12, 10, 760, 150); // 상단 배치
		contentPane.add(pnlUserList);
		
		// ---------------------------------------------------------
		// 2. 하단 좌측: 채팅 영역
		// 채팅 내용 표시 (ScrollPane)
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 170, 560, 300);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);
		
		// 채팅 입력창
		txtInput = new JTextField();
		txtInput.setBounds(12, 480, 460, 50);
		txtInput.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		// 전송 버튼
		btnSend = new JButton("전송");
		btnSend.setBounds(480, 480, 90, 50);
		btnSend.setBackground(new Color(100, 149, 237)); // Cornflower Blue
		btnSend.setForeground(Color.WHITE);
		btnSend.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
		contentPane.add(btnSend);
		
		// ---------------------------------------------------------
		// 3. 하단 우측 게임 컨트롤 (Ready 버튼)
		JPanel pnlControl = new JPanel();
		pnlControl.setBounds(585, 170, 185, 360);
		pnlControl.setBackground(null); // 투명
		pnlControl.setLayout(null);
		contentPane.add(pnlControl);
		
		// Ready 버튼 (크게 배치)
		btnReady = new JButton("READY");
		btnReady.setBounds(0, 260, 185, 100); // 우측 하단 꽉 차게
		btnReady.setFont(new Font("Arial", Font.BOLD, 30));
		btnReady.setBackground(new Color(255, 99, 71)); // Tomato Red
		btnReady.setForeground(Color.WHITE);
		btnReady.setFocusPainted(false);
		pnlControl.add(btnReady);
		
		// 접속 정보 표시 Label
		JLabel lblInfo = new JLabel("접속자: " + username);
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblInfo.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
		lblInfo.setBounds(0, 10, 185, 30);
		pnlControl.add(lblInfo);

		setVisible(true);
		
		// ---------------------------------------------------------
		// 네트워크 연결 및 이벤트 등록
		connectToServer(ip_addr, port_no);
		
		// 이벤트 리스너
		Myaction myaction = new Myaction();
		txtInput.addActionListener(myaction);
		btnSend.addActionListener(myaction);
		
		ReadyAction readyaction = new ReadyAction();
		btnReady.addActionListener(readyaction);
    }
	
	// 서버 연결 메서드
	private void connectToServer(String ip, String port) {
		try {
			socket = new Socket(ip, Integer.parseInt(port));
			AppendText("[시스템] 서버에 연결되었습니다.\n");
			
			// 스트림 초기화
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			// 1. 입장 메시지 전송
			SendMessage("/login " + UserName);
			
			// 내 유저 카드 즉시 추가
			addUserCard(UserName); 
			
			// 2. 수신 스레드 시작
			ListenNetwork net = new ListenNetwork();
			net.start();
			
			txtInput.requestFocus();
			
		} catch(Exception e){
			e.printStackTrace();
			AppendText("[Error] Connection Failed.\n");
		}
	}
	
	// ---------------------------------------------------------
	//  UI 업데이트 로직 (유저 카드 추가,삭제)
	private void addUserCard(String name) {
		if (userCardMap.containsKey(name)) return;
		
		// 카드 패널 생성
		JPanel card = new JPanel();
		card.setPreferredSize(new Dimension(100, 120));
		card.setLayout(new BorderLayout());
		
		// 랜덤 배경색 지정
		int colorIndex = Math.abs(name.hashCode()) % cardColors.length;
		card.setBackground(cardColors[colorIndex]);
		card.setBorder(new LineBorder(Color.BLACK, 1));
		
		// 이름 라벨
		JLabel lblName = new JLabel(name, SwingConstants.CENTER);
		lblName.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
		lblName.setPreferredSize(new Dimension(100, 30));
		
		// 프로필 이미지 (기본 idle 이미지 사용)
		try {
            ImageIcon icon = new ImageIcon("src/img/player_idle.png");
            Image img = icon.getImage();
            // 카드 내부에 들어가기 적당한 크기로 조절 
            Image scaledImg = img.getScaledInstance(60, 80, Image.SCALE_SMOOTH);
            
            JLabel lblProfile = new JLabel(new ImageIcon(scaledImg));
            lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
            
            card.add(lblProfile, BorderLayout.CENTER);
        } catch (Exception e) {
            // 이미지 로드 실패 시 텍스트로 대체
            JLabel lblAlt = new JLabel("●", SwingConstants.CENTER);
            card.add(lblAlt, BorderLayout.CENTER);
        }
		
		card.add(lblName, BorderLayout.SOUTH);
		
		userCardMap.put(name, card);
		pnlUserList.add(card);
		pnlUserList.revalidate();
		pnlUserList.repaint();
	}
	
	private void removeUserCard(String name) {
		if (userCardMap.containsKey(name)) {
			JPanel card = userCardMap.get(name);
			pnlUserList.remove(card);
			userCardMap.remove(name);
			pnlUserList.revalidate();
			pnlUserList.repaint();
		}
	}
	

    // Server Message 수신 스레드
    class ListenNetwork extends Thread {
    	@Override
    	public void run() {
    		while(true) {
    			try {
    				String msg = dis.readUTF();
    				
    				// 1. 리셋 처리
                    if(msg.equals("/RESET")) {
                        if(gameRunner != null) {
                            gameRunner.dispose();
                            gameRunner = null;
                        }
                        setVisible(true);
                        btnReady.setText("READY");
                        btnReady.setBackground(new Color(255, 99, 71));
                        btnReady.setEnabled(true);
                        AppendText("[시스템] 대기실로 복귀했습니다.\n");
                        continue;
                    }
    				
                    // 2. 대기실 로직 
                    if(isVisible()) {
        				if(msg.equals("\n\n[GM] --> 모두 준비되어 게임을 시작하겠습니다. <--")) {
        					gameRunner = new InGameViewRunner(NetworkClient.this);
        					setVisible(false);
        				} else {
        					// 채팅 메시지 분석하여 유저 목록 갱신 (서버 프로토콜에 의존)
        					parseUserStatus(msg);
        					AppendText(msg);
        				}
    				} 
                    // 3. 인게임 로직 
                    else { 
    					serverGameStateMSG = msg;
    				}
    			} catch(IOException e) {
    				AppendText("[Error] Connection Lost.\n");
                    try {
                        if(dos!=null) dos.close();
                        if(dis!=null) dis.close();
                        if(socket!=null) socket.close();
                        break;
                    } catch(Exception ee) {}
    			} 
    		}
    	}
    }
    
    // 입장, 퇴장 메시지 파싱하여 유저 목록 갱신
    private void parseUserStatus(String msg) {
    	if (msg.contains("]님이 입장하였습니다.")) {
    		int start = msg.indexOf("[");
    		int end = msg.indexOf("]");
    		if (start != -1 && end != -1) {
    			String name = msg.substring(start + 1, end);
    			if (!name.equals(UserName)) { // 나 자신은 이미 추가함
    				addUserCard(name);
    			}
    		}
    	}
    	else if (msg.contains("]님이 퇴장하였습니다.")) {
    		int start = msg.indexOf("[");
    		int end = msg.indexOf("]");
    		if (start != -1 && end != -1) {
    			String name = msg.substring(start + 1, end);
    			removeUserCard(name);
    		}
    	}
    }

	// 전송 액션
	class Myaction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == txtInput || e.getSource() == btnSend) {
				String text = txtInput.getText().trim();
				if(text.length() == 0) return;
				
				String msg = String.format("[%s] %s\n", UserName, text);
				SendMessage(msg);
				txtInput.setText("");
				txtInput.requestFocus();
			}
		}
	}
	
	// Ready 버튼 액션
	class ReadyAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == btnReady) {
				// 버튼 토글 디자인
				btnReady.setText("WAITING...");
				btnReady.setBackground(Color.GRAY);
				btnReady.setEnabled(false);
				
				// 서버로 Ready 신호 전송
				String msg = String.format("/READY [%s]", UserName);
				SendMessage(msg);
			}
		}
	}

    public void AppendText(String msg) {
        textArea.append(msg);
        textArea.setCaretPosition(textArea.getText().length());
    }

    public void SendMessage(String msg) {
       try {
    	   dos.writeUTF(msg);   
       } catch(IOException e) {
    	   AppendText("[Error] Send Failed.\n");
       }
    }
    
    public String getServerGameStateMSG() {	return this.serverGameStateMSG; }
    public String getUserName() { return this.UserName; }
}