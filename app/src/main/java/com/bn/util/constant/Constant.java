package com.bn.util.constant;

import com.bn.util.screenscale.ScreenScaleResult;

public class Constant
{
	//=============游戏界面start==================
	public static float PauseLable_Left=50;
	public static float PauseLable_Right=210;
	public static float PauseLable_Top=1780;
	public static float PauseLable_Buttom=1900;

	//=============游戏界面end==================
	//=============暂停界面start=============
	public static float ChooseLevel_Left=100;
	public static float ChooseLevel_Right=450;
	public static float ChooseLevel_Top=850;
	public static float ChooseLevel_Buttom=1150;

	public static float ReStart_Left=450;
	public static float ReStart_Right=700;
	public static float ReStart_Top=850;
	public static float ReStart_Buttom=1150;

	public static float Continue_Left=700;
	public static float Continue_Right=950;
	public static float Continue_Top=850;
	public static float Continue_Buttom=1150;

	//=============暂停界面end=============

	//=============胜利界面start=============
	public static float WinChooseLevel_Left=100;
	public static float WinChooseLevel_Right=450;
	public static float WinChooseLevel_Top=1250;
	public static float WinChooseLevel_Buttom=1550;

	public static float WinReStart_Left=450;
	public static float WinReStart_Right=700;
	public static float WinReStart_Top=1250;
	public static float WinReStart_Buttom=1550;

	public static float WinNext_Left=700;
	public static float WinNext_Right=950;
	public static float WinNext_Top=1250;
	public static float WinNext_Buttom=1550;
	//=============胜利界面end=============

	public static float ChooseButton_Left=115;
	public static float ChooseButton_Right=345;
	public static float ChooseButton_Up=575;
	public static float ChooseButton_Down=805;

	public static float StartButton_Left=390;
	public static float StartButton_Right=770;
	public static float StartButton_Up=805;
	public static float StartButton_Down=1180;

	public static float ExitButton_Left=615;
	public static float ExitButton_Right=820;
	public static float ExitButton_Up=1315;
	public static float ExitButton_Down=1520;

	public static float HelpButton_Left=50;
	public static float HelpButton_Right=245;
	public static float HelpButton_Up=1470;
	public static float HelpButton_Down=1665;


	//===================
	public static float Choose_Back_Left=785;
	public static float Choose_Back_Right=970;
	public static float Choose_Back_Up=115;
	public static float Choose_Back_Down=250;

	public static float Choose_Music_Left=245;
	public static float Choose_Music_Right=845;
	public static float Choose_Music_Up=920;
	public static float Choose_Music_Down=1060;

	public static float Choose_Sound_Left=245;
	public static float Choose_Sound_Right=845;
	public static float Choose_Sound_Up=1200;
	public static float Choose_Sound_Down=1340;
	//===================

	public static float Help_Back_Left=805;
	public static float Help_Back_Right=985;
	public static float Help_Back_Up=70;
	public static float Help_Back_Down=205;

	//选关界面  触摸坐标常量类========start=============
	public static int LevelView_Back_Left_X=800;
	public static int LevelView_Back_Right_X=1000;
	public static int LevelView_Back_Top_Y=75;
	public static int LevelView_Back_Bottom_Y=225;

	public static int LevelView_Series1_Left_X=75;
	public static int LevelView_Series1_Right_X=725;
	public static int LevelView_Series1_Top_Y=450;
	public static int LevelView_Series1_Bottom_Y=750;

	public static int LevelView_Series2_Left_X=325;
	public static int LevelView_Series2_Right_X=975;
	public static int LevelView_Series2_Top_Y=850;
	public static int LevelView_Series2_Bottom_Y=1150;

	public static int LevelView_Series3_Left_X=250;
	public static int LevelView_Series3_Right_X=550;
	public static int LevelView_Series3_Top_Y=1075;
	public static int LevelView_Series3_Bottom_Y=1725;

