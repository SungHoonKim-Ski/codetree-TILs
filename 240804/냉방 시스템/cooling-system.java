import java.util.Scanner;

public class Main {
    public static final int OFFICE = 1;
    public static final int DIR_NUM = 4;
    public static final int MAX_M = 400;
    public static final int MAX_N = 20;
    
    public static int n, m, k;
    public static int[][] grid = new int[MAX_N][MAX_N];
    
    // 각 위치의 시원함의 정도를 관리합니다.
    // 처음에는 전부 0입니다.
    public static int[][] coolness = new int[MAX_N][MAX_N];
    
    // 시원함을 mix할 때
    // 동시에 일어나는 처리를
    // 편하게 하기 위해 사용될 배열입니다.
    public static int[][] temp = new int[MAX_N][MAX_N];
    
    // dx, dy 순서를 상좌우하로 설정합니다.
    // 입력으로 주어지는 숫자에 맞추며,
    // 4에서 현재 방향을 뺏을 때, 반대 방향이 나오도록 설정한 것입니다.
    public static int[] dx = new int[]{-1,  0, 0, 1};
    public static int[] dy = new int[]{ 0, -1, 1, 0};
    
    // 현재 위치 (x, y)에서 해당 방향으로
    // 이동한다 했을 때 벽이 있는지를 나타냅니다.
    public static boolean[][][] block = new boolean[MAX_N][MAX_N][DIR_NUM];
    
    // 시원함을 전파할 시
    // 한 에어컨에 대해
    // 겹쳐서 퍼지는 경우를 막기 위해
    // visited 배열을 사용합니다.
    public static boolean[][] visited = new boolean[MAX_N][MAX_N];
    
    // 현재까지 흐른 시간(분)을 나타냅니다.
    public static int elapsedTime;
    
    // (x, y)가 격자 내에 있는지를 판단합니다.
    public static boolean inRange(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }
    
    // (dx, dy) 값으로부터
    // moveDir값을 추출해냅니다.
    public static int revDir(int xDiff, int yDiff) {
        for(int i = 0; i < 4; i++)
            if(dx[i] == xDiff && dy[i] == yDiff)
                return i;
    
        return -1;
    }
    
    // (x, y)위치에서 moveDir 방향으로
    // power 만큼의 시원함을 만들어줍니다.
    // 이는 그 다음 칸에게도 영향을 끼칩니다.
    public static void spread(int x, int y, int moveDir, int power) {
        // power가 0이 되면 전파를 멈춥니다.
        if(power == 0)
            return;
    
        // 방문 체크를 하고, 해당 위치에 power를 더해줍니다.
        visited[x][y] = true;
        coolness[x][y] += power;
    
        // Case 1. 직진하여 전파되는 경우입니다.
        int nx = x + dx[moveDir];
        int ny = y + dy[moveDir];
        if(inRange(nx, ny) && !visited[nx][ny] && !block[x][y][moveDir])
            spread(nx, ny, moveDir, power - 1);
        
        // Case 2. 대각선 방향으로 전파되는 경우입니다.
        if(dx[moveDir] == 0) {
            int[] nxs = new int[]{x + 1, x - 1};
            for(int i = 0; i < 2; i++) {
                nx = nxs[i];
                ny = y + dy[moveDir];
                // 꺾여 들어가는 곳에 전부 벽이 없는 경우에만 전파가 가능합니다.
                if(inRange(nx, ny) && !visited[nx][ny] && !block[x][y][revDir(nx - x, 0)] && !block[nx][y][moveDir])
                    spread(nx, ny, moveDir, power - 1);
            }
        }
        else {
            int[] nys = new int[]{y + 1, y - 1};
            for(int i = 0; i < 2; i++) {
                nx = x + dx[moveDir];
                ny = nys[i];
                // 꺾여 들어가는 곳에 전부 벽이 없는 경우에만 전파가 가능합니다.
                if(inRange(nx, ny) && !visited[nx][ny] && !block[x][y][revDir(0, ny - y)] && !block[x][ny][moveDir])
                    spread(nx, ny, moveDir, power - 1);
            }
        }
    }
    
    public static void clearVisited() {
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                visited[i][j] = false;
    }
    
    // 에어컨에서 시원함을 발산합니다.
    public static void blow() {
        // 각 에어컨에 대해
        // 시원함을 발산합니다.
        for(int x = 0; x < n; x++)
            for(int y = 0; y < n; y++)
                // 에어컨에 대해
                // 해당 방향으로 시원함을
                // 만들어줍니다.
                if(grid[x][y] >= 2) {
                    int moveDir = (grid[x][y] <= 3) ? (3 - grid[x][y]) 
                                                     : (grid[x][y] - 2);
                    int nx = x + dx[moveDir];
                    int ny = y + dy[moveDir];
    
                    // 전파 전에 visited 값을 초기화해줍니다.
                    clearVisited();
                    // 세기 5에서 시작하여 계속 전파합니다.
                    spread(nx, ny, moveDir, 5);
                }
    }
    
