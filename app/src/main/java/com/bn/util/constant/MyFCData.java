package com.bn.util.constant;

import java.util.ArrayList;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import uk.co.geolib.geolib.C2DPoint;

import com.bn.fastcut.MySurfaceView;
import com.bn.object.BNObject;
import com.bn.util.box2d.Box2DUtil;
import com.bn.util.manager.ShaderManager;
import com.bn.util.manager.TextureManager;

public class MyFCData {
	//================start========================
	public static String[] gamePicName={"s_01.png","s_02.png",
			"s_03.png","s_04.png","s_05.png","s_06.png"};
	public static String goal[]={"40","30","20","20","20","20"};//目标面积
	//物体原始数据
	public static float[][] data={
			{255,826, 74,1216,  520,1626, 1022,548, 689,371,  468,1143},
			{696,288, 114,1072, 422,1156, 316,1716, 978,930, 580,856},
			{536,384, 380,712,  38,772,   289,1034, 230,1392, 540,1224, 844,1392, 788,1028, 1038,772, 694,714},
			{544,320, 112,506,  114,870,  334,970,  110,1066, 108,1434, 542,1618, 968,1434, 966,1066, 754,970, 970,870, 972,506},
			{542,374, 378,828,  52,726, 290,1152,  100,1404,  986,1404, 792,1152, 1032,726, 706,830},
			{290,362, 288,1096, 88,1332, 294,1578, 800,1578, 802,848, 1006,606, 798,362}
	};
	public static boolean[][] dataBool={
			{true,true,true,true,true,true},
			{true,true,true,true,true,true},
			{true,true,false,true,false,true,false,true,true,true},
			{false,false,true,true,true,true,true,true,false,true,true,true},
			{false,true,true,false,true,false,true,true,true},
			{true,false,false,true,true,false,false,true}
	};
	public static float[][] ballData={
			{540,960,50,2.5f,0,1.0f,30,50},
			{550,980,50,1.0f,0,1.0f,20,40}
	};//球基本数据-x,y位置、半径、密度、摩擦力、恢复系数、速度（x、y）

	//================end========================第一关

	//获得创建包围框的顶点数据
	public static float[] getData(float[] boxData,int i)
	{
		float[] result=new float[4];//创建长度为4的一维数组
		for(int j=0;j<result.length;j++)
		{
			result[j]=boxData[(2*i+j)%boxData.length];
		}
		return result;//返回结果
	}

	//获得球的位置
	public static C2DPoint[] getBallPosition(ArrayList<BNObject> alBNBall)
	{
		C2DPoint[] pointLocation=new C2DPoint[alBNBall.size()];//保存球当时位置的点对象
		int count=0;
		for(BNObject ball:alBNBall)//遍历球对象
		{
			pointLocation[count]=new C2DPoint();
			pointLocation[count].x=ball.ballPositionX;//存x坐标
			pointLocation[count].y=ball.ballPositionY;//存y坐标
			count++;
		}
		return pointLocation;
	}

	public static ArrayList<Body> getBody(World world,float[] bodydata)//获得物体世界里的边刚体
	{
		ArrayList<Body> alBNBody=new  ArrayList<Body>();//获得边框刚体对象列表
		for(int i=0;i<bodydata.length/2;i++)//创建包围框
		{
			float[] data=getData(bodydata,i);
			Body bd=Box2DUtil.createEdge//创建直线
					(
							data,
							world,
							true,
							0f,
							0f,
							0f,
							-1
					);
			alBNBody.add(bd);
		}
		return alBNBody;
	}

	public static ArrayList<BNObject> getPauseView()//根据具体数据获得Object对象
	{
		ArrayList<BNObject> pauseView=new ArrayList<BNObject>();
		pauseView.add(
				new BNObject(//绘制暂停界面
						540,
						900,
						1000,
						800,
						TextureManager.getTextures("suspend_bg.png"),
						ShaderManager.getShader(0)
				)
		);
		pauseView.add(
				new BNObject(//绘制选择关卡按钮
						300,
						1000,
						200,
						200,
						TextureManager.getTextures("guanQia_a.png"),
						ShaderManager.getShader(0)
				)
		);
		pauseView.add(
				new BNObject(//绘制重玩按钮
						550,
						1000,
						200,
						200,
						TextureManager.getTextures("replay_a.png"),
						ShaderManager.getShader(0)
				)
		);
		pauseView.add(
				new BNObject(//绘制继续按钮
						800,
						1000,
						200,
						200,
						TextureManager.getTextures("resume_a.png"),
						ShaderManager.getShader(0)
				)
		);
		return pauseView;
	}