	public static int LevelView_PickUp1_1_Left_X=190;
	public static int LevelView_PickUp1_1_Right_X=625;
	public static int LevelView_PickUp1_1_Top_Y=500;
	public static int LevelView_PickUp1_1_Bottom_Y=860;

	public static int LevelView_PickUp1_2_Left_X=410;
	public static int LevelView_PickUp1_2_Right_X=855;
	public static int LevelView_PickUp1_2_Top_Y=1072;
	public static int LevelView_PickUp1_2_Bottom_Y=1400;

	public static int LevelView_PickUp2_1_Left_X=485;
	public static int LevelView_PickUp2_1_Right_X=920;
	public static int LevelView_PickUp2_1_Top_Y=625;
	public static int LevelView_PickUp2_1_Bottom_Y=960;

	public static int LevelView_PickUp2_2_Left_X=185;
	public static int LevelView_PickUp2_2_Right_X=615;
	public static int LevelView_PickUp2_2_Top_Y=1170;
	public static int LevelView_PickUp2_2_Bottom_Y=1535;

	public static int LevelView_PickUp3_1_Left_X=190;
	public static int LevelView_PickUp3_1_Right_X=605;
	public static int LevelView_PickUp3_1_Top_Y=620;
	public static int LevelView_PickUp3_1_Bottom_Y=955;

	public static int LevelView_PickUp3_2_Left_X=500;
	public static int LevelView_PickUp3_2_Right_X=920;
	public static int LevelView_PickUp3_2_Top_Y=1230;
	public static int LevelView_PickUp3_2_Bottom_Y=1580;
	//选关界面  触摸坐标常量类========end=============

	//标准屏幕的宽度
	public static float StandardScreenWidth=1080;
	//标准屏幕的高度
	public static float StandardScreenHeight=1920;

	//标准屏幕宽高比
	public static float ratio=StandardScreenWidth/StandardScreenHeight;
	//缩放计算结果
	public static ScreenScaleResult ssr;

	//----------------物理世界   start----------------
	public static final float RATE = 10;//屏幕到现实世界的比例 10px：1m;
	public static final boolean DRAW_THREAD_FLAG=true;//绘制线程工作标志位

	public static final float TIME_STEP = 1.0f/60.0f;//模拟的的频率
	public static final int ITERA = 10;//迭代越大，模拟约精确，但性能越低

	//迭代越大，模拟约精确，但性能越低
	public static final int Vec_ITERA=6;//速度迭代
	public static final int POSITON_ITERA=2;//位置迭代
	//----------------物理世界   end----------------


	public static float fromPixSizeToNearSize(float size)
	{
		return size*2/StandardScreenHeight;
	}
	//屏幕x坐标到视口x坐标
	public static float fromScreenXToNearX(float x)
	{
		return (x-StandardScreenWidth/2)/(StandardScreenHeight/2);
	}
	//屏幕y坐标到视口y坐标
	public static float fromScreenYToNearY(float y)
	{
		return -(y-StandardScreenHeight/2)/(StandardScreenHeight/2);
	}
	//实际屏幕x坐标到标准屏幕x坐标
	public static float fromRealScreenXToStandardScreenX(float rx)
	{
		return (rx-ssr.lucX)/ssr.ratio;
	}
	//实际屏幕y坐标到标准屏幕y坐标
	public static float fromRealScreenYToStandardScreenY(float ry)
	{
		return (ry-ssr.lucY)/ssr.ratio;
	}
	//标准屏幕x坐标到实际屏幕x坐标
	public static float fromStandardScreenXToRealScreenX(float rx)
	{
		return rx*ssr.ratio+ssr.lucX;
	}
	//标准屏幕y坐标到实际屏幕y坐标
	public static float fromStandardScreenYToRealScreenY(float ry)
	{
		return ry*ssr.ratio+ssr.lucY;
	}
	public static float fromStandardScreenSizeToRealScreenSize(float size)
	{
		return size*ssr.ratio;
	}

}
