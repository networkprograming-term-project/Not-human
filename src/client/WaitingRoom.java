package client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class WaitingRoom extends JFrame {
    private JPanel contentPane;
    private JTextField txtInput;
    private String UserName;
    private JButton btnSend;
    private JTextArea textArea;
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
    private Socket socket; // 연결소켓
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private JLabel lblUserName;

	/**
	 * Create the frame.
	 */
	public WaitingRoom(String username, String ip_addr, String port_no) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 392, 462);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
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
			AppendText("연결되었스빈다.");
			is = socket.getInputStream();
			os = socket.getOutputStream();
			dis = new DataInputStream(is);
			dos = new DataOutputStream(os);
			
			SendMessage("/login " + UserName);
			
			ListenNetwork net = new ListenNetwork();
			net.start();
			
			Myaction myaction = new Myaction();
			txtInput.addActionListener(myaction);
			btnSend.addActionListener(myaction);
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
    				AppendText(msg);
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
}
