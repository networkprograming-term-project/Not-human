/*---------------게임 화면 클래스--------------------*/

package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

import java.util.*;


//-----------------------------메인 클래스----------------------------------------
public class InGameView extends JPanel implements ActionListener, KeyListener{
	private Image backgroundImage; // 배경 이미지
	public static final int MAX_W = 1300; // 맵 수편
	public static final int MAX_H = 800; // 맵 수직
	private Timer timer; // 게임 업데이트를 위한 타이머
	
	public static final int SPEED = 1; // 플레이어의 이동 속도
	
    private boolean[] keys; // 키 입력 상태를 추적하는 불리언 배열
	
	private Player player; // 플레이어 (본인)
	
	// Ai 클래스, Ai 관리하는 벡터
	private Vector<GameAi> aiVec = new Vector<>();
	
	public InGameView() {
		backgroundImage = new ImageIcon("src/img/map.png").getImage(); // 배경 이미지 로드
		keys = new boolean[256]; // 키 입력 상태를 저장할 충분한 크기의 배열을 초기화
		
        timer = new Timer(5, this); // 타이머를 5ms 간격으로 설정, 두번째 인자는 ActionListener를 구현한 현재 객체
        timer.start(); // 타이머 시작
        
        // 본인 (플레이어) 생성
		player = new Player();
		
		// GameAi 생성
		for(int i=0; i<10; i++) {
			aiVec.add(new GameAi());
		}
		
		addKeyListener(this); // 키 리스너 추가
		setFocusable(true); // 키 입력을 받기 위해 포커스 가능하도록 설정
	    setPreferredSize(new Dimension(MAX_W, MAX_H)); // 패널의 선호 사이즈 설정
	}
	
	
	// 여기서 게임 전체 상황과 한 프레임을 그리면 될 듯
	public void paintComponent(Graphics g) {
        super.paintComponent(g); // 상위 클래스의 paintComponent 호출
        // 배경 이미지 그리기.
        g.drawImage(backgroundImage, 0, 0, MAX_W, MAX_H, this);
        
        // 플레이어 이미지 그리기.
        g.drawImage(player.getImg(), player.getPosX(), player.getPosY(), this);
        
        // GameAI 이미지 그리기.
        for (GameAi gameAi : aiVec) {
            g.drawImage(gameAi.getImg(), gameAi.getPosX(), gameAi.getPosY(), this);
        }
	}
	
	// ActionListener 인터페이스를 구현한 메소드, 타이머 이벤트가 발생할 때마다 호출
    public void actionPerformed(ActionEvent e) {
        updatePlayerPosition(); // 플레이어 위치를 업데이트
        updateGameAiPosition();
        repaint(); // 패널을 다시 그림 (paintComponent 호출)
    }

    
    // 플레이어의 위치 업데이트 메소드
    private void updatePlayerPosition() {
        int posX = player.getPosX();
        int posY = player.getPosY();
    	
    	if (keys[KeyEvent.VK_LEFT]) {
    		posX  -= SPEED; // 왼쪽 키가 눌리면 왼쪽으로 이동
        }
        if (keys[KeyEvent.VK_RIGHT]) {
        	posX  += SPEED; // 오른쪽 키가 눌리면 오른쪽으로 이동
        }
        if (keys[KeyEvent.VK_UP]) {
        	posY  -= SPEED; // 위쪽 키가 눌리면 위로 이동
        }
        if (keys[KeyEvent.VK_DOWN]) {
        	posY  += SPEED; // 아래쪽 키가 눌리면 아래로 이동
        }
        
        // 플레이어가 창의 경계를 넘지 않도록 위치를 조정
        posX = Math.max(posX, 0);
        posX = Math.min(posX, MAX_W - player.getImg().getWidth(null));
        
        posY = Math.max(posY, 0);
        posY = Math.min(posY, MAX_H - player.getImg().getHeight(null));
        
        
     // 플레이어 위치 조정
        player.setPosX(posX);
        player.setPosY(posY);
        
     //// 디버깅용 트레커
     //System.out.println("x: " + posX + " y: " + posY);
    }

    
    // GameAi 위치 업데이트 메소드
    private void updateGameAiPosition() {
    	for(GameAi gameAi : aiVec) {
            // AI이동 목표 지점 설정
        	gameAi.movingGoalSetting();
            
            // 이동 상태가 허용 되었으면
            gameAi.movingOneStap();
                         
//            int posX = gameAi.getPosX();
//            int posY = gameAi.getPosY();
//            
//            // 창의 경계를 넘지 않도록 위치를 조정
//            posX = Math.max(posX, 0);
//            posX = Math.min(posX, MAX_W - gameAi.getImg().getWidth(null));
//            
//            posY = Math.max(posY, 0);
//            posY = Math.min(posY, MAX_H - gameAi.getImg().getHeight(null));
//            
//            
//            // 플레이어 위치 조정
//            gameAi.setPosX(posX);
//            gameAi.setPosY(posY);
    	}
    }

    // KeyListener 인터페이스를 구현한 메소드, 키가 눌렸을 때 호출
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; // 해당 키가 눌렸다면 배열에 true를 설정
    }

    // KeyListener 인터페이스를 구현한 메소드, 키에서 손을 떼었을 때 호출
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false; // 해당 키에서 손을 떼었다면 배열에 false를 설정
    }

    // KeyListener 인터페이스의 메소드, 키 타이핑 이벤트를 처리, 여기서는 구현x
    public void keyTyped(KeyEvent e) { }

}