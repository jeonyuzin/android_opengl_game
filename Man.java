package com.example.final_test;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Man extends Unit {

    // 병사의 타입 0~8까지 사용한다.
    int mType = 0;
    // 병사의 상태 방어, 이동, 공격, 전투중을 나타낸다.
    int mState = 0;
    // 현재의 에너지를 나타낸다.
    int mCurrentEnergy = 0;
    // 병사의 총 에너지를 나타낸다.
    int mEnergy = 100;
    // 병사의 방어력을 나타낸다.
    int mDefence = 0;
    // 병사의 공격력을 나타낸다.
    int mAttackPoint = 0;
    // 1:아군, 2:적군을 나타낸다.
    int mKind = 0;
    // 병사의 인덱스 값을 나타낸다. 배열로 표현할 예정이므로 자신의 배열 인덱스를 갖는다.
    int mIndex = 0;
    // 병사의 게임맵의 블럭 위치를 나타낸다.
    int mPosBlockRow = 0;
    int mPosBlockCol = 0;

    //체력 개체
    Panel mForceEnergy;

    //명령 문구 유지시간
    private int mStateWordCount=0;
    // MainGLRenderer를 참조한다.
    private MainGLRenderer mMainGLRenderer;

    // 길찾기 알고리즘 추가
    //현재 이동중인지
    private boolean mPathMove = false;
    //블럭간 움직임 여부
    private boolean mPathMoveStep = false;
    //최단거리 알고리즘에 의해  mPath배열의 현재 인덱스
    private int mPathStep = 0;
    //최단거리 알고리즘에 의해 mPath배열의 크기
    private int mPathMax = 0;
    //최단거리 알고리즘에 의해 관리되는 배열
    private int[][]mPath = new int[400][2];


    //명령상태
    private int mOrderState = ConstMgr.ORDER_DEFENCE;


    // 생성자
    public Man(int programImage, int programSolidColor, MainGLRenderer mainGLRenderer) {
        super(programImage, programSolidColor);
        mMainGLRenderer = mainGLRenderer;
        // mCount는 매번 루프를 돌때마다 호출된다.  병사의 움직임에 관여하는데 병사마다
        // 다른 시작점을 줌으로써 각기 다른 움직임을 갖도록 처리한다.
        mCount = (int) (Math.random() * 100);
    }

    // 병사의 속성을 설정한다.타입, 종류, 인덱스번호

    public void setProperty(int type, int kind, int index) {
        mType = type;
        mKind = kind;
        mIndex = index;
        setType(type);
    }

    // 병사의 타입을 설정한다.
    public void setType(int type) {
        mType = type;
        //검병일 경우
        if (mType == ConstMgr.TYPE_SWORD_MAN) {
            mEnergy = 500;
            mDefence = 30;
            mAttackPoint = 20;
        }
        // 창병일 경우
        else if (mType == ConstMgr.TYPE_SPEAR_MAN) {
            mEnergy = 500;
            mDefence = 10;
            mAttackPoint = 30;
        }
        // 궁병일 경우
        else if (mType == ConstMgr.TYPE_BOW_MAN) {
            mEnergy = 500;
            mDefence = 5;
            mAttackPoint = 8;
        }
        // 방패병일 경우
        else if (mType == ConstMgr.TYPE_SHIELD_MAN) {
            mEnergy = 500;
            mDefence = 70;
            mAttackPoint = 5;
        }
        // 장군일 경우
        else if (mType == ConstMgr.TYPE_GENERAL) {
            mEnergy = 2000;
            mDefence = 30;
            mAttackPoint = 30;
        }
        // 기지일 경우
        else if (mType == ConstMgr.TYPE_BASE) {
            mEnergy = 2000;
            mDefence = 30;
            mAttackPoint = 30;
        }
        // 장비/장합
        else if (mType == ConstMgr.TYPE_GENERAL1) {
            mEnergy = 2000;
            mDefence = 30;
            mAttackPoint = 30;
        }
        // 관우/하후돈
        else if (mType == ConstMgr.TYPE_GENERAL2) {
            mEnergy = 2000;
            mDefence = 30;  // 30% 차감
            mAttackPoint = 30;
        }
        // 유비/조조
        else if (mType == ConstMgr.TYPE_GENERAL3) {
            mEnergy = 2000;
            mDefence = 30;  // 30% 차감
            mAttackPoint = 30;
        }
        // 현재의 에너지를 가득 채우고 상태 초기화
        mCurrentEnergy = mEnergy;
    }

    // 해당좌표로 이동함
    public void moveTo(int x, int y) {
        mTargetX = x;
        mTargetY = y;
    }

    // 해당좌표로 이동함

    public void moveTo(float x, float y) {
        mTargetX = x;
        mTargetY = y;
    }

    // 가로방향 확대축소를 설정함
    public void setScaleX(float scaleX) {
        this.mScaleX = scaleX;
    }

    // 현재 맵의 위치를 반환함

    public int getBlockRow() {
        return mPosBlockRow;
    }

    public int getBlockCol() {
        return mPosBlockCol;
    }

    // 해당 블럭에 위치를 지정함

    public void setToBlock(int row, int col) {
        mPosBlockRow = row;
        mPosBlockCol = col;
        Map.setForceInfo(row,col,this.mKind,this.mIndex);//맵의 블럭 위치를 설정하는 곳에 맵의 위치에 유닛을 할당.
        mTargetX = Map.getPosX(row, col);
        mTargetY = Map.getPosY(row, col) + this.mHeight / 3;
        mPosX = mTargetX;
        mPosY = mTargetY;
    }

    // 해당 블럭이로 이동함.

    public void moveToBlock(int row, int col) {
        // 이동하려는 위치중 현재 블럭을 제외함.
        if (row == mPosBlockRow && col == mPosBlockCol) {
            return;
        }

        //먼저 위치를 이동시킨 후 현재 위치는 제거
        Map.setForceInfo(row,col,this.mKind,this.mIndex);
        Map.setForceInfo(mPosBlockRow,mPosBlockCol,0,0);
        mPosBlockRow = row;
        mPosBlockCol = col;
        mTargetX = Map.getPosX(row, col);
        mTargetY = Map.getPosY(row, col) + this.mHeight / 3;
    }

    // 병사의 생각을 관리함.
    int mCount = 0;
    // 길찾기 알고리즘에서 사용하는 블럭개수 관리

    int mPathCount = 0;

    // 병사가 생각하도록 만드는 함수
    public void think() {
        if (mIsActive == false) {
            return;
        }
        mCount++;
        // 변수형 범위를 넘어설 경우를 대비해 초기화
        if (mCount > 30000) {
            mCount = 0;
        }
        //탐색알고리즘의 취약이나 많이 지체될 경우 다시 초기화함.
        if(mPathCount>500){
            mPathStep=0;
            mPathMove=false;
            mPathMoveStep=false;
            mPathMax=0;
        }
        if (this.mPosX > this.mTargetX + 10) {
            this.mPosX = this.mPosX - 9;
            moveWord();//think마다 호출
        } else if (this.mPosX < this.mTargetX - 10) {
            this.mPosX = this.mPosX + 9;
            moveWord();
        }
        if (this.mPosY > this.mTargetY + 10) {
            this.mPosY = this.mPosY - 9;
            moveWord();
        } else if (this.mPosY < this.mTargetY - 10) {
            this.mPosY = this.mPosY + 9;
            moveWord();
        }

        if (mCount % 100 < 25) {
            this.mBitmapState = 0;
        } else if (mCount % 100 < 50) {
            this.mBitmapState = 1;
        } else if (mCount % 100 < 75) {
            this.mBitmapState = 2;
        } else {
            this.mBitmapState = 3;
        }

        if (mPathMove == true) {
            // 움직일 경우
            if (mPathMoveStep == true) {

                //moveToBlock(mPath[mPathStep][ConstMgr.ROW], mPath[mPathStep][ConstMgr.COL]);
                //mPathMoveStep = false;
                if(mPathStep == 0 || (Map.mLandForceInfo[mPath[mPathStep][ConstMgr.ROW]][mPath[mPathStep][ConstMgr.COL]][0] != ConstMgr.KIND_OUR &&
                        Map.mLandForceInfo[mPath[mPathStep][ConstMgr.ROW]][mPath[mPathStep][ConstMgr.COL]][0] != ConstMgr.KIND_ENEMY)){
                    moveToBlock(mPath[mPathStep][ConstMgr.ROW], mPath[mPathStep][ConstMgr.COL]);
                    mPathMoveStep = false;
                }
                else if((Map.mLandForceInfo[mPath[mPathStep][ConstMgr.ROW]][mPath[mPathStep][ConstMgr.COL]][0] == ConstMgr.KIND_OUR &&
                        Map.mLandForceInfo[mPath[mPathStep][ConstMgr.ROW]][mPath[mPathStep][ConstMgr.COL]][0] == ConstMgr.KIND_ENEMY)){
                    if(mCount % 100 != 0){//길을 막을시 이벤트
                    }
                    else {
                        setOrder(ConstMgr.ORDER_ATTACK);
                    }
                }

            }
            // 해당 블럭의 좌표에 도착했을 경우(좌표의 이동인 관계로 범위로 체크했음)

            if(mPathMoveStep == false) {
                if (this.mPosX <= this.mTargetX + 10
                        && this.mPosX >= this.mTargetX - 10
                        && this.mPosY <= this.mTargetY + 10
                        && this.mPosY >= this.mTargetY - 10) {
                    this.mPosX = this.mTargetX;
                    this.mPosY = this.mTargetY;
                    mPathMoveStep = true;
                    mPathStep++;
                    // mPath배열의 마지막 인덱스까지 계산했다면 최종 목적지에 도착한 상태임

                    if (mPathStep == mPathMax) {
                        // 다시 값을 초기화 하고 도착했다고 알림
                        mPathMove = false;
                        mPathMax = 0;
                        mPathCount = 0;//도착
                        setOrder(ConstMgr.ORDER_ATTACK);
                    }
                }
            }
        }
        if(mPathMove == false){
            if(mPathMoveStep == false){
                if(mOrderState == ConstMgr.ORDER_ATTACK){
                    if(mCount % 100 ==0){
                        setOrder(ConstMgr.ORDER_ATTACK);
                    }
                }
            }
        }
        // 주변에 적이 있을 경우
        if(attackNear()){
            if(mOrderState == ConstMgr.ORDER_ATTACK){
                mPathMove = false;
                mPathMoveStep = false;
            }
            return;
        }

        //현재 상태에서 주변의 적이 가까이 올 경우 공격모드
        if(mOrderState!=ConstMgr.ORDER_ATTACK){
            if(findNear()){
                setOrder(ConstMgr.ORDER_ATTACK);
            }
        }
    }

    // 병사 객체를 게임이 아닌 초기화면이나 전체 맵에서 사용하기 위해 사용하는 함수

    public void thinkSimple() {
        mCount++;

        if (mCount > 30000) {
            mCount = 0;
        }
        if (this.mPosX > this.mTargetX + 5) {
            this.mPosX = this.mPosX - 4;
        } else if (this.mPosX < this.mTargetX - 4) {
            this.mPosX = this.mPosX + 4;
        }
        if (this.mPosY > this.mTargetY + 5) {
            this.mPosY = this.mPosY - 4;
        } else if (this.mPosY < this.mTargetY - 5) {
            this.mPosY = this.mPosY + 4;
        }

        if (mCount % 100 < 25) {
            this.mBitmapState = 0;
        } else if (mCount % 100 < 50) {
            this.mBitmapState = 1;
        } else if (mCount % 100 < 75) {
            this.mBitmapState = 2;
        } else {
            this.mBitmapState = 3;
        }
    }

    //병사당 관리될 에너지를 붙이기 위해 객체를 전달받는 메소드
    public void addObject(Panel forceEnergy) {
        mForceEnergy = forceEnergy;
    }

    //병사의 대화 위치 설정
    public void moveWord() {
        mForceEnergy.setPos(this.mPosX, this.mPosY + this.getHeight() / 2);
    }


    // 병사와 에너지바를 활성화, 비활성화
    public void setIsActiveAll(boolean isActive) {
        setIsActive(isActive);
        mForceEnergy.setIsActive(isActive);
        // 비활성화일 경우 초기화시킴
    }
    // 해당 위치에 병사 및 에너지바를 설정함.
    public void setPosAll(int x, int y) {
        this.setPos(x, y);
        mForceEnergy.setPos(x, y + this.getHeight());
    }
    // 게임 맵의 블럭 위치에 병사, 에너지의 위치를 설정함
    public void setPosBlockAll(int row, int col) {
        setToBlock(row, col);
        mForceEnergy.setPos(Map.getPosX(row,col), Map.getPosY(row, col) + this.getHeight());
    }
    // 병사, 에너지를 모두 해당 블럭으로 이동시킴
    public void moveToBlockAll(int row, int col) {
        this.moveToBlock(row, col);
        mForceEnergy.setPos(this.mPosX, this.mPosY + this.getHeight());
    }
    // 병사, 에너지를 모두 출력함
    public void drawAll(float[] m) {
        this.draw(m);
        mForceEnergy.draw(m);
    }

    public int[][] getShortPath(int tRow, int tCol) throws Exception {
        // 현재 병사의 블럭을 중심으로 시작함.
        int sRow = mPosBlockRow;
        int sCol = mPosBlockCol;
        // 현재 지형을 path에 대입함. 움직일 수 없는 지형은 10000을 입력함.
        int[][] path = new int[Map.mInfoSizeRow][Map.mInfoSizeCol];
        for (int i = 0; i < Map.mInfoSizeRow; i++) {
            for (int j = 0; j < Map.mInfoSizeCol; j++) {
                if (Map.mInfo[i][j]==0 ){
                    // 평지일 경우 지나다닐 수 있다.
                    path[i][j] = 0;
                } else {
                    // 장애물일 경우 임의의 높은 숫자로 설정함.
                    path[i][j] = 10000;
                }
            }
        }
        // 최종으로 반환할 경로 배열
        int[][] calcPath = new int[10000][2];
        // 최종으로 반환할 경로 배열에 최종적으로 관리할 인덱스 변수
        int maxNum = 0;
        // 현재의 위치를 먼저 담는다.
        calcPath[maxNum][ConstMgr.ROW] = sRow;
        calcPath[maxNum][ConstMgr.COL] = sCol;
        maxNum++;
        // 최종목적지에 다다랗다면 findIt을 true롤 설정한다.
        boolean findIt = false;
        // 최단거리를 구하기 위한 반복회수 10000으로 설정
        int loopCount = 10000;
        for (int curNum = 0; curNum < loopCount; curNum++) {
            // 최종 좌표를 읽어와 처리함
            int curRow = calcPath[curNum][ConstMgr.ROW];
            int curCol = calcPath[curNum][ConstMgr.COL];
            // 현재 위치가 이동이 불가할 경우 findIt = false 설정후 종료
            if (path[curRow][curCol] == 10000) {
                findIt = false;
                break;
            }
            // 현재 좌표와 움직이려는 좌표를 기준으로 4 반향 중
            // 가장 가까운 방향을 먼저 시작하도록 계산한다.
            float[] tempDir = new float[4];
            tempDir[0] = (float) (Math.abs(curRow - 1 - tRow) * Math.abs(curRow - 1 - tRow) + Math.abs(curCol - tCol) * Math.abs(curCol - tCol));
            tempDir[1] = (float) (Math.abs(curRow + 1 - tRow) * Math.abs(curRow + 1 - tRow) + Math.abs(curCol - tCol) * Math.abs(curCol - tCol));
            tempDir[2] = (float) (Math.abs(curRow - tRow) * Math.abs(curRow - tRow) + Math.abs(curCol - 1 - tCol) * Math.abs(curCol - 1 - tCol));
            tempDir[3] = (float) (Math.abs(curRow - tRow) * Math.abs(curRow - tRow) + Math.abs(curCol + 1 - tCol) * Math.abs(curCol + 1 - tCol));
            int[] tempDirValue = {0, 1, 2, 3};
            for (int i = 0; i < 4; i++) {
                for (int j = 1; j < 4; j++){
                    if (tempDir[j] < tempDir[i]) {
                        float temp = tempDir[i];
                        tempDir[i] = tempDir[j];
                        tempDir[j] = temp;
                        int tempValue = tempDirValue[i];
                        tempDirValue[i] = tempDirValue[j];
                        tempDirValue[j] = tempValue;
                    }
                }
            }
            int minusRow = curRow - 1;
            int plusRow = curRow + 1;
            int minusCol = curCol - 1;
            int plusCol = curCol + 1;
            if (minusRow < 0)
                minusRow = 0;
            if (plusRow > Map.mInfoSizeRow - 1)
                plusRow = Map.mInfoSizeRow - 1;
            if (minusCol < 0)
                minusCol = 0;
            if (plusCol > Map.mInfoSizeCol - 1)
                plusCol = Map.mInfoSizeCol - 1;

            // 4방향 중 목적지와 동일하다면 findIt = true가 됨
            // dirOrder : 0 윗쪽, 1 아래쪽, 2 왼쪽, 3 오른쪽
            int dirOrder = 0;
            if (curRow - 1 == tRow && curCol == tCol) {
                dirOrder = 0;
                //findIt = true;
            }
            else if (curRow + 1 == tRow && curCol == tCol) {
                dirOrder = 1;
                //findIt = true;
            }
            else if (curRow == tRow && curCol - 1 == tCol) {
                dirOrder = 2;
                //findIt = true;
            }
            else if (curRow == tRow && curCol + 1 == tCol) {
                dirOrder = 3;
                //findIt = true;
            }
            else {
                // 자신이 지나온 길은 path의 값을 아래쪽에서 1씩 증가시킨다.
                // 2 최종 목적지가 아닐 경우 4군데를 비교하여 가까운쪽 방향을 우선순위로 하여
                // 1 path의 값이 낮은곳을 먼저 찾게 한다.
                if (tempDirValue[0] == 0) {
                    if (curRow != 0 && path[minusRow][curCol] <= path[minusRow][curCol] && path[minusRow][curCol] <= path[plusRow][curCol] && path[minusRow][curCol] <= path[curRow][minusCol] && path[minusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 0;
                    }
                    else if (curCol != 0 && path[curRow][minusCol] <= path[minusRow][curCol] && path[curRow][minusCol] <= path[plusRow][curCol] && path[curRow][minusCol] <= path[curRow][minusCol] && path[curRow][minusCol] <= path[curRow][plusCol]) {
                        dirOrder = 2;
                    }
                    else if (curCol != Map.mInfoSizeCol - 1 && path[curRow][plusCol] <= path[minusRow][curCol] && path[curRow][plusCol] <= path[plusRow][curCol] && path[curRow][plusCol] <= path[curRow][minusCol] && path[curRow][plusCol] <= path[curRow][plusCol]) {
                        dirOrder = 3;
                    }
                    else if (curRow != Map.mInfoSizeRow  - 1 && path[plusRow][curCol] <= path[minusRow][curCol] && path[plusRow][curCol] <= path[plusRow][curCol] && path[plusRow][curCol] <= path[curRow][minusCol] && path[plusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 1;
                    }
                }
                else if (tempDirValue[0] == 1) {
                    if (curRow != Map.mInfoSizeRow - 1 && path[plusRow][curCol] <= path[minusRow][curCol] && path[plusRow][curCol] <= path[plusRow][curCol] && path[plusRow][curCol] <= path[curRow][minusCol] && path[plusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 1;
                    }
                    else if (curCol != 0 && path[curRow][minusCol] <= path[minusRow][curCol] && path[curRow][minusCol] <= path[plusRow][curCol] && path[curRow][minusCol] <= path[curRow][minusCol] && path[curRow][minusCol] <= path[curRow][plusCol]) {
                        dirOrder = 2;
                    }
                    else if (curCol != Map.mInfoSizeCol - 1 && path[curRow][plusCol] <= path[minusRow][curCol] && path[curRow][plusCol] <= path[plusRow][curCol] && path[curRow][plusCol] <= path[curRow][minusCol] && path[curRow][plusCol] <= path[curRow][plusCol]) {
                        dirOrder = 3;
                    }
                    else if (curRow != 0 && path[minusRow][curCol] <= path[minusRow][curCol] && path[minusRow][curCol] <= path[plusRow][curCol] && path[minusRow][curCol] <= path[curRow][minusCol] && path[minusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 0;
                    }

                }
                else if (tempDirValue[0] == 2) {
                    if (curCol != 0 && path[curRow][minusCol] <= path[minusRow][curCol] && path[curRow][minusCol] <= path[plusRow][curCol] && path[curRow][minusCol] <= path[curRow][minusCol] && path[curRow][minusCol] <= path[curRow][plusCol]) {
                        dirOrder = 2;
                    }
                    else if (curRow != 0 && path[minusRow][curCol] <= path[minusRow][curCol] && path[minusRow][curCol] <= path[plusRow][curCol] && path[minusRow][curCol] <= path[curRow][minusCol] && path[minusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 0;
                    }
                    else if (curRow != Map.mInfoSizeRow - 1 && path[plusRow][curCol] <= path[minusRow][curCol] && path[plusRow][curCol] <= path[plusRow][curCol] && path[plusRow][curCol] <= path[curRow][minusCol] && path[plusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 1;
                    }
                    else if (curCol != Map.mInfoSizeCol - 1 && path[curRow][plusCol] <= path[minusRow][curCol] && path[curRow][plusCol] <= path[plusRow][curCol] && path[curRow][plusCol] <= path[curRow][minusCol] && path[curRow][plusCol] <= path[curRow][plusCol]) {
                        dirOrder = 3;
                    }
                }
                else if (tempDirValue[0] == 3) {
                    if (curCol != Map.mInfoSizeCol - 1 && path[curRow][plusCol] <= path[minusRow][curCol] && path[curRow][plusCol] <= path[plusRow][curCol] && path[curRow][plusCol] <= path[curRow][minusCol] && path[curRow][plusCol] <= path[curRow][plusCol]) {
                        dirOrder = 3;
                    }
                    else if (curRow != 0 &&	path[minusRow][curCol] <= path[minusRow][curCol] && path[minusRow][curCol] <= path[plusRow][curCol] && path[minusRow][curCol] <= path[curRow][minusCol] && path[minusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 0;
                    }
                    else if (curRow != Map.mInfoSizeRow - 1 && path[plusRow][curCol] <= path[minusRow][curCol] && path[plusRow][curCol] <= path[plusRow][curCol] && path[plusRow][curCol] <= path[curRow][minusCol] && path[plusRow][curCol] <= path[curRow][plusCol]) {
                        dirOrder = 1;
                    }
                    else if (curCol != 0 && path[curRow][minusCol] <= path[minusRow][curCol] && path[curRow][minusCol] <= path[plusRow][curCol] && path[curRow][minusCol] <= path[curRow][minusCol] && path[curRow][minusCol] <= path[curRow][plusCol]) {
                        dirOrder = 2;
                    }
                }
            }

            // 방향이 정해졌으므로 해당 방향으로 움직인다.
            if (dirOrder == 0) {
                if (curRow - 1 == tRow && curCol == tCol) {
                    findIt = true;
                }
                if (curRow != 0 && path[curRow - 1][curCol] != 10000) {
                    // 지나온 길은 path에 1씩 증가시킨다. 이후 지나온 길은 위의 로직에 의해
                    // 우선순위에서 밀려난다.
                    path[curRow - 1][curCol]++;
                    calcPath[maxNum][ConstMgr.ROW] = curRow - 1;
                    calcPath[maxNum][ConstMgr.COL] = curCol;
                    maxNum++;
                }
            }
            else if (dirOrder == 1) {
                if (curRow + 1 == tRow && curCol == tCol) {
                    findIt = true;
                }
                if (curRow != Map.mInfoSizeRow - 1 && path[curRow + 1][curCol] != 10000) {
                    path[curRow + 1][curCol]++;
                    calcPath[maxNum][ConstMgr.ROW] = curRow + 1;
                    calcPath[maxNum][ConstMgr.COL] = curCol;
                    maxNum++;
                }
            }
            else if (dirOrder == 2) {
                if (curRow == tRow && curCol - 1 == tCol) {
                    findIt = true;
                }
                if (curCol != 0 && path[curRow][curCol - 1] != 10000) {
                    path[curRow][curCol - 1]++;
                    calcPath[maxNum][ConstMgr.ROW] = curRow;
                    calcPath[maxNum][ConstMgr.COL] = curCol - 1;
                    maxNum++;
                }

            }
            else if (dirOrder == 3) {
                if (curRow == tRow && curCol + 1 == tCol) {
                    findIt = true;
                }
                if (curCol != Map.mInfoSizeCol - 1 && path[curRow][curCol + 1] != 10000) {
                    path[curRow][curCol + 1]++;
                    calcPath[maxNum][ConstMgr.ROW] = curRow;
                    calcPath[maxNum][ConstMgr.COL] = curCol + 1;
                    maxNum++;
                }
            }

            // 목적지를 찾았으므로 종료한다.
            // 더 검색하고 최단거리를 계산할수도 있겠지만 타협하고 여기서 종료한다.
            if (findIt == true) {
                break;
            }

            // 10000번 반복했는데도 아직 찾지 못했으므로 findIt=false 다.
            if (curNum > 0 && curNum == loopCount - 1) {
                findIt = false;
                break;
            }
        }

        // 아직 최종 좌표의 배열을 반환 전에 중복된 길은 제거한다.
        if (findIt == true) {
            for (int i = 0; i < maxNum - 1; i++) {
                for (int j = i + 1; j < maxNum; j++) {
                    if (calcPath[i][ConstMgr.ROW] == calcPath[j][ConstMgr.ROW] && calcPath[i][ConstMgr.COL] == calcPath[j][ConstMgr.COL]) {
                        int gab = j - i;
                        for (int k = i; k < maxNum; k++) {
                            calcPath[k][ConstMgr.ROW] = calcPath[k + gab][ConstMgr.ROW];
                            calcPath[k][ConstMgr.COL] = calcPath[k + gab][ConstMgr.COL];
                        }
                        maxNum = maxNum - gab;
                        i--;
                        break;
                    }
                }
            }
        }
        else {
            // 못찾았을 경우 오류로 처리
            throw new Exception();
        }
        // 최종적으로 계산된 배열의 크기와 배열을 반환한다.
        mPathMax = maxNum;
        return calcPath;
    }

    // 목적지에 맞는 블럭 목록을 mPath에 축적함.
    public void moveToPosBlock(int row, int col) {
        mPathMove = false;
        mPathMoveStep = false;
        mPathMax = 0;
        mPathStep = 0;
        try {
            mTargetBlockRow = row;
            mTargetBlockCol = col;
            mPath = getShortPath(row, col);
            mPathMove = true;
            mPathMoveStep = true;
        }
        catch (Exception ex) {
            Log.e("", "길찾기오류:" + ex.toString());
        }
    }

    // 주변의 적중 가장 가까운 블럭의 적을 검색함
    //MainGlRender클래스에서 addOurForce메소드에 유닛이 가까운 적을 찾아가도록 추가
    public int[] findNearEnemyPosBlock(){
        int enemyIndex = -1;
        int tempCount = 0;
        int tempShortDistance = 0;
        int sRow = mPosBlockRow;
        int sCol = mPosBlockCol;
        int[] rtvBlock = {-1, -1};
        if(mKind == ConstMgr.KIND_OUR) {
            for(int i=0; i< Map.mInfoSizeRow; i++){
                for(int j=0; j< Map.mInfoSizeCol; j++){
                    if(Map.mLandForceInfo[i][j][0] == ConstMgr.KIND_ENEMY){
                        if(tempCount ==0) {
                            enemyIndex = Map.mLandForceInfo[i][j][1];
                            tempShortDistance = Math.abs(sRow - i) + Math.abs(sCol - j);
                            rtvBlock[0] = i;
                            rtvBlock[1] = j;
                        }
                        else {
                            if(Math.abs(sRow - i) + Math.abs(sCol - j) < tempShortDistance){
                                enemyIndex = Map.mLandForceInfo[i][j][1];
                                tempShortDistance = Math.abs(sRow - i) + Math.abs(sCol - j);
                                rtvBlock[0] = i;
                                rtvBlock[1] = j;
                            }
                        }
                        tempCount++;
                    }
                }
            }
        }
        else if(mKind == ConstMgr.KIND_ENEMY){
            for(int i=0; i< Map.mInfoSizeRow; i++){
                for(int j=0; j< Map.mInfoSizeCol; j++){
                    if(Map.mLandForceInfo[i][j][0] == ConstMgr.KIND_OUR){
                        if(tempCount ==0) {
                            enemyIndex = Map.mLandForceInfo[i][j][1];
                            tempShortDistance = (sRow - i) + (sCol - j);
                            rtvBlock[0] = i;
                            rtvBlock[1] = j;
                        }
                        else {
                            if(Math.abs(sRow - i) + Math.abs(sCol - j) < tempShortDistance){
                                enemyIndex = Map.mLandForceInfo[i][j][1];
                                tempShortDistance = Math.abs(sRow - i) + Math.abs(sCol - j);
                                rtvBlock[0] = i;
                                rtvBlock[1] = j;
                            }
                        }
                        tempCount++;
                    }
                }
            }
        }
        return rtvBlock;
    }

    
    //3*3블럭안에 적이 가까이 왔는지 체크(적군일경우)
    //적이 가까이 왔을 경우 true, 없을 경우  false로 상태를 체크하고 
    //think메소드에 주변의 적이 가깝에 왔는지 체크
    public boolean findNear(){
        if(mKind == ConstMgr.KIND_ENEMY){
            for(int i=mPosBlockRow -3; i<= mPosBlockRow + 3; i++){
                for(int j=mPosBlockCol -3; j  < mPosBlockCol + 3; j++){
                    if(i<0 || i>19 || j <0 || j>19){
                        continue;
                    }
                    if(Map.mLandForceInfo[i][j][0] == ConstMgr.KIND_OUR){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    //근처의 적 공격을 위한 구현메소드 실제작동은 Renderer
    public boolean attackNear() {
        boolean attackCls = false;
        int calcPosBlockRow = -1;
        int calcPosBlockCol = -1;
        int tempArrayCount = 8;
        // 궁병은 두 블럭을 더 공격할 수 있음.
        if (mType == ConstMgr.TYPE_BOW_MAN){
            tempArrayCount = 23;
        }
        // 아군은 윗쪽을 먼저 공격함
        if(mKind == ConstMgr.KIND_OUR) {
            for(int i=0;i< tempArrayCount;i++) {
                if(i==0) {
                    calcPosBlockRow = mPosBlockRow;
                    calcPosBlockCol = mPosBlockCol - 1;
                }
                else if(i==1){
                    calcPosBlockRow = mPosBlockRow - 1;
                    calcPosBlockCol = mPosBlockCol - 1;
                }
                else if(i==2){
                    calcPosBlockRow = mPosBlockRow + 1;
                    calcPosBlockCol = mPosBlockCol - 1;
                }
                else if(i==3){
                    calcPosBlockRow = mPosBlockRow - 1;
                    calcPosBlockCol = mPosBlockCol;
                }
                else if(i==4){
                    calcPosBlockRow = mPosBlockRow + 1;
                    calcPosBlockCol = mPosBlockCol;
                }
                else if(i==5){
                    calcPosBlockRow = mPosBlockRow;
                    calcPosBlockCol = mPosBlockCol + 1;
                }
                else if(i==6){
                    calcPosBlockRow = mPosBlockRow - 1;
                    calcPosBlockCol = mPosBlockCol + 1;
                }
                else if(i==7){
                    calcPosBlockRow = mPosBlockRow + 1;
                    calcPosBlockCol = mPosBlockCol + 1;
                }
                if(mType == ConstMgr.TYPE_BOW_MAN) {
                    if(i==8) {  // 최상단 5곳.
                        calcPosBlockRow = mPosBlockRow;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==9){
                        calcPosBlockRow = mPosBlockRow - 1;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==10){
                        calcPosBlockRow = mPosBlockRow + 1;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==11){
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==12){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==13){ // 상단 2곳
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol - 1;
                    }
                    else if(i==14){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol - 1;
                    }
                    else if(i==15){ // 중간 2곳
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol;
                    }
                    else if(i==16) {
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol;
                    }
                    else if(i==17){ // 하단 2곳
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol + 1;
                    }
                    else if(i==18){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol + 1;
                    }
                    else if(i==19){ // 최하단 5곳
                        calcPosBlockRow = mPosBlockRow;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==20){	// 하단
                        calcPosBlockRow = mPosBlockRow - 1;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==21){
                        calcPosBlockRow = mPosBlockRow + 1;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==22){
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==23){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                }
                if (calcPosBlockRow >= 0 && calcPosBlockRow < Map.mInfoSizeRow && calcPosBlockCol >= 0 && calcPosBlockCol < Map.mInfoSizeCol) {
                    if(mIsActive == true) {
                        if (Map.mLandForceInfo[calcPosBlockRow][calcPosBlockCol][0] == ConstMgr.KIND_ENEMY) {
                            mMainGLRenderer.attackNear(ConstMgr.KIND_ENEMY,Map.mLandForceInfo[calcPosBlockRow][calcPosBlockCol][1], mAttackPoint);
                            return true;
                        }
                    }
                }
            }
        }
        // 적군은 아래쪽을 먼저 공격함
        else if(mKind == ConstMgr.KIND_ENEMY) {
            for(int i=0;i< 8; i++) {
                if(i==0) {
                    calcPosBlockRow = mPosBlockRow;
                    calcPosBlockCol = mPosBlockCol + 1;
                }
                else if(i==1){
                    calcPosBlockRow = mPosBlockRow - 1;
                    calcPosBlockCol = mPosBlockCol + 1;
                }
                else if(i==2){
                    calcPosBlockRow = mPosBlockRow + 1;
                    calcPosBlockCol = mPosBlockCol + 1;
                }
                else if(i==3){
                    calcPosBlockRow = mPosBlockRow - 1;
                    calcPosBlockCol = mPosBlockCol;
                }
                else if(i==4){
                    calcPosBlockRow = mPosBlockRow + 1;
                    calcPosBlockCol = mPosBlockCol;
                }
                else if(i==5){
                    calcPosBlockRow = mPosBlockRow;
                    calcPosBlockCol = mPosBlockCol - 1;
                }
                else if(i==6){
                    calcPosBlockRow = mPosBlockRow - 1;
                    calcPosBlockCol = mPosBlockCol - 1;
                }
                else if(i==7){
                    calcPosBlockRow = mPosBlockRow + 1;
                    calcPosBlockCol = mPosBlockCol - 1;
                }
                if(mType == ConstMgr.TYPE_BOW_MAN) {

                    if(i==8) {  // 최상단 5곳.
                        calcPosBlockRow = mPosBlockRow;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==9){
                        calcPosBlockRow = mPosBlockRow - 1;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==10){
                        calcPosBlockRow = mPosBlockRow + 1;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==11){
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==12){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol + 2;
                    }
                    else if(i==13){ // 상단 2곳
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol + 1;
                    }
                    else if(i==14){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol + 1;
                    }
                    else if(i==15){ // 중간 2곳
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol;
                    }
                    else if(i==16) {
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol;
                    }
                    else if(i==17){ // 하단 2곳
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol - 1;
                    }
                    else if(i==18){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol - 1;
                    }
                    else if(i==19){ // 최하단 5곳
                        calcPosBlockRow = mPosBlockRow;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==20){	// 하단
                        calcPosBlockRow = mPosBlockRow - 1;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==21){
                        calcPosBlockRow = mPosBlockRow + 1;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==22){
                        calcPosBlockRow = mPosBlockRow - 2;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                    else if(i==23){
                        calcPosBlockRow = mPosBlockRow + 2;
                        calcPosBlockCol = mPosBlockCol - 2;
                    }
                }
                if (calcPosBlockRow >= 0 && calcPosBlockRow < Map.mInfoSizeRow && calcPosBlockCol >= 0 && calcPosBlockCol < Map.mInfoSizeCol) {
                    if(mIsActive == true) {
                        if (Map.mLandForceInfo[calcPosBlockRow][calcPosBlockCol][0] == ConstMgr.KIND_OUR) {
                            mMainGLRenderer.attackNear(ConstMgr.KIND_OUR, Map.mLandForceInfo[calcPosBlockRow][calcPosBlockCol][1], mAttackPoint);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //명령
    public void setOrder (int order){
        if(this.mType == ConstMgr.TYPE_BASE){
            return;
        }
        if(order == ConstMgr.ORDER_ATTACK){
            int[] posBlock = findNearEnemyPosBlock();
            if(posBlock[0] != -1 && posBlock[1] != -1 ) {
                moveToPosBlock(posBlock[0], posBlock[1]);
            }
        }
        else if (order == ConstMgr.ORDER_DEFENCE) {
        }
    }
    
    //죽으면 그리기 취소
    public void setDead(){
        this.setIsActiveAll(false);
    }
    //피격시 피해계산
    public void attacked(int point){
        this.mCurrentEnergy = (int) (this.mCurrentEnergy - (point * (100 - this.mDefence) * 0.01f));
        if(this.mCurrentEnergy >=0){
            Log.e("", "에너지" + ((float)this.mCurrentEnergy)/ this.mEnergy );
            mForceEnergy.setScaleX(((float)this.mCurrentEnergy)/ this.mEnergy);
        }
        if(this.getIsActive() == true && this.mCurrentEnergy <=0){
            mMainGLRenderer.mActivity.soundFight();
            this.setDead();
        }
    }



    // 그리기
    void draw(float[] m){

        // 활성화 상태가 아니라면 그리지 않음

        if (mIsActive == false) {
            return;
        }

        // 회전, 가로, 세로 확대/축소를 미리 체크하여 변환모듈 호출을 관리함

        if(this.mAngle != 0) {
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.setIdentityM(mRotationMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, mPosX, mPosY, 0);
            Matrix.setRotateM(mRotationMatrix, 0, this.mAngle, 0, 0, -1.0f);
            Matrix.multiplyMM(mMVPMatrix, 0, m, 0, mTranslationMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix2, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        }
        else if(this.mScaleX != 1.0f || this.mScaleY != 1.0f) {
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.setIdentityM(mScaleMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, mPosX, mPosY, 0);
            Matrix.scaleM(mScaleMatrix, 0, this.mScaleX, this.mScaleY, 1.0f);
            Matrix.multiplyMM(mMVPMatrix, 0, m, 0, mTranslationMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix2, 0, mMVPMatrix, 0, mScaleMatrix, 0);
        }
        else {
            Matrix.setIdentityM(mTranslationMatrix, 0);
            Matrix.translateM(mTranslationMatrix, 0, mPosX, mPosY, 0);
            Matrix.multiplyMM(mMVPMatrix2, 0, m, 0, mTranslationMatrix, 0);
        }
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0,
                mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHandleBitmap[mBitmapState]);
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mMVPMatrix2, 0);
        GLES20.glUniform1i(mSamplerLoc, 0);
        // 투명한 배경을 처리함
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // 해당 이미지 핸들을 찾아 출력해줌 (think 메소드에서 mBtimapState를
        // 주기적으로 변경함
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }
}