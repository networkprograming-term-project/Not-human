/*---------------게임 플레이어 클래스--------------------*/

package client;

import java.awt.*;
import javax.swing.*;

public class GameAi{
	private Image playerImage;
	// AI 포지션
	private int posX;
	private int posY;
	
	// AI 목표 이동 포지션
	private int movingGoalX;
	private int movingGoalY;
    // 목표 방향, 이동 할 거리
	private int goalDirection;
	private int goalDistance;
	//스피드
	private int SPEED = 1;
    
	// 투명 여부
	private boolean isVisible;
	
	// 움직이는 중
	private boolean isMoving;
	

	public GameAi() {
		playerImage = new ImageIcon("src/img/player.png").getImage(); // 플레이어 이미지 로드
		
		// 랜덤 스폰
		posX = (int)(Math.random() * InGameView.MAX_W) + 10;
		posY = (int)(Math.random() * InGameView.MAX_H) + 10;
		
		// 움직임 멈춘 상태
		isMoving = false;
	}
	
	// 목표 이동 지점 설정
	public void movingGoalSetting() {
		
		// 이동 중이면 목표 재설정 금지
        if(!isMoving) {
        	// AI이동 목표 지점 재설정
        	goalDirection =  (int)(Math.random()*9);
        	goalDistance = ((int)(Math.random()*400) + 50);
        	
        	
        	switch (goalDirection){
        	case 0:	// 상
        		movingGoalX = posX;
        		movingGoalY = posY - goalDistance;
        		break;
        	case 1:	// 상우
        		movingGoalX = posX + goalDistance;
        		movingGoalY = posY - goalDistance;
        		break;
        	case 2: // 우
        		movingGoalX = posX + goalDistance;
        		movingGoalY = posY;
        		break;
        	case 3: // 우하
        		movingGoalX = posX + goalDistance;
        		movingGoalY = posY + goalDistance;
        		break;
        	case 4: // 하
        		movingGoalX = posX;
        		movingGoalY = posY + goalDistance;
        		break;
        	case 5: // 하좌
        		movingGoalX = posX - goalDistance;
        		movingGoalY = posY + goalDistance;
        		break;
        	case 6: // 좌
        		movingGoalX = posX - goalDistance;
        		movingGoalY = posY;
        		break;
        	case 7: // 상좌
        		movingGoalX = posX - goalDistance;
        		movingGoalY = posY - goalDistance;
        		break;
        	case 8: // 대기 상태
        		movingGoalX = posX;
        		movingGoalY = posY;
        		goalDistance = ((int)(Math.random()*200) + 50);
        		break;
        	}
        	
        	// 창의 경계를 넘지 않도록 위치를 조정
        	movingGoalX = Math.max(movingGoalX, 0);
        	movingGoalX = Math.min(movingGoalX, InGameView.MAX_W - playerImage.getWidth(null));
            
        	movingGoalY = Math.max(movingGoalY, 0);
        	movingGoalY = Math.min(movingGoalY, InGameView.MAX_H - playerImage.getHeight(null));
        	
        	// 목표 설정 후 이동 허용
    	    isMoving = true;
        }
	}
	
	// 목표 지점으로 이동
	public void movingOneStap() {
		// 목표지점 달성하지 못했다면 이동
		if((movingGoalX != posX || movingGoalY != posY) && goalDirection != 8) {
			switch (goalDirection){
        	case 0:	// 상
        		posY -= SPEED;
        		break;
        	case 1:	// 상우
        		posX += SPEED;
        		posY -= SPEED;
        		break;
        	case 2: // 우
        		posX += SPEED;
        		break;
        	case 3: // 우하
        		posX += SPEED;
        		posY += SPEED;
        		break;
        	case 4: // 하
        		posY += SPEED;
        		break;
        	case 5: // 하좌
        		posX -= SPEED;
        		posY += SPEED;
        		break;
        	case 6: // 좌
        		posX -= SPEED;
        		break;
        	case 7: // 상좌
        		posX -= SPEED;
        		posY -= SPEED;
        		break;
        	}
			
			// 창의 경계를 넘지 않도록 위치를 조정
			posX = Math.max(posX, 0);
			posX = Math.min(posX, InGameView.MAX_W - playerImage.getWidth(null));
            
			posY = Math.max(posY, 0);
			posY = Math.min(posY, InGameView.MAX_H - playerImage.getHeight(null));
			
			return;
		}
		else if(Math.abs(goalDistance) > 0 && goalDirection == 8){ // 디렉션 8인 경우 (대기 상태
			goalDistance = Math.abs(goalDistance) - 1; // 대기 시간 1 감소
			return;
		}
		
		// 목표지점 달성했다면 멈춤
		isMoving = false;
	}

	//setter, getter ----------------------//
	public Image getImg() {
		return this.playerImage;
	}
		
	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	
	public boolean isMoving() {
		return isMoving;
	}


	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	public int getMovingGoalX() {
		return this.movingGoalX;
	}
	
	public int getMovingGoalY() {
		return this.movingGoalY;
	}
	
	public void setPosX(int posX) {
		this.posX = posX;
	}
	
	public void setPosY(int posY) {
		this.posY = posY;
	}
	
	public int getPosX() {
		return this.posX;
	}
	
	public int getPosY() {
		return this.posY;
	}
}