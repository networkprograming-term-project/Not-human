package client;

import java.awt.*;
import javax.swing.*;

public class KillerRender {
    // 킬러의 현재 상태(방향)를 나타내는 열거형
    public enum Direction {
        IDLE, LEFT, RIGHT, UP, DOWN, PUNCH
    }
    
    // 식별용 이름
    private String name;

    // 모든 방향의 이미지를 배열로 저장
    private Image img_idle;
    private Image[] img_left = new Image[2];
    private Image[] img_right = new Image[2];
    private Image[] img_up = new Image[2];
    private Image[] img_down = new Image[2];
    // 공격 이미지 변수
    private Image img_punch;
    
    // 현재 상태 및 프레임 변수
    private Direction currentDirection = Direction.IDLE;
    private int currentFrame = 0; // 0 또는 1 (두 개의 이미지를 교차)
    
    // 이미지 크기 변수
    private int width;
    private int height;
    
    // 킬러 포지션
    private int posX;
    private int posY;
    
    // 생존 여부 (킬러도 게임 오버 로직이 있다면 필요)
    private boolean isAlive;
    // 투명 여부
    private boolean isVisible;
    

    // 생성자: 이름을 받아서 초기화
    public KillerRender(String name) {
        this.name = name;
        initImages();
        
        // 초기 위치는 일단 0,0 (나중에 서버에서 받아옴)
        posX = 0;
        posY = 0;
    }
    
    // 기본 생성자
    public KillerRender() {
        this.name = "";
        initImages();
    }
    
    private void initImages() {
        try {
            // [변경됨] player_ -> seeker_ 로 이미지 경로 변경
            img_idle = new ImageIcon("src/img/seeker_idle.png").getImage();

            img_left[0] = new ImageIcon("src/img/seeker_left1.png").getImage();
            img_left[1] = new ImageIcon("src/img/seeker_left2.png").getImage();

            img_right[0] = new ImageIcon("src/img/seeker_right1.png").getImage();
            img_right[1] = new ImageIcon("src/img/seeker_right2.png").getImage();

            img_up[0] = new ImageIcon("src/img/seeker_up1.png").getImage();
            img_up[1] = new ImageIcon("src/img/seeker_up2.png").getImage();

            img_down[0] = new ImageIcon("src/img/seeker_down1.png").getImage();
            img_down[1] = new ImageIcon("src/img/seeker_down2.png").getImage();

            // 펀치 이미지 로드
            img_punch = new ImageIcon("src/img/seeker_punch.png").getImage();
            
            // 이미지 크기 저장
            width = img_left[1].getWidth(null);
            height = img_down[1].getHeight(null);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Killer 이미지 로딩 실패: " + e.getMessage());
            // 이미지가 없어도 실행은 되도록 종료는 하지 않음 (필요시 System.exit(1) 추가)
        }
        isAlive = true;
    }
    
    // 서버에서 받은 정수형 방향 코드를 처리하는 메서드
    // 0:IDLE, 1:LEFT, 2:RIGHT, 3:UP, 4:DOWN
    public void setDirection(int dirCode) {
        switch(dirCode) {
            case 1: setDirection(Direction.LEFT); break;
            case 2: setDirection(Direction.RIGHT); break;
            case 3: setDirection(Direction.UP); break;
            case 4: setDirection(Direction.DOWN); break;
            case 5: setDirection(Direction.PUNCH); break; //5번은 PUNCH
            default: setDirection(Direction.IDLE); break;
        }
    }
    
    // 킬러의 방향 상태 변경
    public void setDirection(Direction dir) {
        // 방향이 바뀔 때만 프레임을 리셋
        if (this.currentDirection != dir) {
            this.currentDirection = dir;
            this.currentFrame = 0;
        }
    }
    
    // 애니메이션 프레임 교차 변경
    public void nextAnimationFrame() {
        // IDLE 상태일 때는 애니메이션 불필요
        if (currentDirection == Direction.IDLE) {
            currentFrame = 0;
            return;
        }
        currentFrame = (currentFrame + 1) % 2; // 0과 1을 반복
    }
    
    // 현재 상태(방향)와 현재 프레임에 맞는 이미지를 반환
    public Image getImg() {
        switch (currentDirection) {
            case PUNCH: return img_punch; // [추가]
            case LEFT:  return img_left[currentFrame];
            case RIGHT: return img_right[currentFrame];
            case UP:    return img_up[currentFrame];
            case DOWN:  return img_down[currentFrame];
            case IDLE:
            default:    return img_idle;
        }
    }
    
    //setter, getter ----------------------
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setName(String name) { this.name=name; }
    public String getName() { return this.name; }
    
    public boolean isAlive() { return isAlive; }
    public void setAlive(boolean isAlive) { this.isAlive = isAlive; }
    
    public boolean isVisible() { return isVisible; }
    public void setVisible(boolean isVisible) { this.isVisible = isVisible; }
    
    public void setPosX(int posX) { this.posX = posX; }
    public void setPosY(int posY) { this.posY = posY; }
    
    public int getPosX() { return this.posX; }
    public int getPosY() { return this.posY; }
}