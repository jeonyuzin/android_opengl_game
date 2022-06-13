package com.example.final_test;

//애플리케이션에서 공통으로 사용하기 위한 상수나 상태를 관리하기 위한 클래스
public class ConstMgr {
    //유저정보
    private static String user_id;
    private static String user_nickname;

    public void setUser(String id,String nickname){
        user_id=id;
        user_nickname=nickname;
    }

    // 화면크기 (가상)
    public final static int SCREEN_WIDTH = 2000;
    public final static int SCREEN_HEIGHT = 1200;

    //화면모드
    public final static int SCREEN_INTRO = 1;
    public final static int SCREEN_MAP = 2;
    public final static int SCREEN_GAME = 3;
    public final static int SCREEN_REG=4;
    public static int SCREEN_MODE = SCREEN_GAME;

    // 병사의 수 관리
    public final static int OURFORCE_SIZE = 100;
    public final static int ENEMYFORCE_SIZE = 100;
    // 병사의 넓이와 높이
    public final static int FORCE_WIDTH = 60;
    public final static int FORCE_HEIGHT = 90;
    public final static int GENERAL_WIDTH = 100;
    public final static int GENERAL_HEIGHT = 150;
    // 병사의 타입
    public final static int TYPE_SPEAR_MAN = 0; // 창병
    public final static int TYPE_SWORD_MAN = 1; // 검병
    public final static int TYPE_BOW_MAN = 2;   // 궁병
    public final static int TYPE_SHIELD_MAN = 3;// 방패병
    public final static int TYPE_GENERAL = 4;   // 장군
    public final static int TYPE_BASE = 5; 	// 기지
    public final static int TYPE_GENERAL1 = 6;  // 장비, 장합
    public final static int TYPE_GENERAL2 = 7;  // 관우, 하후돈
    public final static int TYPE_GENERAL3 = 8;  // 유비, 조조

    //에너지바의 넓이와 높이
    public final static int FORCE_ENERGY_WIDTH = 60;
    public final static int FORCE_ENERGY_HEIGHT = 10;

    // 아군 적군 구별
    public final static int KIND_OUR=1;
    public final static int KIND_ENEMY=2;

    // 도시개수
    public final static int CITY_SIZE = 2;


    // 나무, 가옥, 지형
    public static int MAP_TREEHOUSE_SIZE = 9;    // 크기
    public final static int MAP_LAND_GREEN = 0;   // 땅
    public final static int MAP_LAND_WATER = 1;   // 물
    public final static int MAP_TREE1 = 2;  // 나무
    public final static int MAP_TREE2 = 3;
    public final static int MAP_TREE3 = 4;
    public final static int MAP_TREE4 = 5;
    public final static int MAP_HOUSE1 = 6; // 가옥
    public final static int MAP_HOUSE2 = 7;
    public final static int MAP_HOUSE3 = 8;

    // 나무, 가옥의 개수
    public final static int TREEHOUSE_SIZE = 100;
    // 나무의 넓이와 높이
    public final static int TREE_WIDTH = 100;
    public final static int TREE_HEIGHT = 150;


    // 길찾기에 사용할 행과 열
    public final static int ROW = 0;
    public final static int COL = 1;

    // 명령 (공격, 방어)
    public final static int ORDER_DEFENCE = 0;
    public final static int ORDER_ATTACK = 1;
}

