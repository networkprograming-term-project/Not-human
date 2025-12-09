package client;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ClientExe extends JFrame {

	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField txtIpAddress;
	private JTextField txtPortNumber;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientExe frame = new ClientExe();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientExe() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// 1. 배경 이미지 로드
		ImageIcon icon = new ImageIcon("src/img/title.png");
		Image img = icon.getImage();
		int imgWidth = icon.getIconWidth();
		int imgHeight = icon.getIconHeight();

		// 2. 배경을 그리는 contentPane 생성
		contentPane = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
			}
		};
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null); // 절대 좌표 사용
		
		// 창 크기를 이미지 크기에 맞춤
		contentPane.setPreferredSize(new Dimension(imgWidth, imgHeight));
		setContentPane(contentPane);
		pack();
		setLocationRelativeTo(null); // 화면 정중앙에 창 띄우기
		
		// ---------------------------------------------------------
		// 3. 중앙 하단에 배치할 "입력 박스(패널)" 생성
		// ---------------------------------------------------------
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(null); // 박스 내부도 절대 좌표 사용
		
		// 박스 디자인 설정 (반투명 흰색 배경, 검은 테두리)
		inputPanel.setBackground(new Color(255, 255, 255, 150)); // Alpha 150 = 반투명
		inputPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		
		// 박스 크기 및 위치 계산
		int boxWidth = 320;
		int boxHeight = 250;
		int boxX = (imgWidth - boxWidth) / 2; // 가로 중앙 정렬
		int boxY = imgHeight - boxHeight - 50; // 바닥에서 50px 위로 띄움
		
		inputPanel.setBounds(boxX, boxY, boxWidth, boxHeight);
		contentPane.add(inputPanel); // 메인 화면에 박스 추가

		// ---------------------------------------------------------
		// 4. 컴포넌트들을 박스(inputPanel) 내부에 추가
		//    (좌표는 이제 inputPanel의 왼쪽 위(0,0)가 기준이 됩니다)
		// ---------------------------------------------------------
		
		// [Row 1] User Name
		JLabel lblUserName = new JLabel("User Name");
		lblUserName.setBounds(20, 20, 100, 30);
		lblUserName.setFont(new Font("Malgun Gothic", Font.BOLD, 14)); // 폰트 크기 조정
		inputPanel.add(lblUserName);
		
		txtUserName = new JTextField();
		txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
		txtUserName.setBounds(130, 20, 150, 30);
		inputPanel.add(txtUserName);
		txtUserName.setColumns(10);
		
		// [Row 2] IP Address
		JLabel lblIpAddress = new JLabel("IP Address");
		lblIpAddress.setBounds(20, 70, 100, 30);
		lblIpAddress.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
		inputPanel.add(lblIpAddress);
		
		txtIpAddress = new JTextField();
		txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
		txtIpAddress.setText("127.0.0.1");
		txtIpAddress.setBounds(130, 70, 150, 30);
		inputPanel.add(txtIpAddress);
		
		// [Row 3] Port Number
		JLabel lblPortNumber = new JLabel("Port Number");
		lblPortNumber.setBounds(20, 120, 100, 30);
		lblPortNumber.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
		inputPanel.add(lblPortNumber);
		
		txtPortNumber = new JTextField();
		txtPortNumber.setText("30000");
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setBounds(130, 120, 150, 30);
		inputPanel.add(txtPortNumber);
		
		// [Row 4] Connect Button
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(20, 180, 260, 40); // 박스 너비에 맞춰 꽉 차게
		btnConnect.setFont(new Font("Malgun Gothic", Font.BOLD, 16));
		btnConnect.setBackground(new Color(50, 100, 200)); // 버튼 색상 (파란 계열)
		btnConnect.setForeground(Color.WHITE); // 글자 색상 (흰색)
		inputPanel.add(btnConnect);
		
		// 이벤트 리스너 연결
		Myaction action = new Myaction();
		btnConnect.addActionListener(action);
		txtUserName.addActionListener(action);
		txtIpAddress.addActionListener(action);
		txtPortNumber.addActionListener(action);
	}
	
	class Myaction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = txtUserName.getText().trim();
			String host = txtIpAddress.getText().trim();
			String port = txtPortNumber.getText().trim();
			NetworkClient networkClient = new NetworkClient(name, host, port);
			setVisible(false);
		}
	}
}