    // (x, y) 위치는
    // mix된 이후 시원함이
    // 얼마가 되는지를 계산해줍니다.
    public static int getMixedCoolness(int x, int y) {
        int remainingC = coolness[x][y];
        for(int i = 0; i < DIR_NUM; i++) {
            int nx = x + dx[i], ny = y + dy[i];
            // 사이에 벽이 존재하지 않는 경우에만 mix가 일어납니다.
            if(inRange(nx, ny) && !block[x][y][i]) {
                // 현재의 시원함이 더 크다면, 그 차이를 4로 나눈 값 만큼 빠져나갑니다.
                if(coolness[x][y] > coolness[nx][ny])
                    remainingC -= (coolness[x][y] - coolness[nx][ny]) / 4;
                // 그렇지 않다면, 반대로 그 차이를 4로 나눈 값만큼 받아오게 됩니다.
                else
                    remainingC += (coolness[nx][ny] - coolness[x][y]) / 4;
            }
        }
        return remainingC;
    }
    
    // 시원함이 mix됩니다.
    public static void mix() {
        // temp 배열을 초기화 해줍니다.
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                temp[i][j] = 0;
    
        // 각 칸마다 시원함이 mix된 이후의 결과를 받아옵니다.
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++) 
                temp[i][j] = getMixedCoolness(i, j);
        
        // temp 값을 coolness 배열에 다시 옮겨줍니다.
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                coolness[i][j] = temp[i][j];
    }
    
    // 외벽에 해당하는 칸인지를 판단합니다.
    public static boolean isOuterWall(int x, int y) {
        return x == 0 || x == n - 1 || y == 0 || y == n - 1;
    }
    
    // 외벽과 인접한 칸 중
    // 시원함이 있는 곳에 대해서만
    // 1만큼 시원함을 하락시킵니다.
    public static void drop() {
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                if(isOuterWall(i, j) && coolness[i][j] > 0)
                    coolness[i][j]--;
    }
    
    // 시원함이 생기는 과정을 반복합니다.
    public static void simulate() {
        // Step1. 에어컨에서 시원함을 발산합니다.
        blow();
    
        // Step2. 시원함이 mix됩니다.
        mix();
    
        // Step3. 외벽과 인접한 칸의 시원함이 떨어집니다.
        drop();
    
        // 시간이 1분씩 증가합니다.
        elapsedTime++;
    }
    
    // 종료해야 할 순간인지를 판단합니다.
    // 모든 사무실의 시원함의 정도가 k 이상이거나,
    // 흐른 시간이 100분을 넘게 되는지를 살펴봅니다.
    public static boolean end() {
        // 100분이 넘게 되면 종료해야 합니다.
        if(elapsedTime > 100)
            return true;
        
        // 사무실 중에
        // 시원함의 정도가 k 미만인 곳이
        // 단 하나라도 있으면, 아직 끝내면 안됩니다.
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                if(grid[i][j] == OFFICE && 
                   coolness[i][j] < k)
                   return false;
    
        // 모두 k 이상이므로, 종료해야 합니다.
        return true;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // 입력:
        n = sc.nextInt();
        m = sc.nextInt();
        k = sc.nextInt();

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                grid[i][j] = sc.nextInt();

        for(int i = 0; i < m; i++) {
            int bx = sc.nextInt();
            int by = sc.nextInt();
            int bdir = sc.nextInt();
            bx--; by--;
            // 현재 위치 (bx, by)에서
            // bdir 방향으로 나아가려고 했을 때
            // 벽이 있음을 표시해줍니다.
            block[bx][by][bdir] = true;

            int nx = bx + dx[bdir];
            int ny = by + dy[bdir];
            // 격자를 벗어나지 않는 칸과 벽을 사이에 두고 있다면,
            // 해당 칸에서 반대 방향(3-bdir)으로 진입하려고 할 때도
            // 벽이 있음을 표시해줍니다.
            if(inRange(nx, ny))
                block[nx][ny][3 - bdir] = true;
        }

        // 종료조건이 만족되기 전까지
        // 계속 시뮬레이션을 반복합니다.
        while(!end())
            simulate();

        // 출력:
        if(elapsedTime <= 100)
            System.out.print(elapsedTime);
        else
            System.out.print(-1);
    }
}