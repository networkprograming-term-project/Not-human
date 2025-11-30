/*---------------게임 화면 클래스--------------------*/

package server;

import java.awt.*;
import java.awt.event.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.*;
import javax.swing.Timer;


import java.util.*;


//-----------------------------메인 클래스----------------------------------------
// 실제 보이진 않지만 게임이 실행되는 영역
public class GameManager extends JPanel implements ActionListener{
	
	//-----------------------게임 내 객체들---------------------------------------//
	private Killer killer; // 킬러
	private Vector<Player> playerVec = new Vector<>(); // 도망자 player를 관리하는 벡터
	private Vector<GameAi> gameAiVec = new Vector<>(); // gameAi를 관리하는 벡터

	//-----------------------게임 상태 관련 변수---------------------------------------//
	private Timer timer; // 게임 업데이트를 위한 타이머
	public static final int MAX_W = 1300; // 맵 수편
	public static final int MAX_H = 800; // 맵 수직
	int gameTime = 180;
	
	//------------------------클라이언트 정보-----------------------------------------//
	Vector<String> playerNames;
	private ServerManager serverManager; // 서버 매니저 참조 변수
	
	public GameManager(Vector<String> playerNames, ServerManager serverManager) {
		// 클라이언트를 식별할 수 있는 이름이 들어있는 벡터
		this.playerNames = playerNames;
		this.serverManager=serverManager;
		
		// 술래 선정
		if (!playerNames.isEmpty()) {
			int index = (int)(Math.random() * playerNames.size());
			String picked = playerNames.remove(index);
			killer = new Killer(picked);
		} else {
			// 테스트용 더미 킬러 (혼자 접속 시 에러 방지)
			killer = new Killer("DummyKiller");
		}
	    
		// 도망자 생성
		for(String playerName : playerNames) {
			playerVec.add(new Player(playerName));
		}
		
		// GameAi 생성
		for(int i=0; i<10; i++) {
			gameAiVec.add(new GameAi(i));
		}
		
		// 게임 루프 시작 (15ms)
		timer = new Timer(15, this); 
        timer.start(); // 타이머 시작
	}
	
	// 클라이언트의 키 입력을 처리하는 메서드
    public void handleInput(String playerName, String msg) {
        // msg 예시: "/KEY PRESS 37" (37=Left)
        String[] parts = msg.split(" ");
        String action = parts[1]; // PRESS or RELEASE
        int keyCode = Integer.parseInt(parts[2]);
        boolean isPressed = action.equals("PRESS");
        
        // 플레이어 입력 처리
     	for(Player p : playerVec) {
     		if(p.getName().equals(playerName)) {
     			p.setKeyInput(keyCode, isPressed); // Player 클래스에 해당 메서드 필요
     			return;
     		}
     	}
     	
     	// 킬러 입력 처리 (추가 필요)
     	if(killer.getName().equals(playerName)) {
     		killer.setKeyInput(keyCode, isPressed); // Killer 클래스에 해당 메서드 필요
     	}
    }
	
	// ActionListener 인터페이스를 구현한 메소드, 타이머 이벤트가 발생할 때마다 호출
    public void actionPerformed(ActionEvent e) {
    	//5ms 마다 플레임 업데이트
    	// 킬러 프레임 업데이트
    	updateKillerPosition(); // 킬러 프레임 업데이트
    	updatePlayerPosition(); // 플레이어 프레임 업데이트
        updateGameAiPosition(); // gameAI 프레임 업데이트
        
        // 계산된 현재 상태를 모든 클라이언트에게 전송 (Broadcast)
        if(serverManager != null) {
        	String stateMsg = getOneFrameStateMsg();
        	serverManager.broadcast(stateMsg);
        }
    }
	
    // Killer 위치 업데이트 메소드
    private void updateKillerPosition() {
    	killer.update(); // Killer 내부 이동 로직 호출
    }
    
    // 플레이어 위치 업데이트 (물리 연산)
    private void updatePlayerPosition() {
    	for(Player player: playerVec) {
    		player.update(); // Player 내부 이동 로직 호출
    	}
    }
    
    // GameAi 위치 업데이트 메소드
    private void updateGameAiPosition() {
    	for(GameAi gameAi : gameAiVec) {
            
    		// 목표지점 설정 안돼있으면 AI이동 목표 지점 설정
        	gameAi.movingGoalSetting();
            
            // 이동 상태가 허용 되었으면
            gameAi.movingOneStap();
    	}
    }

    
    
    // 전체 클라이언트로 브로드캐스트 되어야할 메시지
	public String getOneFrameStateMsg() {
		/*
		 * 본인이 어떤 역할인지
		 * 게임 AI (상태, 좌표, 방향, 이미지, )
		 * 플레이어 (uid, 상태, 좌표, 방향, 이미지, )
		 * 술래 (uid, 상태, 좌표, 방향, 이미지, 목숨, )
		 * 게임정보 (시간, 생존자, )
		 * 
		 * 
		 */
		
		
		StringBuilder sb = new StringBuilder();
		//--------- gameAI 정보 붙이기------------------------//
		for(GameAi ai : gameAiVec) {
            sb.append(String.format("%d,%d,%d/", ai.getPosX(), ai.getPosY(), ai.getImgState()));
        }
        sb.append("@");
		
		//--------- player 정보 붙이기------------------------//
        for(Player p : playerVec) {
            sb.append(String.format("%s,%d,%d,%d/", p.getName(), p.getPosX(), p.getPosY(), p.getDirection()));
        }
        sb.append("@");
		
		//--------- killer 정보 붙이기------------------------//
     	sb.append(String.format("%s,%d,%d,%d", killer.getName(), killer.getPosX(), killer.getPosY(), killer.getDirection()));
     	sb.append("@");
		
		//--------- gameState 정보 붙이기------------------------//
     	sb.append("RUNNING"); // 예시
		
		return sb.toString();
	}
}