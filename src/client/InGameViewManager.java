/*---------------게임 화면 클래스--------------------*/

package client;

import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.*;
import javax.swing.Timer;
import java.util.*;


//-----------------------------메인 클래스----------------------------------------
public class InGameViewManager extends JPanel implements KeyListener, ActionListener{
	private Image backgroundImage; // 배경 이미지
	public static final int MAX_W = 1300; // 맵 수편
	public static final int MAX_H = 800; // 맵 수직
	
	private Timer timer; // 게임 업데이트를 위한 타이머
	private boolean[] keys; // 키 입력 상태를 추적하는 불리언 배열
	
	// 애니메이션 속도 조절용 변수
	private int animationCounter = 0;
	private static final int ANIMATION_FRAME_DELAY = 20;
	
    //-----------------------게임 내 객체들---------------------------------------//
	private PlayerRender playerRender; // 도망자
  	private KillerRender killerRender; // 킬러
  	private Vector<PlayerRender> playerRenderVec = new Vector<>(); // 도망자 player를 관리하는 벡터
  	private Vector<GameAiRender> gameAiRenderVec = new Vector<>(); // Ai 클래스, Ai 관리하는 벡터
    
	//---------------데이터 송수신 관련-----------------//
  	NetworkClient networkClient; // 서버와 송수신할 함수를 가지고있는 객체
	
	
	public InGameViewManager(NetworkClient networkClient) {
		// 서버와 통신할 객체 등록
		this.networkClient = networkClient;
		
		backgroundImage = new ImageIcon("src/img/map.png").getImage(); // 배경 이미지 로드
		keys = new boolean[256]; // 키 입력 상태를 저장할 충분한 크기의 배열을 초기화
		
        // 도망자 생성
		playerRender = new PlayerRender();
		
		// 킬러 렌더 객체 초기화
        killerRender = new KillerRender();
		
		// GameAi 생성
		for(int i=0; i<10; i++) {
			gameAiRenderVec.add(new GameAiRender(i));
		}
		
		addKeyListener(this); // 키 리스너 추가
		setFocusable(true); // 키 입력을 받기 위해 포커스 가능하도록 설정
	    setPreferredSize(new Dimension(MAX_W, MAX_H)); // 패널의 선호 사이즈 설정
	    
	    // 타이머를 15ms 간격으로 설정, 두번째 인자는 ActionListener를 구현한 현재 객체
		timer = new Timer(15, this); 
	    timer.start(); // 타이머 시작
	}
	
