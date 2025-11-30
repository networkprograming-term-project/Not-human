package server;

import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;

public class Player {
	
	// 플레이어 고유 식별 이름
	private String playerName;
	private int posX;
    private int posY;
    private int direction=0; // 0:IDLE, 1:LEFT, 2:RIGHT, 3:UP, 4:DOWN
    
    private final static int speed = 1;
    private int width = new ImageIcon("src/img/player_left2.png").getImage().getWidth(null);
    private int height = new ImageIcon("src/img/player_down2.png").getImage().getHeight(null);
    
 // 키 입력 상태 저장 (서버가 물리 계산을 하기 위해)
    public boolean keyLeft, keyRight, keyUp, keyDown;
    
	public Player(String playerName) {
		this.playerName = playerName;
		
		this.posX = (int)(Math.random() * (GameManager.MAX_W - width));
	    this.posY = (int)(Math.random() * (GameManager.MAX_H - height));
	}
	
	// 클라이언트의 키 입력을 받아 상태 저장
    public void setKeyInput(int keyCode, boolean isPressed) {
        switch(keyCode) {
            case KeyEvent.VK_LEFT:  keyLeft = isPressed; break;
            case KeyEvent.VK_RIGHT: keyRight = isPressed; break;
            case KeyEvent.VK_UP:    keyUp = isPressed; break;
            case KeyEvent.VK_DOWN:  keyDown = isPressed; break;
        }
    }
	
    // 매 프레임마다 호출되어 실제 좌표를 이동시킴
    public void update() {
        int dx = 0;
        int dy = 0;
        
        // 키 입력에 따른 이동량 계산
        if (keyLeft) dx -= speed;
        if (keyRight) dx += speed;
        if (keyUp) dy -= speed;
        if (keyDown) dy += speed;

        boolean isMoving = (dx != 0 || dy != 0);

        if (isMoving) {
            // 대각선 이동 시 좌우 애니메이션 우선 적용
            if (dx < 0) {
                direction = 1; // LEFT
            } else if (dx > 0) {
                direction = 2; // RIGHT
            } else if (dy < 0) {
                direction = 3; // UP
            } else if (dy > 0) {
                direction = 4; // DOWN
            }
        } else {
            // 움직이지 않으면 IDLE
            direction = 0;
        }

        // 좌표 적용
        posX += dx;
        posY += dy;
        // 맵 경계 처리 (화면 밖으로 나가지 않게)
        posX = Math.max(0, Math.min(posX, GameManager.MAX_W - width));
        posY = Math.max(0, Math.min(posY, GameManager.MAX_H - height));
    }
    
	public String getName() { return playerName; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getDirection() { return direction; }
    
    public void setDirection(int dir) { this.direction = dir; }
}
