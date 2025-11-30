package server;

import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;

public class Killer {
	private String playerName;
	
	private int posX;
    private int posY;
    private int direction = 0; // 0:IDLE, 1:LEFT, 2:RIGHT, 3:UP, 4:DOWN
    
    // 술래는 도망자보다 조금 더 빠르게 설정
    private final static int speed = 2; 
    private int width = new ImageIcon("src/img/seeker_left2.png").getImage().getWidth(null);
    private int height = new ImageIcon("src/img/seeker_down2.png").getImage().getHeight(null);
    
    private boolean keyLeft, keyRight, keyUp, keyDown;
    
    public Killer(String playerName) {
        this.playerName = playerName;
        this.posX = (int)(Math.random() * (GameManager.MAX_W - width));
        this.posY = (int)(Math.random() * (GameManager.MAX_H - height));
    }
    
    public void setKeyInput(int keyCode, boolean isPressed) {
        switch(keyCode) {
            case KeyEvent.VK_LEFT:  keyLeft = isPressed; break;
            case KeyEvent.VK_RIGHT: keyRight = isPressed; break;
            case KeyEvent.VK_UP:    keyUp = isPressed; break;
            case KeyEvent.VK_DOWN:  keyDown = isPressed; break;
        }
    }
    
    public void update() {
        int dx = 0;
        int dy = 0;
        boolean isMoving = false;

        if (keyLeft) { 
            dx -= speed; 
            direction = 1; 
            isMoving = true; 
        }
        if (keyRight) { 
            dx += speed; 
            direction = 2; 
            isMoving = true; 
        }
        if (keyUp) { 
            dy -= speed; 
            direction = 3; 
            isMoving = true; 
        }
        if (keyDown) { 
            dy += speed; 
            direction = 4; 
            isMoving = true; 
        }

        if (!isMoving) direction = 0;

        posX += dx;
        posY += dy;

        // 맵 경계 처리
        posX = Math.max(0, Math.min(posX, GameManager.MAX_W - width));
        posY = Math.max(0, Math.min(posY, GameManager.MAX_H - height));
    }
    
 // Getter
    public String getName() { return playerName; }
    public void setName(String name) { this.playerName=name; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getDirection() { return direction; }
    
    // [추가] 충돌 감지를 위해 크기 반환
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