	public static ArrayList<BNObject> getBall(MySurfaceView mv,World world,float[][] ballBaseData)//创建需被绘制的球
	{
		ArrayList<BNObject> alBNBall=new ArrayList<BNObject>();//创建需被绘制的球对象列表
		for(int i=0;i<ballBaseData.length;i++)
		{
			BNObject bn=Box2DUtil.createCircle//创建球
					(
							mv,
							ballBaseData[i][0],
							ballBaseData[i][1],
							ballBaseData[i][2],
							world,
							0,
							TextureManager.getTextures("dartsmall.png"),
							ballBaseData[i][3],
							ballBaseData[i][4],
							ballBaseData[i][5],
							-2
					);
			bn.body.setLinearVelocity(new Vec2(ballBaseData[i][6],ballBaseData[i][7]));//给球赋予初速度值
			alBNBall.add(bn);//将球添加进绘制列表中
		}
		return alBNBall;
	}
	public static BNObject ChangeLable(float x,float y, float width,float height,String texName)//切换按钮状态
	{
		BNObject object=new BNObject(
				x,
				y,
				width,
				height,
				TextureManager.getTextures(texName),
				ShaderManager.getShader(0)

		);
		return object;
	}
	public static ArrayList<BNObject> WinView()//胜利界面
	{
		ArrayList<BNObject> winView=new ArrayList<BNObject>();
		winView.add(
				new BNObject(//绘制胜利界面
						540,
						900,
						1000,
						1500,
						TextureManager.getTextures("win.png"),
						ShaderManager.getShader(0)
				)
		);
		winView.add(
				new BNObject(//绘制选择关卡按钮
						300,
						1400,
						200,
						200,
						TextureManager.getTextures("guanQia_a.png"),
						ShaderManager.getShader(0)
				)
		);
		winView.add(
				new BNObject(//绘制重玩按钮
						550,
						1400,
						200,
						200,
						TextureManager.getTextures("replay_a.png"),
						ShaderManager.getShader(0)
				)
		);
		winView.add(
				new BNObject(//绘制继续按钮
						800,
						1400,
						200,
						200,
						TextureManager.getTextures("next_a.png"),
						ShaderManager.getShader(0)
				)
		);
		return winView;
	}
	public static ArrayList<BNObject> getCurrentData(int cur,float x,float y,float width,float height)//获得当前剩余面积的物体对象
	{
		String currentArea=cur+"";//获得当前剩余面积
		ArrayList<BNObject> AreaData=new ArrayList<BNObject>();//剩余面积的数据对象
		for(int i=0;i<=currentArea.length()-1;i++)
		{
			String str=currentArea.charAt(i)+"";//获得数据
			int data=Integer.parseInt(str);//转换成相应的数据
			AreaData.add(
					new BNObject(//切割面积
							x+width*i,
							y,
							width,
							height,
							TextureManager.getTextures("number.png"),
							ShaderManager.getShader(0),
							data
					)
			);
		}
		return AreaData;
	}

	public static ArrayList<BNObject> getData(int level)//获得原始面积数据
	{
		String goal=MyFCData.goal[level];//第一关

		ArrayList<BNObject> AreaData=new ArrayList<BNObject>();//绘制切割面积
		for(int i=0;i<=goal.length()-1;i++)
		{
			String str=goal.charAt(i)+"";//获得数据
			int data=Integer.parseInt(str);//转换成相应的数据
			AreaData.add(
					new BNObject(//切割面积
							370+40*i,
							80,
							40,
							50,
							TextureManager.getTextures("number.png"),
							ShaderManager.getShader(0),
							data
					)
			);
		}
		return AreaData;
	}
	public static ArrayList<BNObject> getLableObject()//获得游戏界面的一些基本lable
	{
		ArrayList<BNObject> lable=new ArrayList<BNObject>();//创建lable对象的列表
		lable.add(
				new BNObject(//游戏背景图
						1080/2,
						1920/2,
						1080,
						1920,
						TextureManager.getTextures("gg.png"),
						ShaderManager.getShader(0)
				)
		);
		lable.add(
				new BNObject(//切割面积lable1
						180,
						80,
						280,
						100,
						TextureManager.getTextures("lable1.png"),
						ShaderManager.getShader(0)
				)
		);
		lable.add(
				new BNObject(//目标面积的百分号
						480,
						80,
						80,
						80,
						TextureManager.getTextures("lable2.png"),
						ShaderManager.getShader(0)
				)
		);
		lable.add(
				new BNObject(//剩余面积数据的百分号
						990,
						80,
						80,
						80,
						TextureManager.getTextures("lable2.png"),
						ShaderManager.getShader(0)
				)
		);
		lable.add(
				new BNObject(//暂停按钮
						110,
						1850,
						135,
						140,
						TextureManager.getTextures("zhanting.png"),
						ShaderManager.getShader(0)
				)
		);
		return lable;
	}
}
