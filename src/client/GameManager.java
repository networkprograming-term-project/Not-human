package client;

import javax.swing.JFrame;

public class GameManager {

	public static void main(String[] args) {
		 JFrame frame = new JFrame("Not-Human Game"); // 게임 윈도우를 생성
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 윈도우 닫힐 때 프로그램 종료
	        InGameView gamePanel = new InGameView(); // 게임 패널 객체 생성
	        frame.add(gamePanel); // 프레임에 게임 패널 추가
	        frame.pack(); 
	        frame.setLocationRelativeTo(null); // 윈도우를 화면 가운데에 위치
	        frame.setVisible(true); // 윈도우를 보이게 설정
	        gamePanel.requestFocusInWindow(); // 키 입력을 받기 위해 게임 패널에 포커스 요청
	}

}