	// ActionListener 인터페이스를 구현한 메소드, 타이머 이벤트가 발생할 때마다 호출
    public void actionPerformed(ActionEvent e) {
    	//15ms 마다 게임의 전체 상황을 받아와 변화를 감지
    	String serverMsg = networkClient.getServerGameStateMSG();
    	
    	// 아직 서버에서 아무 것도 못 받은 경우 방어
        if (serverMsg == null || serverMsg.isEmpty()) {
            return;
        }
        
    	System.out.println(serverMsg);
    	
    	//  서버 메시지가 게임 데이터 포맷인지 확인
        // 정상 포맷: AI@Player@Killer@GameState (최소 3개의 @가 있어 4조각이 나야 함)
        if (!serverMsg.contains("@")) {
            // 채팅 메시지나 시스템 알림인 경우 무시 (또는 로그 출력)
            // System.out.println("System Msg: " + serverMsg);
            return;
        }
        
    	// 메시지 형태: AI정보... @ Player정보... @ Killer정보 @ GameState
    	String[] arr = serverMsg.split("@"); 
    	
    	// [예외 해결] 배열 크기 체크 (데이터가 짤려서 왔을 경우 방어)
    	if (arr.length < 4) {
            return;
        }
    	
    	String gameAisMsg = arr[0];
    	String playersMsg = arr[1];
    	String killerMsg = arr[2];
    	String gameStateMsg = arr[3];
    	
    	//--------- gameAI 정보 파싱------------------------//
    	// 예: 100,200,imgState/ ...
    	if(!gameAisMsg.equals("") && !gameAisMsg.equals("a")) {
            String [] gameAiMsg = gameAisMsg.split("/");
            // AI 개수가 맞는지 확인 후 루프
            int len = Math.min(gameAiMsg.length, gameAiRenderVec.size());
            
            for(int i=0; i<len; i++) {
                if(gameAiMsg[i].isEmpty()) continue;
                
                String [] commend = gameAiMsg[i].split(","); 
                if(commend.length < 3) continue;

                GameAiRender gar = gameAiRenderVec.get(i); 
                
                gar.setPosX(Integer.parseInt(commend[0])); 
                gar.setPosY(Integer.parseInt(commend[1])); 
                // 서버에서 받은 방향 코드를 AI에게 적용 (이미지 변경용)
                gar.setDirection(Integer.parseInt(commend[2]));
            }
        }
    	
    	//--------- player 정보 파싱------------------------//
        // 예: UserA,100,200,1/UserB,300,400,2/
    	if(!playersMsg.equals("") && !playersMsg.equals("a") && !playersMsg.startsWith("dummy")) {
            String[] pDatas = playersMsg.split("/");
            
            for(String pData : pDatas) {
                String[] info = pData.split(",");
                if(info.length < 4) continue;

                String pName = info[0];
                int x = Integer.parseInt(info[1]);
                int y = Integer.parseInt(info[2]);
                int dir = Integer.parseInt(info[3]);
 
                // 해당 이름의 플레이어가 벡터에 있는지 확인
                boolean found = false;
                for(PlayerRender pr : playerRenderVec) {
                    if(pr.getName().equals(pName)) {
                        pr.setPosX(x);
                        pr.setPosY(y);
                        pr.setDirection(dir);
                        found = true;
                        break;
                    }
                }
                
                // 없으면 새로 생성 (새로 접속한 유저)
                if(!found) {
                    PlayerRender newPr = new PlayerRender(pName);
                    newPr.setPosX(x);
                    newPr.setPosY(y);
                    newPr.setDirection(dir);
                    playerRenderVec.add(newPr);
                }
            }
            
            // 접속 끊긴 유저 제거 로직
            // playerRenderVec.removeIf(pr -> !currentFrameNames.contains(pr.getName()));
        }
    	
    	//--------- killer 정보 파싱------------------------//
    	// 예: KillerName,100,200,1
        // 서버에서 데이터가 없으면 "a" 등을 보냄
    	if (!killerMsg.equals("a") && !killerMsg.isEmpty()) {
            String[] kInfo = killerMsg.split(",");
            if (kInfo.length >= 4) {
            	killerRender.setName(kInfo[0]); 
                killerRender.setPosX(Integer.parseInt(kInfo[1]));
                killerRender.setPosY(Integer.parseInt(kInfo[2]));
                killerRender.setDirection(Integer.parseInt(kInfo[3]));
                
                // 킬러가 보이도록 설정
                killerRender.setVisible(true);
            }
        }
    	
    	//--------- gameState 정보 파싱------------------------//
    	// 예: "RUNNING" or "GAMEOVER" or "TIME:170"
    	if (!gameStateMsg.equals("a") && !gameStateMsg.isEmpty()) {
            // 게임 상태에 따라 UI 처리 (예: 게임 종료 시 팝업 등)
            // 여기서는 단순히 콘솔 출력이나 타이머 변수 업데이트 등을 수행할 수 있음
            // System.out.println("Current State: " + gameStateMsg);
            
            if(gameStateMsg.startsWith("GAMEOVER")) {
                // 게임 종료 처리 로직
                // timer.stop();
                // JOptionPane.showMessageDialog(this, "Game Over!");
            }
        }
    	
    	//--------- [5] 애니메이션 프레임 업데이트 (제안하신 부분) ------------------------//
        animationCounter++;
        if (animationCounter >= ANIMATION_FRAME_DELAY) {
            animationCounter = 0; // 카운터 리셋
            
            // 1. 모든 AI의 다음 프레임 호출
            for(GameAiRender ai : gameAiRenderVec) {
                ai.nextAnimationFrame();
            }
            
            // 2. 모든 플레이어의 다음 프레임 호출
            for(PlayerRender pr : playerRenderVec) {
                pr.nextAnimationFrame();
            }
            
            // 3. 킬러의 다음 프레임 호출
            if(killerRender != null) {
                killerRender.nextAnimationFrame();
            }
        }
    	repaint(); // 패널을 다시 그림 (paintComponent 호출)
    }
	
	
	// 화면 그리기
	public void paintComponent(Graphics g) {
        super.paintComponent(g); // 상위 클래스의 paintComponent 호출
        // 배경 이미지 그리기.
        g.drawImage(backgroundImage, 0, 0, MAX_W, MAX_H, this);
        
        // 플레이어 이미지 그리기.
        for (PlayerRender pr : playerRenderVec) {
             g.drawImage(pr.getImg(), pr.getPosX(), pr.getPosY(), this);
             if(pr.getName().equals(networkClient.getUserName())) {
            	 // 본인 디스플레이에만 이름 표시
            	 String name = "▼ " + pr.getName(); // 이름 앞에 화살표 추가
                 
                 // 위치 계산 (캐릭터 머리 위 중앙)
                 int textX = pr.getPosX() - 5; 
                 int textY = pr.getPosY() - 10;

                 // [테두리 효과] 검은색으로 4방향에 먼저 그림 (그림자 역할)
                 g.setColor(Color.BLACK);
                 g.drawString(name, textX - 1, textY);
                 g.drawString(name, textX + 1, textY);
                 g.drawString(name, textX, textY - 1);
                 g.drawString(name, textX, textY + 1);
                 
                 // [메인 글씨] 밝은 노란색으로 그 위에 덮어씀 (가시성 확보)
                 g.setColor(Color.YELLOW); 
                 g.drawString(name, textX, textY);
             } 
        }
        
        // GameAI 이미지 그리기.
        for (GameAiRender gameAiRender : gameAiRenderVec) {
            g.drawImage(gameAiRender.getImg(), gameAiRender.getPosX(), gameAiRender.getPosY(), this);
        }
        
        // Killer 이미지 그리기
        if (killerRender != null && killerRender.isVisible()) {
            g.drawImage(killerRender.getImg(), killerRender.getPosX(), killerRender.getPosY(), this);
            g.setColor(Color.RED);
            
            if(killerRender.getName().equals(networkClient.getUserName())) {
            	// 본인 디스플레이에만 이름 표시
            	String name = "▼ " + killerRender.getName(); // 이름 앞에 화살표 추가
                
                // 위치 계산 (캐릭터 머리 위 중앙)
                int textX = killerRender.getPosX() - 5; 
                int textY = killerRender.getPosY() - 10;

                // [테두리 효과] 검은색으로 4방향에 먼저 그림 (그림자 역할)
                g.setColor(Color.BLACK);
                g.drawString(name, textX - 1, textY);
                g.drawString(name, textX + 1, textY);
                g.drawString(name, textX, textY - 1);
                g.drawString(name, textX, textY + 1);
                
                // [메인 글씨] 밝은 노란색으로 그 위에 덮어씀 (가시성 확보)
                g.setColor(Color.RED); 
                g.drawString(name, textX, textY);
            } 
        }
	}
	
    // KeyListener 인터페이스를 구현한 메소드, 키가 눌렸을 때 호출
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true; // 해당 키가 눌렸다면 배열에 true를 설정
        
        // 내 키 입력을 서버로 전송 (화면 움직임 X, 전송 O)
        networkClient.SendMessage("/KEY PRESS " + e.getKeyCode());
    }

    // KeyListener 인터페이스를 구현한 메소드, 키에서 손을 떼었을 때 호출
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false; // 해당 키에서 손을 떼었다면 배열에 false를 설정
        
        networkClient.SendMessage("/KEY RELEASE " + e.getKeyCode());
    }

    // KeyListener 인터페이스의 메소드, 키 타이핑 이벤트를 처리, 여기서는 구현x
    public void keyTyped(KeyEvent e) { }

}