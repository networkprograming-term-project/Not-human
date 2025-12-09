package server;

public class Smoke {
    private int x, y;
    private int currentSize;
    private int lifeTime; // 수명 
    
    // 설정
    private static final int MAX_LIFE = 500; 
    private static final int MAX_SIZE = 700; // 최대 크기
    private static final int GROWTH_RATE = 6; // 커지는 속도

    public Smoke(int x, int y) {
        this.x = x;
        this.y = y;
        this.currentSize = 50; // 초기 크기
        this.lifeTime = MAX_LIFE;
    }

    // 매 프레임 호출: 크기는 키우고 수명은 줄임
    public void update() {
        if (lifeTime > 0) {
            lifeTime--;
            if (currentSize < MAX_SIZE) {
                currentSize += GROWTH_RATE;
            }
        }
    }

    public boolean isExpired() {
        return lifeTime <= 0;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return currentSize; }
}