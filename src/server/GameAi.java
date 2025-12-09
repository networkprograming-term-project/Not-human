/*-----게임 AI 목표지점을 결정(AI의 뇌)--------- */
package server;


public class GameAi {
	// ai를 식별할 수 있는 번호
    private int gid;
	
	// ai 이미지 크기
	private int width = 36;
	private int height = 50;
	
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
	private final static int SPEED = 1;
	
	// 현재 이미지 상태 (방향: 0=IDLE, 1=LEFT, 2=RIGHT, 3=UP, 4=DOWN)
	private int imgState=0;
    
	// 투명 여부
	private boolean isVisible;
	
	// 움직이는 중
	private boolean isMoving;
	
	
	public GameAi(int gid) {
		//고유 식별 번호 등록
		this.gid = gid;
		
		// 랜덤 스폰
		posX = (int)(Math.random() * GameManager.MAX_W) - width;
		posY = (int)(Math.random() * GameManager.MAX_H) - height;
		
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
        	
        	// 목표 방향(goalDirection 0~8)에 따라 전송용 imgState(0~4) 설정
        	updateImgState();
        	
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
            movingGoalX = Math.min(movingGoalX, GameManager.MAX_W - width);

            movingGoalY = Math.max(movingGoalY, 0);
            movingGoalY = Math.min(movingGoalY, GameManager.MAX_H - height);
        	
        	// 목표 설정 후 이동 허용
    	    isMoving = true;
        }
	}
	
	// 8방향을 4방향 코드로 변환
	private void updateImgState() {
		if (goalDirection == 8) {
			imgState = 0; // IDLE
			return;
		}
			
		// 0:상, 1:상우, 2:우, 3:우하, 4:하, 5:하좌, 6:좌, 7:상좌
		switch (goalDirection) {
			case 1: case 2: case 3: 
				imgState = 2; // RIGHT
				break;
			case 5: case 6: case 7: 
				imgState = 1; // LEFT
				break;
			case 0: 
				imgState = 3; // UP
				break;
			case 4: 
				imgState = 4; // DOWN
				break;
			default:
				imgState = 0;
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
			
			// 경계 체크
            posX = Math.max(posX, 0);
            posX = Math.min(posX, GameManager.MAX_W - width);
            posY = Math.max(posY, 0);
            posY = Math.min(posY, GameManager.MAX_H - height);
			
			return;
		}
		else if(Math.abs(goalDistance) > 0 && goalDirection == 8){ // 디렉션 8인 경우 (대기 상태)
			goalDistance = Math.abs(goalDistance) - 1; // 대기 시간 1 감소
			return;
		}
		
		// 목표지점 달성했다면 멈춤
		isMoving = false;
		imgState = 0; // 멈추면 IDLE 상태로 변경
	}


	
	//setter, getter ----------------------
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean isVisible) { this.isVisible = isVisible; }
    public boolean isMoving() { return isMoving; }
    public void setMoving(boolean isMoving) { this.isMoving = isMoving; }
    public int getMovingGoalX() { return this.movingGoalX; }
    public int getMovingGoalY() { return this.movingGoalY; }
    public void setPosX(int posX) { this.posX = posX; }
    public void setPosY(int posY) { this.posY = posY; }
    public int getPosX() { return this.posX; }
    public int getPosY() { return this.posY; }
    
    public int getImgState() {return this.imgState;}
    public int getGID() {return this.gid;}
}
