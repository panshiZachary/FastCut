package com.bn.fastcut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geopolygons.C2DPolygon;
import com.bn.object.BNObject;
import com.bn.object.BNPolyObject;
import com.bn.object.BNView;
import com.bn.util.box2d.Box2DUtil;
import com.bn.util.box2d.MyContactFilter;
import com.bn.util.constant.Constant;
import com.bn.util.constant.GeoLibUtil;
import com.bn.util.constant.GeometryConstant;
import com.bn.util.constant.MatrixState;
import com.bn.util.constant.MyFCData;
import com.bn.util.constant.ParticleConstant;
import com.bn.util.constant.SwitchIndex;
import com.bn.util.constant.isCutUtil;
import com.bn.util.manager.ShaderManager;
import com.bn.util.manager.TextureManager;
import com.bn.util.snow.ParticleForDraw;
import com.bn.util.snow.ParticleSystem;
import android.annotation.SuppressLint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import static com.bn.util.constant.Constant.*;

@SuppressLint({ "UseSparseArrays", "ViewConstructor" })
public class MySurfaceView extends GLSurfaceView
{
	private SceneRenderer mRenderer;//场景渲染器
	MyActivity activity;

	ArrayList<BNObject> alBNBall=new ArrayList<BNObject>();//创建需被绘制的球对象列表
	ArrayList<BNObject> alFlyPolygon=new ArrayList<BNObject>();//飞走的多边形
	ArrayList<Body> alBNBody=new ArrayList<Body>();//获得刚体对象列表

	List<ParticleSystem> fps=new ArrayList<ParticleSystem>();//放雪花粒子系统的列表
	HashMap<Integer,ArrayList<BNObject>> GameData=new HashMap<Integer,ArrayList<BNObject>>();//存放游戏界面的数据
	ArrayList<ArrayList<BNObject>> alBNPO=new ArrayList<ArrayList<BNObject>>();//存放其他界面的数据
	public ArrayList<Body> BallBody=new ArrayList<Body>();//存放球刚体

	int FireID[]=new int[13];//存放火花ID
	public static int CheckpointIndex=0;//具体选择第几关
	int kniftNum=0;//切割的刀数
	int gameTime=0;//游戏时间
	int tipIndex=1;//帮助界面提示界面标志位
	int tempIndex=0;//绘制火花的索引值
	int pauseDegree=0;//旋转的结束标志
	int isAgain=0;//重玩
	int pauseOne=0;//暂停界面只绘制一次
	int lineIndex=0;//切在两球中间的警告线的标志
	int chooseIndex=1;//选关界面1--on-on 2--on-off 3--off-on 4--off-off

	int beforeArea=100;
	int beforeKnifeNum=1;
	int tieCount=0;

	public World world;//创建世界

	public boolean isCutRigid=false;//是否碰到刚边
	boolean isJudgePolygon=true;//是否在物理世界里删除包围框或者添加包围框
	boolean isJudgeBall=true;//是否在物理世界里删除球刚体或者添加球刚体
	boolean isStartGame=false;//是否开始游戏----加载图片成功的标志位
	boolean isOpen=true;//是否播放音乐
	boolean isWorldStep=true;//是否允许物理模拟
	boolean isFly=false;//true为飞走的木块
	boolean isPause=false;//暂停
	boolean isWin=false;//胜利
	boolean BackGroundMusic=true;//选项界面背景音乐的标志位
	boolean SoundEffect=true;//选项界面声音特效的标志位

	boolean isDrawSnow=false;//播放雪花
	boolean isLine=false;//绘制切割线
	boolean isLight=false;//绘制刀光
	boolean tryAgain=false;//重玩开始游戏界面的标志位
	boolean twinkle=false;//画的线是否闪烁
	boolean isFirstPause=false;//暂停界面旋转出来的标志位
	boolean isPush=false;//是否push
	boolean isDrawWin=false;
	boolean isCut=false;//不能切多边形
	boolean isOwnAxe=false;
	boolean isCutOne=false;

	public float[] intersectPoint=new float[2];//两条线段的交点
	float[] cpData;//多边形的顶点数据
	float AreaSize=0;//多边形面积
	float AllArea=0;//总面积
	float tip0_X=540;//帮助界面第一个小提示的x坐标
	float tip1_X=1240;//帮助界面第二个小提示的x坐标
	float tip2_X=1940;//帮助界面第三个小提示的x坐标
	float point_X=430;//帮助界面小白点的x坐标
	float x;//开始按下的x坐标
	float y;//开始按下的y坐标

	long gameST=0;//游戏开始的时间
	long pauseTime=0;//游戏暂停

	ParticleForDraw[] fpfd;//雪花绘制的对象

	BNObject line=null;//0---线     1--刀光
	BNObject Knifefire;//绘制火花的对象
	BNObject axe;//绘制斧头

	Object lockA=new Object();//加锁对象
	Object lockB=new Object();//加锁对象
	Object lockC=new Object();//给选关界面加锁
	Object lockD=new Object();//给胜利界面加锁

	BNPolyObject bnpo;

	SwitchIndex switchIndex=SwitchIndex.MainFrame;
	public MySurfaceView(MyActivity activity){
		super(activity);
		this.activity=activity;
		this.setEGLContextClientVersion(2); //设置使用OPENGL ES2.0
		mRenderer = new SceneRenderer();	//创建场景渲染器
		setRenderer(mRenderer);				//设置渲染器
		setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);//设置渲染模式为主动渲染
	}
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		switch(switchIndex)
		{
			case MainFrame://主界面
				MainTouchTask main=new MainTouchTask();
				main.doTask(e);
				break;
			case OptionFrame://选项界面
				OptionTouchTask option=new OptionTouchTask();
				option.doTask(e);
				break;
			case HelpFrame://帮助界面
				HelpTouchTask help=new HelpTouchTask();
				help.doTask(e);
				break;
			case SelectFrame://选关界面
				SelectTouchTask select=new SelectTouchTask();
				select.doTask(e);
				break;
			case FirstLevelFrame://第一大关
				FirstLevelTouchTask first=new FirstLevelTouchTask();
				first.doTask(e);
				break;
			case SecondLevelFrame://第二大关
				SecondLevelTouchTask second=new SecondLevelTouchTask();
				second.doTask(e);
				break;
			case ThirdLevelFrame://第三大关
				ThirdLevelTouchTask third=new ThirdLevelTouchTask();
				third.doTask(e);
				break;
			case GameViewFrame://游戏界面
				GameViewTouchTask gameView=new GameViewTouchTask();
				gameView.doTask(e);
				break;
		}
		return true;
	}
	public void HelpTip(int tipIndex,boolean isLeftSliding)//帮助界面切换图片方法
	{
		if(tipIndex==1)//如果是帮助界面第一个小提示
		{
			if(isLeftSliding)//如果是左滑
			{
				tip0_X=540;
				tip1_X=1240;
				tip2_X=1940;
				point_X=430;
			}else//如果是右滑
			{
				tip0_X=540;
				tip1_X=1240;
				tip2_X=-160;
				point_X=430;
			}
		}else if(tipIndex==2)//如果是帮助界面第二个小提示
		{
			tip0_X=-160;
			tip1_X=540;
			tip2_X=1240;
			point_X=515;
		}else if(tipIndex==3)//如果是帮助界面第三个小提示
		{
			if(isLeftSliding)//如果是左滑
			{
				tip0_X=1240;
				tip1_X=1940;
				tip2_X=540;
				point_X=605;
			}else//如果是右滑
			{
				tip0_X=1240;
				tip1_X=-160;
				tip2_X=540;
				point_X=605;
			}
		}
		BNObject bn0=new BNObject(tip0_X,1050,700,1150,
				TextureManager.getTextures("tip1.png"),ShaderManager.getShader(0));//第一张提示图
		BNObject bn1=new BNObject(tip1_X,1050,700,1150,
				TextureManager.getTextures("tip2.png"),ShaderManager.getShader(0));//第二张提示图
		BNObject bn2=new BNObject(tip2_X,1050,700,1150,
				TextureManager.getTextures("tip3.png"),ShaderManager.getShader(0));//第三张提示图
		BNObject bn=new BNObject(point_X,1675,38,38,
				TextureManager.getTextures("point_white.png"),ShaderManager.getShader(0));//白点图
		synchronized(lockC)
		{
			alBNPO.get(2).remove(1);
			alBNPO.get(2).add(1,bn0);
			alBNPO.get(2).remove(2);
			alBNPO.get(2).add(2,bn1);
			alBNPO.get(2).remove(3);
			alBNPO.get(2).add(3,bn2);
			alBNPO.get(2).remove(4);
			alBNPO.get(2).add(4,bn);
		}
	}

	public void drawWinBuffer()//绘制胜利界面方法
	{
		if(isDrawWin)
		{
			isWin=true;

			ArrayList<BNObject> windata=MyFCData.getCurrentData(getAreaPercent(),625,1100,50,70);//获得当前剩余的面积
			synchronized(lockB)
			{
				for(int i=4;i<GameData.get(6).size();i++)//删除先前的数据
				{
					GameData.get(6).remove(i);
				}
				for(int i=0;i<windata.size();i++)//面积的百分比
				{
					GameData.get(6).add(windata.get(i));
				}
				if(kniftNum<10)
				{
					windata=MyFCData.getCurrentData(kniftNum,680,950,50,80);
				}else if(kniftNum>10&&kniftNum<100)
				{
					windata=MyFCData.getCurrentData(kniftNum,625,950,50,80);
				}else if(kniftNum>100)
				{
					windata=MyFCData.getCurrentData(kniftNum,580,950,50,80);
				}
				for(int i=0;i<windata.size();i++)//切割的刀数
				{
					GameData.get(6).add(windata.get(i));
				}
				if(gameTime<10)
				{
					windata=MyFCData.getCurrentData(gameTime,680,800,50,80);
				}else if(gameTime>10&&gameTime<100)
				{
					windata=MyFCData.getCurrentData(gameTime,625,800,50,80);
				}else if(gameTime>100)
				{
					windata=MyFCData.getCurrentData(gameTime,580,800,50,80);
				}
				for(int i=0;i<windata.size();i++)//游戏所用时间
				{
					GameData.get(6).add(windata.get(i));
				}
			}
		}
	}

	//MainFrame内部类=======================start==================================

	class MainTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction()){
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					//选项按钮
					if(x>Constant.ChooseButton_Left&&x<Constant.ChooseButton_Right
							&&y>Constant.ChooseButton_Up&&y<Constant.ChooseButton_Down)
					{
						BNObject bn=new BNObject(230,690,260,240,
								TextureManager.getTextures("option_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(1);
							alBNPO.get(0).add(1,bn);
						}
					}
					//开始按钮
					else if(x>Constant.StartButton_Left&&x<Constant.StartButton_Right
							&&y>Constant.StartButton_Up&&y<Constant.StartButton_Down)
					{
						BNObject bn=new BNObject(580,990,440,430,
								TextureManager.getTextures("play_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(4);
							alBNPO.get(0).add(4,bn);
						}
					}
					//退出按钮
					else if(x>Constant.ExitButton_Left&&x<Constant.ExitButton_Right
							&&y>Constant.ExitButton_Up&&y<Constant.ExitButton_Down)
					{
						BNObject bn=new BNObject(720,1420,220,220,
								TextureManager.getTextures("exit_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(3);
							alBNPO.get(0).add(3,bn);
						}
					}
					//帮助按钮
					else if(x>Constant.HelpButton_Left&&x<Constant.HelpButton_Right
							&&y>Constant.HelpButton_Up&&y<Constant.HelpButton_Down)
					{
						BNObject bn=new BNObject(145,1565,210,220,
								TextureManager.getTextures("help_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(2);
							alBNPO.get(0).add(2,bn);
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					//选项按钮
					if(x>Constant.ChooseButton_Left&&x<Constant.ChooseButton_Right
							&&y>Constant.ChooseButton_Up&&y<Constant.ChooseButton_Down)
					{
						BNObject bn=new BNObject(230,690,280,260,
								TextureManager.getTextures("option_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(1);
							alBNPO.get(0).add(1,bn);
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
						//on--on
						if(BackGroundMusic&&SoundEffect)
						{
							chooseIndex=1;//切到选关界面1
						}else if(BackGroundMusic&&(!SoundEffect))//on--off
						{
							chooseIndex=2;//切到选关界面2
						}else if((!BackGroundMusic)&&SoundEffect)//off--on
						{
							chooseIndex=3;//切到选关界面3
						}else//off--off
						{
							chooseIndex=4;//切到选关界面4
						}
						switchIndex=SwitchIndex.OptionFrame;
					}
					//开始按钮
					else if(x>Constant.StartButton_Left&&x<Constant.StartButton_Right
							&&y>Constant.StartButton_Up&&y<Constant.StartButton_Down)
					{
						BNObject bn=new BNObject(580,990,460,450,
								TextureManager.getTextures("play_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(4);
							alBNPO.get(0).add(4,bn);
						}
						switchIndex=SwitchIndex.SelectFrame;
						isDrawSnow=true;
						initSnow();//初始化雪花数据
						x=0;y=0;
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}
					//退出按钮
					else if(x>Constant.ExitButton_Left&&x<Constant.ExitButton_Right
							&&y>Constant.ExitButton_Up&&y<Constant.ExitButton_Down)
					{
						BNObject bn=new BNObject(720,1420,240,240,
								TextureManager.getTextures("exit_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(3);
							alBNPO.get(0).add(3,bn);
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
						System.exit(0);//退出游戏
					}
					//帮助按钮
					else if(x>Constant.HelpButton_Left&&x<Constant.HelpButton_Right
							&&y>Constant.HelpButton_Up&&y<Constant.HelpButton_Down)
					{
						BNObject bn=new BNObject(145,1565,230,240,
								TextureManager.getTextures("help_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(0).remove(2);
							alBNPO.get(0).add(2,bn);
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
						switchIndex=SwitchIndex.HelpFrame;
					}
					break;
			}
		}
	}

	//MainFrame内部类=======================end==================================

	//选项界面内部类==========================start==================================

	class OptionTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction()){
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					//返回
					if(x>Constant.Choose_Back_Left&&x<Constant.Choose_Back_Right
							&&y>Constant.Choose_Back_Up&&y<Constant.Choose_Back_Down)
					{
						switchIndex=SwitchIndex.MainFrame;
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}//背景音乐
					else if(x>Constant.Choose_Music_Left&&x<Constant.Choose_Music_Right
							&&y>Constant.Choose_Music_Up&&y<Constant.Choose_Music_Down)
					{
						BackGroundMusic=!BackGroundMusic;//背景音乐状态置反
						//on--on
						if(BackGroundMusic&&SoundEffect)
						{
							chooseIndex=1;//切到选关界面1
						}else if(BackGroundMusic&&(!SoundEffect))//on--off
						{
							chooseIndex=2;//切到选关界面2
						}else if((!BackGroundMusic)&&SoundEffect)//off--on
						{
							chooseIndex=3;//切到选关界面3
						}else//off--off
						{
							chooseIndex=4;//切到选关界面4
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}//声音特效
					else if(x>Constant.Choose_Sound_Left&&x<Constant.Choose_Sound_Right
							&&y>Constant.Choose_Sound_Up&&y<Constant.Choose_Sound_Down)
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						SoundEffect=!SoundEffect;//声音特效状态置反
						//on--on
						if(BackGroundMusic&&SoundEffect)
						{
							chooseIndex=1;//切到选关界面1
						}else if(BackGroundMusic&&(!SoundEffect))//on--off
						{
							chooseIndex=2;//切到选关界面2
						}else if((!BackGroundMusic)&&SoundEffect)//off--on
						{
							chooseIndex=3;//切到选关界面3
						}else//off--off
						{
							chooseIndex=4;//切到选关界面4
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					//on--on
					if(chooseIndex==1)
					{
						BNObject bn=new BNObject(545,990,600,140,
								TextureManager.getTextures("musicOn.png"),ShaderManager.getShader(0));
						BNObject bn1=new BNObject(545,1270,600,140,
								TextureManager.getTextures("soundOn.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(1).remove(1);
							alBNPO.get(1).add(1,bn);
							alBNPO.get(1).remove(2);
							alBNPO.get(1).add(2,bn1);
						}
					}else if(chooseIndex==2)
					{
						BNObject bn=new BNObject(545,990,600,140,
								TextureManager.getTextures("musicOn.png"),ShaderManager.getShader(0));
						BNObject bn1=new BNObject(545,1270,600,140,
								TextureManager.getTextures("soundOff.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(1).remove(1);
							alBNPO.get(1).add(1,bn);
							alBNPO.get(1).remove(2);
							alBNPO.get(1).add(2,bn1);
						}
					}else if(chooseIndex==3)
					{
						BNObject bn=new BNObject(545,990,600,140,
								TextureManager.getTextures("musicOff.png"),ShaderManager.getShader(0));
						BNObject bn1=new BNObject(545,1270,600,140,
								TextureManager.getTextures("soundOn.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(1).remove(1);
							alBNPO.get(1).add(1,bn);
							alBNPO.get(1).remove(2);
							alBNPO.get(1).add(2,bn1);
						}
					}else if(chooseIndex==4)
					{
						BNObject bn=new BNObject(545,990,600,140,
								TextureManager.getTextures("musicOff.png"),ShaderManager.getShader(0));
						BNObject bn1=new BNObject(545,1270,600,140,
								TextureManager.getTextures("soundOff.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(1).remove(1);
							alBNPO.get(1).add(1,bn);
							alBNPO.get(1).remove(2);
							alBNPO.get(1).add(2,bn1);
						}
					}
					break;
			}
		}
	}

	//选项界面内部类==========================end==================================

	//帮助界面内部类==============================start=========================

	class HelpTouchTask
	{
		void doTask(MotionEvent e)
		{
			boolean isLeftSliding=true;
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					break;
				case MotionEvent.ACTION_MOVE:
					float mxe=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					BNObject bn0=new BNObject(tip0_X-(x-mxe),1050,700,1150,
							TextureManager.getTextures("tip1.png"),ShaderManager.getShader(0));//第一张提示图
					BNObject bn1=new BNObject(tip1_X-(x-mxe),1050,700,1150,
							TextureManager.getTextures("tip2.png"),ShaderManager.getShader(0));//第二张提示图
					BNObject bn2=new BNObject(tip2_X-(x-mxe),1050,700,1150,
							TextureManager.getTextures("tip3.png"),ShaderManager.getShader(0));//第三张提示图
					synchronized(lockC)
					{
						alBNPO.get(2).remove(1);
						alBNPO.get(2).add(1,bn0);
						alBNPO.get(2).remove(2);
						alBNPO.get(2).add(2,bn1);
						alBNPO.get(2).remove(3);
						alBNPO.get(2).add(3,bn2);
					}
					break;
				case MotionEvent.ACTION_UP:
					float lxe=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					float lye=Constant.fromRealScreenYToStandardScreenY(e.getY());//将当前屏幕坐标转换为标准屏幕坐标
					//返回
					if(lxe>Constant.Help_Back_Left&&lxe<Constant.Help_Back_Right
							&&lye>Constant.Help_Back_Up&&lye<Constant.Help_Back_Down)
					{
						switchIndex=SwitchIndex.MainFrame;
					}
					//左滑
					if(x-lxe>350)
					{
						isLeftSliding=true;
						if(tipIndex==1)//当前是第一张提示图
						{
							tipIndex=2;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==2)//当前是第二张提示图
						{
							tipIndex=3;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==3)//当前是第三张提示图
						{
							tipIndex=1;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}
					}else
					{
						isLeftSliding=true;
						if(tipIndex==1)//当前是第一张提示图
						{
							tipIndex=1;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==2)//当前是第二张提示图
						{
							tipIndex=2;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==3)//当前是第三张提示图
						{
							tipIndex=3;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}
					}
					if(lxe-x>350)//右滑
					{
						isLeftSliding=false;
						if(tipIndex==1)//当前是第一张提示图
						{
							tipIndex=3;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==2)//当前是第二张提示图
						{
							tipIndex=1;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==3)//当前是第三张提示图
						{
							tipIndex=2;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}
					}else
					{
						isLeftSliding=false;
						if(tipIndex==1)//当前是第一张提示图
						{
							tipIndex=1;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==2)//当前是第二张提示图
						{
							tipIndex=2;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}else if(tipIndex==3)//当前是第三张提示图
						{
							tipIndex=3;
							HelpTip(tipIndex,isLeftSliding);//切换图片
						}
					}
					break;
			}
		}
	}

	//帮助界面内部类==============================end=========================

	//选关界面内部类============================start==========================

	class SelectTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					if(x>=LevelView_Back_Left_X&&x<=LevelView_Back_Right_X&&
							y>=LevelView_Back_Top_Y&&y<=LevelView_Back_Bottom_Y)//点击返回按钮 则回到首界面
					{
						isDrawSnow=false;
						switchIndex=SwitchIndex.MainFrame;
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}
					if(x>=LevelView_Series1_Left_X&&x<=LevelView_Series1_Right_X&&
							y>=LevelView_Series1_Top_Y&&y<=LevelView_Series1_Bottom_Y)//如果选择了第一小关
					{
						BNObject bn=new BNObject(400,600,650,300,
								TextureManager.getTextures("set1_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(3).remove(1);
							alBNPO.get(3).add(1,bn);
						}
					}else if(x>=LevelView_Series2_Left_X&&x<=LevelView_Series2_Right_X&&
							y>=LevelView_Series2_Top_Y&&y<=LevelView_Series2_Bottom_Y)//如果选择了第二小关
					{
						BNObject bn=new BNObject(650,1000,650,300,
								TextureManager.getTextures("set2_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(3).remove(2);
							alBNPO.get(3).add(2,bn);
						}
					}else if(x>=LevelView_Series3_Left_X&&x<=LevelView_Series3_Right_X&&
							y>=LevelView_Series3_Top_Y&&y<=LevelView_Series3_Bottom_Y)//如果选择了第三小关
					{
						BNObject bn=new BNObject(400,1400,650,300,
								TextureManager.getTextures("set3_b.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(3).remove(3);
							alBNPO.get(3).add(3,bn);
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					if(x>=LevelView_Series1_Left_X&&x<=LevelView_Series1_Right_X&&
							y>=LevelView_Series1_Top_Y&&y<=LevelView_Series1_Bottom_Y)//如果选择了第一小关
					{
						isDrawSnow=false;
						switchIndex=SwitchIndex.FirstLevelFrame;
						BNObject bn=new BNObject(400,600,650,300,
								TextureManager.getTextures("set1_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(3).remove(1);
							alBNPO.get(3).add(1,bn);
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}else if(x>=LevelView_Series2_Left_X&&x<=LevelView_Series2_Right_X&&
							y>=LevelView_Series2_Top_Y&&y<=LevelView_Series2_Bottom_Y)//如果选择了第二小关
					{
						isDrawSnow=false;
						switchIndex=SwitchIndex.SecondLevelFrame;
						BNObject bn=new BNObject(650,1000,650,300,
								TextureManager.getTextures("set2_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(3).remove(2);
							alBNPO.get(3).add(2,bn);
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}else if(x>=LevelView_Series3_Left_X&&x<=LevelView_Series3_Right_X&&
							y>=LevelView_Series3_Top_Y&&y<=LevelView_Series3_Bottom_Y)//如果选择了第三小关
					{
						isDrawSnow=false;
						switchIndex=SwitchIndex.ThirdLevelFrame;
						BNObject bn=new BNObject(400,1400,650,300,
								TextureManager.getTextures("set3_a.png"),ShaderManager.getShader(0));
						synchronized(lockC)
						{
							alBNPO.get(3).remove(3);
							alBNPO.get(3).add(3,bn);
						}
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}
					break;
			}
		}
	}

	//选关界面内部类============================end==========================

	//第一关卡界面内部类=========================start========================

	class FirstLevelTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					if(x>=LevelView_Back_Left_X&&x<=LevelView_Back_Right_X&&
							y>=LevelView_Back_Top_Y&&y<=LevelView_Back_Bottom_Y)//点击返回按钮 则回到选关界面
					{
						isDrawSnow=true;
						initSnow();//初始化雪花数据
						switchIndex=SwitchIndex.SelectFrame;
						setPressSoundEffect("switchpane.ogg");//播放按键音
					}else if(x>=LevelView_PickUp1_1_Left_X&&x<=LevelView_PickUp1_1_Right_X&&
							y>=LevelView_PickUp1_1_Top_Y&&y<=LevelView_PickUp1_1_Bottom_Y)//选择第一大关的第一小关
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						CheckpointIndex=0;//1-1
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						switchIndex=SwitchIndex.GameViewFrame;
					}else if(x>=LevelView_PickUp1_2_Left_X&&x<=LevelView_PickUp1_2_Right_X&&
							y>=LevelView_PickUp1_2_Top_Y&&y<=LevelView_PickUp1_2_Bottom_Y)//选择第一大关的第二小关
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						CheckpointIndex=1;//1-2
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						switchIndex=SwitchIndex.GameViewFrame;
					}
					break;
			}
		}
	}

	//第一关卡界面内部类=========================start========================

	//第二关卡界面内部类=========================start========================

	class SecondLevelTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					if(x>=LevelView_Back_Left_X&&x<=LevelView_Back_Right_X&&
							y>=LevelView_Back_Top_Y&&y<=LevelView_Back_Bottom_Y)//点击返回按钮 则回到选关界面
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						isDrawSnow=true;
						initSnow();//初始化雪花数据
						switchIndex=SwitchIndex.SelectFrame;
					}else if(x>=LevelView_PickUp2_1_Left_X&&x<=LevelView_PickUp2_1_Right_X&&
							y>=LevelView_PickUp2_1_Top_Y&&y<=LevelView_PickUp2_1_Bottom_Y)//选择第二大关的第一小关
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						CheckpointIndex=2;//2-1
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						switchIndex=SwitchIndex.GameViewFrame;
					}else if(x>=LevelView_PickUp2_2_Left_X&&x<=LevelView_PickUp2_2_Right_X&&
							y>=LevelView_PickUp2_2_Top_Y&&y<=LevelView_PickUp2_2_Bottom_Y)//选择第二大关的第二小关
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						CheckpointIndex=3;//2-2
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						switchIndex=SwitchIndex.GameViewFrame;
					}
					break;
			}
		}
	}
	//第二关卡界面内部类=========================end========================

	//第三关卡界面内部类=========================start======================

	class ThirdLevelTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					if(x>=LevelView_Back_Left_X&&x<=LevelView_Back_Right_X&&
							y>=LevelView_Back_Top_Y&&y<=LevelView_Back_Bottom_Y)//点击返回按钮 则回到选关界面
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						isDrawSnow=true;
						initSnow();//初始化雪花数据
						switchIndex=SwitchIndex.SelectFrame;
					}else if(x>=LevelView_PickUp3_1_Left_X&&x<=LevelView_PickUp3_1_Right_X&&
							y>=LevelView_PickUp3_1_Top_Y&&y<=LevelView_PickUp3_1_Bottom_Y)//选择第三大关的第一小关
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						CheckpointIndex=4;//3-1
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						switchIndex=SwitchIndex.GameViewFrame;
					}else if(x>=LevelView_PickUp3_2_Left_X&&x<=LevelView_PickUp3_2_Right_X&&
							y>=LevelView_PickUp3_2_Top_Y&&y<=LevelView_PickUp3_2_Bottom_Y)//选择第三大关的第二小关
					{
						setPressSoundEffect("switchpane.ogg");//播放按键音
						CheckpointIndex=5;//3-2
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						switchIndex=SwitchIndex.GameViewFrame;
					}
					break;
			}
		}
	}
	//第三关卡界面内部类=========================end========================

	//游戏界面内部类=======================start========================
	class GameViewTouchTask
	{
		void doTask(MotionEvent e)
		{
			switch(e.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					x=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					y=Constant.fromRealScreenYToStandardScreenY(e.getY());
					if(isPause&&x>Constant.ChooseLevel_Left&&x<Constant.ChooseLevel_Right&&y>Constant.ChooseLevel_Top&&y<Constant.ChooseLevel_Buttom)
					{//暂停界面 选择关卡
						BNObject object=MyFCData.ChangeLable(300, 1000,200, 200, "guanQia_b.png");
						synchronized(lockB)
						{
							GameData.get(5).remove(1);
							GameData.get(5).add(1,object);
						}
					}
					else if(isPause&&x>Constant.ReStart_Left&&x<Constant.ReStart_Right&&y>Constant.ReStart_Top&&y<Constant.ReStart_Buttom)
					{//暂停界面 重玩
						BNObject object=MyFCData.ChangeLable(550,1000,200,200, "replay_b.png");
						synchronized(lockB)
						{
							GameData.get(5).remove(2);
							GameData.get(5).add(2,object);
						}
					}
					else if(isPause&&x>Constant.Continue_Left&&x<Constant.Continue_Right&&y>Constant.Continue_Top&&y<Constant.Continue_Buttom)
					{//暂停界面 继续
						BNObject object=MyFCData.ChangeLable(800,1000,200,200, "resume_b.png");
						synchronized(lockB)
						{
							GameData.get(5).remove(3);
							GameData.get(5).add(3,object);
						}
					}
					else if(isWin&&x>Constant.WinChooseLevel_Left&&x<Constant.WinChooseLevel_Right&&y>Constant.WinChooseLevel_Top&&y<Constant.WinChooseLevel_Buttom)
					{//胜利界面 选择关卡
						BNObject object=MyFCData.ChangeLable(300, 1400,200, 200, "guanQia_b.png");
						synchronized(lockB)
						{
							GameData.get(6).remove(1);
							GameData.get(6).add(1,object);
						}
					}
					else if(isWin&&x>Constant.WinReStart_Left&&x<Constant.WinReStart_Right&&y>Constant.WinReStart_Top&&y<Constant.WinReStart_Buttom)
					{//胜利界面  重玩
						BNObject object=MyFCData.ChangeLable(550,1400,200,200, "replay_b.png");
						synchronized(lockB)
						{
							GameData.get(6).remove(2);
							GameData.get(6).add(2,object);
						}
					}
					else if(isWin&&x>Constant.WinNext_Left&&x<Constant.WinNext_Right&&y>Constant.WinNext_Top&&y<Constant.WinNext_Buttom)
					{//胜利界面  下一关
						BNObject object=MyFCData.ChangeLable(800,1400,200,200, "next_b.png");

						synchronized(lockB)
						{
							GameData.get(6).remove(3);
							GameData.get(6).add(3,object);
						}
					}
					break;
				case MotionEvent.ACTION_MOVE:
					Knifefire=null;
					isCutRigid=false;
					if(!isPause&&!isWin&&!isCut)
					{
						float mxe=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
						float mye=Constant.fromRealScreenYToStandardScreenY(e.getY());
						if(Math.abs(x-mxe)<=10||Math.abs(y-mye)<=10)//不绘制
						{
							isLine=false;//不画线
							isCutRigid=false;
						}else
						{
							line=new BNObject(x,y,mxe,mye,
									TextureManager.getTextures("line.png"),//纹理图片名称
									ShaderManager.getShader(0),true,0);
							isLine=true;//绘制切割线
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					Knifefire=null;
					isCutRigid=false;//没有切到刚边
					line=null;//删除切割线的对象
					isLine=false;//停止绘制线
					float lxe=Constant.fromRealScreenXToStandardScreenX(e.getX());//将当前屏幕坐标转换为标准屏幕坐标
					float lye=Constant.fromRealScreenYToStandardScreenY(e.getY());

					if(!isCutUtil.isCutPolygon(MySurfaceView.this,GameData.get(2).get(0).cp, x, y, lxe, lye)&&!isCut)
					{
						if(isCutRigid)//是否切到了刚边
						{
							synchronized(lockA)
							{
								Knifefire=new BNObject(intersectPoint[0],intersectPoint[1],150,150,
										TextureManager.getTextures("spark_1.png"),//纹理图片名称
										ShaderManager.getShader(0));//创建火花对象
								isLine=false;//绘制切割线
							}
							setPressSoundEffect("peng.ogg");
						}
					}

					if(!isCut&&(pauseOne==0)&&lxe>Constant.PauseLable_Left&&lxe<Constant.PauseLable_Right&&lye>Constant.PauseLable_Top&&lye<Constant.PauseLable_Buttom)
					{//绘制暂停界面
						pauseOne=1;//暂停界面只允许按一次
						setPressSoundEffect("click.ogg");
						isPause=true;
						isFirstPause=true;//暂停界面旋转出来
						synchronized(lockA)
						{
							alFlyPolygon.clear();//清空飞走的多边形列表
						}
						pauseTime=System.currentTimeMillis();
					}
					if(isPause&&lxe>Constant.ChooseLevel_Left&&lxe<Constant.ChooseLevel_Right&&lye>Constant.ChooseLevel_Top&&lye<Constant.ChooseLevel_Buttom)
					{//暂停界面  选择关卡
						pauseOne=0;//允许进入暂停界面
						isDrawSnow=true;
						initSnow();//初始化雪花数据
						setPressSoundEffect("click.ogg");
						switchIndex=SwitchIndex.SelectFrame;
						BNObject object=MyFCData.ChangeLable(300, 1000,200, 200, "guanQia_a.png");
						synchronized(lockB)
						{
							GameData.get(5).remove(1);
							GameData.get(5).add(1,object);
						}
					}
					else if(isPause&&lxe>Constant.ReStart_Left&&lxe<Constant.ReStart_Right&&lye>Constant.ReStart_Top&&lye<Constant.ReStart_Buttom)
					{//暂停界面  重玩
						pauseOne=0;//允许进入暂停界面
						setPressSoundEffect("click.ogg");
						switchIndex=SwitchIndex.GameViewFrame;
						tryAgain=true;//重玩
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						BNObject object=MyFCData.ChangeLable(550,1000,200,200, "replay_a.png");
						synchronized(lockB)
						{
							GameData.get(5).remove(2);
							GameData.get(5).add(2,object);
						}
					}
					else if(isPause&&lxe>Constant.Continue_Left&&lxe<Constant.Continue_Right&&lye>Constant.Continue_Top&&lye<Constant.Continue_Buttom)
					{//暂停界面  继续
						pauseOne=0;//允许进入暂停界面
						setPressSoundEffect("click.ogg");
						if(pauseTime<System.currentTimeMillis())
						{
							pauseTime=System.currentTimeMillis()-pauseTime;
						}
						isPause=false;
						switchIndex=SwitchIndex.GameViewFrame;
						BNObject object=MyFCData.ChangeLable(800,1000,200,200, "resume_a.png");
						synchronized(lockB)
						{
							GameData.get(5).remove(3);
							GameData.get(5).add(3,object);
						}
					}
					else if(isWin&&lxe>Constant.WinChooseLevel_Left&&lxe<Constant.WinChooseLevel_Right&&lye>Constant.WinChooseLevel_Top&&lye<Constant.WinChooseLevel_Buttom)
					{//胜利界面  选择关卡
						isDrawSnow=true;
						initSnow();//初始化雪花数据
						setPressSoundEffect("click.ogg");
						switchIndex=SwitchIndex.SelectFrame;
						BNObject object=MyFCData.ChangeLable(300, 1400,200, 200, "guanQia_a.png");
						synchronized(lockB)
						{
							GameData.get(6).remove(1);
							GameData.get(6).add(1,object);
						}
					}
					else if(isWin&&lxe>Constant.WinReStart_Left&&lxe<Constant.WinReStart_Right&&lye>Constant.WinReStart_Top&&lye<Constant.WinReStart_Buttom)
					{//胜利界面  重玩
						setPressSoundEffect("click.ogg");
						switchIndex=SwitchIndex.GameViewFrame;
						tryAgain=true;//重玩
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						BNObject object=MyFCData.ChangeLable(550,1400,200,200, "replay_a.png");
						synchronized(lockB)
						{
							GameData.get(6).remove(2);
							GameData.get(6).add(2,object);
						}
					}
					else if(isWin&&lxe>Constant.WinNext_Left&&lxe<Constant.WinNext_Right&&lye>Constant.WinNext_Top&&lye<Constant.WinNext_Buttom)
					{//胜利界面  下一关
						if(CheckpointIndex>=5)
						{
							CheckpointIndex=0;
						}else
						{
							CheckpointIndex++;
						}
						isLine=true;
						initGameView();//初始化游戏界面
						gameST=System.currentTimeMillis();
						BNObject object=MyFCData.ChangeLable(800,1400,200,200, "next_a.png");
						synchronized(lockB)
						{
							GameData.get(6).remove(3);
							GameData.get(6).add(3,object);
						}
					}
					if(!isPause&&!isWin&&!isCut&&x!=lxe&&y!=lye)
					{
						//判断是否划到了多边形
						if(!isCutUtil.isCutPolygon(MySurfaceView.this,GameData.get(2).get(0).cp, x, y, lxe, lye))
						{
							return;
						}
						line=null;
						line=new BNObject(x,y,lxe,lye,
								TextureManager.getTextures("light.png"),//纹理图片名称
								ShaderManager.getShader(0),true,1);
						isLight=true;//开始绘制刀光
						kniftNum++;//切到木块的刀数
						//获得切分后的经过合并等操作的多边形列表
						ArrayList<C2DPolygon> lastPolygons=isCutUtil.getCutPolysArrayList(MySurfaceView.this,GameData.get(2), x, y, lxe, lye);
						//判断手划过的区域需要去掉的部分并创建包围框
						C2DPoint[] pointLocation=MyFCData.getBallPosition(alBNBall);//获得各个球的位置
						//判断切割线的距离是否小于球的半径===================start==========================
						for(int i=0;i<pointLocation.length;i++)
						{
							//切割线的距离如果小于球的半径
							if(GeometryConstant.lengthPointToLine((float)pointLocation[i].x,(float)pointLocation[i].y, x, y, lxe, lye)<=50)
							{
								setPressSoundEffect("gamefail.ogg");//播放游戏失败的声音
								tryAgain=true;
								initGameView();//初始化游戏界面
								gameST=System.currentTimeMillis();
								return;//直接返回
							}
						}
						//判断切割线的距离是否小于球的半径===================end==========================
						synchronized(lockA)
						{
							alFlyPolygon.clear();//将飞走的物体列表清空
						}
						boolean isPlayWin=false;//播放成功切到木块

						for(C2DPolygon cp:lastPolygons)//遍历C2DPolygon对象
						{
							int kk=0;
							for(int i=0;i<pointLocation.length;i++)
							{
								//该多边形内是否有球
								if(cp.Contains(pointLocation[i]))
								{
									kk++;
								}
							}
							if(kk==alBNBall.size())//如果该多边形区域内有两个球
							{
								isPlayWin=true;
								AreaSize=(float)cp.GetArea();//获得多边形的面积

								cpData=GeoLibUtil.fromC2DPolygonToVData(cp);
								isJudgePolygon = true;//允许删除或者创建包围框
								isJudgeBall=false;//不允许删除或者创建球

								GeometryConstant.judgeDirection(cp,x, y, lxe, lye);//判断多边形木块飞走的方向
								ArrayList<BNObject> bnObject=new ArrayList<BNObject>();
								BNPolyObject bnpo=new BNPolyObject//创建多边形背景图
										(
												MySurfaceView.this,
												TextureManager.getTextures(MyFCData.gamePicName[CheckpointIndex]),//纹理图片名称
												ShaderManager.getShader(0),//程序ID
												GeoLibUtil.fromC2DPolygonToVData(cp),//顶点数据
												1080,//原始图形的宽
												1920,//原始图形的高
												0,
												0
										);
								bnObject.add(bnpo);

								GameData.remove(2);//清空需被绘制的BNPolyObject对象列表
								GameData.put(2, bnObject);

								bnObject=new ArrayList<BNObject>();
								bnObject=MyFCData.getCurrentData(getAreaPercent(),850,80,40,50);//获得当前剩余的面积
								GameData.remove(3);//清空显示剩余面积数据的列表
								GameData.put(3, bnObject);//添加进列表

								//获得斧头道具===============start====================
								if(CheckpointIndex>=4)
								{
									if(kniftNum==1)
									{
										beforeArea=100;
									}
									if(beforeArea-getAreaPercent()>25)
									{
										System.out.println("25==========================");
										beforeKnifeNum=kniftNum;
										axe=new BNObject(540,960,200,200,TextureManager.getTextures("tiebuff.png"),
												ShaderManager.getShader(0));
										for(int i=0;i<MyFCData.dataBool[CheckpointIndex].length;i++)
										{
											MyFCData.dataBool[CheckpointIndex][i]=true;
										}
										tieCount=0;
										isOwnAxe=true;
									}
									beforeArea=getAreaPercent();
									if(kniftNum-beforeKnifeNum==1)
									{
										isOwnAxe=false;
										isCutOne=true;
										if(CheckpointIndex==4)
										{
											for(int i=0;i<MyFCData.dataBool[CheckpointIndex].length;i++)
											{
												if(i==0||i==3||i==5)
												{
													MyFCData.dataBool[CheckpointIndex][i]=false;
												}else
												{
													MyFCData.dataBool[CheckpointIndex][i]=true;
												}
											}
										}else
										{
											for(int i=0;i<MyFCData.dataBool[CheckpointIndex].length;i++)
											{
												if(i==1||i==2||i==5||i==6)
												{
													MyFCData.dataBool[CheckpointIndex][i]=false;
												}else
												{
													MyFCData.dataBool[CheckpointIndex][i]=true;
												}
											}
										}
									}
								}
								//获得斧头道具===============end====================


								int goal=Integer.parseInt(MyFCData.goal[CheckpointIndex]);//目标面积
								if(goal>=getAreaPercent())//胜利
								{
									isCut=true;
									gameTime=(int)((System.currentTimeMillis()-gameST-pauseTime)/1000);//获得游戏时间
									if(gameTime<1)//如果时间小于1秒  则默认为1秒
									{
										gameTime=1;
									}
									//游戏胜利界面 使球缓慢停下来======================start=====================
									deleteBall();
									float[][] ballBaseData={
											{(float)pointLocation[0].x,(float)pointLocation[0].y,50,2.5f,0.8f,0.35f,30,50},
											{(float)pointLocation[1].x,(float)pointLocation[1].y,50,1f,0.8f,0.35f,20,40}
									};
									synchronized(lockD)
									{
										alBNBall.clear();
										addBall(ballBaseData);
									}
									//游戏胜利界面 使球缓慢停下来======================end=====================
									setPressSoundEffect("gamesucc.ogg");
								}
							}else if(kk==0)//木块飞走
							{
								BNPolyObject bnfly=new BNPolyObject//创建多边形背景图
										(
												MySurfaceView.this,
												TextureManager.getTextures(MyFCData.gamePicName[CheckpointIndex]),//纹理图片名称
												ShaderManager.getShader(2),//程序ID
												GeoLibUtil.fromC2DPolygonToVData(cp),//顶点数据
												1080,//原始图形的宽
												1920,//原始图形的高
												0.005f,
												-0.005f
										);
								synchronized(lockA)
								{
									alFlyPolygon.add(bnfly);
								}
								continue;
							}else if(kk==1)
							{
								twinkle=true;//画闪烁线
								lineIndex=0;
								line=new BNObject(x,y,lxe,lye,
										TextureManager.getTextures("line.png"),//纹理图片名称
										ShaderManager.getShader(0),true,0);
								isLine=true;//绘制切割线
							}
						}
						if(isPlayWin)//如果成功切到木块 则播放成功的声音
						{
							setPressSoundEffect("cut.ogg");
						}
						//判断手划过的区域需要去掉的部分并创建包围框==============end====================
					}
					isLight=false;//停止绘制刀光
					break;
			}
		}
	}
	//游戏界面内部类==============================end====================================


	private class SceneRenderer implements Renderer
	{
		public void onDrawFrame(GL10 gl)
		{
			drawWinBuffer();//对胜利界面进行缓冲
			//清除深度缓冲与颜色缓冲
			GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
			//初始化界面
			if(!isStartGame)
			{
				initArrayList();
			}
			if(BackGroundMusic&&!(switchIndex.equals(SwitchIndex.GameViewFrame))&&(!isOpen))
			{
				activity.sm.StartBackGroundSound();//开启背景音乐
				isOpen=true;
			}
			if(isOpen&&(switchIndex.equals(SwitchIndex.GameViewFrame)||(!BackGroundMusic)))
			{
				activity.sm.EndBackGroundSound();//关闭背景音乐
				isOpen=false;
			}
			if(switchIndex.equals(SwitchIndex.GameViewFrame))
			{//绘制游戏界面
				drawGameView();
			}else{
				//绘制开始界面或者选关界面
				drawFirstView();
			}
			if(isCutRigid&&Knifefire!=null)//绘制火花
			{
				drawFire();
			}
			if(isOwnAxe)//绘制斧头
			{
				if(tieCount>150)
				{
					isCut=false;//允许划多边形
					axe=null;
					axe=new BNObject(980,500,120,120,TextureManager.getTextures("tiebuff.png"),
							ShaderManager.getShader(0));
					axe.drawSelf();
				}else
				{
					isCut=true;//不允许划多边形
					tieCount++;
					MatrixState.pushMatrix();//保护场景
					MatrixState.translate(tieCount*0.003f, tieCount*0.003f, 0);
					MatrixState.scale((1f-tieCount*0.002f), (1f-tieCount*0.002f), 1);
					axe.drawSelf();
					MatrixState.popMatrix();//保护场景
				}
			}
			if(!isOwnAxe&&isCutOne)//使用了斧头
			{
				tieCount++;
				MatrixState.pushMatrix();//保护场景
				MatrixState.translate(-tieCount*0.0005f, -tieCount*0.0005f, 0);
				MatrixState.scale(1+tieCount*0.001f, 1+tieCount*0.001f, 1);
				if (axe!=null){
					axe.drawSelf();
				}
				MatrixState.popMatrix();//保护场景
				if(tieCount>300)
				{
					isCutOne=false;
				}
			}
		}
		public void onSurfaceChanged(GL10 gl, int width, int height){
			//设置视窗大小及位置
			GLES20.glViewport
					(
							(int)Constant.ssr.lucX,//x
							(int)Constant.ssr.lucY,//y
							(int)(Constant.StandardScreenWidth*Constant.ssr.ratio),//width
							(int)(Constant.StandardScreenHeight*Constant.ssr.ratio)//height
					);
			//计算GLSurfaceView的宽高比
			float ratio = (float) width / height;
			//调用此方法计算产生透视投影矩阵
			MatrixState.setProjectOrtho(-ratio, ratio, -1, 1, 1, 100);
			//调用此方法产生摄像机9参数位置矩阵
			MatrixState.setCamera(0,0,3,0f,0f,0f,0f,1.0f,0.0f);
			//初始化变换矩阵
			MatrixState.setInitStack();
			//初始化光源位置
			MatrixState.setLightLocation(0, 15, 0);
		}
		public void onSurfaceCreated(GL10 gl, EGLConfig config)
		{
			Vec2 gravity = new Vec2(0.0f,0.0f);
			world = new World(gravity);//创建World类对象
			world.setAllowSleep(true);//允许静止物体休眠
			//给世界添加碰撞过滤相关类
			world.setContactFilter(new MyContactFilter(MySurfaceView.this));
			//初始化着色器
			ShaderManager.loadingShader(MySurfaceView.this);
			//设置屏幕背景色RGBA
			GLES20.glClearColor(0,0,0, 0);
			if(!switchIndex.equals(SwitchIndex.GameViewFrame))
			{
				activity.sm.StartBackGroundSound();
			}
		}
	}
	//初始化各种界面
	int step=1;
	public void initArrayList()
	{
		if(step==8)
		{
			initStepEight();//火花图片
			isStartGame=true;
			return;
		}
		else if(step==1)
		{
			initStepOne();//0首界面
			step++;
		}else if(step==2)
		{
			initStepTwo();//1选项界面--on--on
			step++;
		}else if(step==3)
		{
			initStepThree();//2帮助界面--第1个
			step++;
		}else if(step==4)
		{
			initStepFour();//第一大关选关界面
			step++;
		}else if(step==5)
		{
			initStepFive();//第一小关选关界面
			step++;
		}else if(step==6)
		{
			initStepSix();//第二小关选关界面
			step++;
		}else if(step==7)
		{
			initStepSeven();//第三小关选关界面
			step++;
		}
	}
	//初始化各个界面的方法
	ArrayList<BNObject> tempArrayList=new ArrayList<BNObject>();
	BNView bn;
	//首界面
	public void initStepOne()
	{
		TextureManager.loadingTexture(MySurfaceView.this,0,9);
		//0首界面
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("bg_01.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(230,690,280,260,
				TextureManager.getTextures("option_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(145,1565,230,240,
				TextureManager.getTextures("help_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(720,1420,240,240,
				TextureManager.getTextures("exit_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(580,990,460,450,
				TextureManager.getTextures("play_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(0, tempArrayList);
	}
	//选项界面--on--on
	public void initStepTwo()
	{
		TextureManager.loadingTexture(MySurfaceView.this,9,3);
		TextureManager.loadingTexture(MySurfaceView.this,30,2);
		//1选项界面--on--on
		tempArrayList=new ArrayList<BNObject>();
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("choose.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(545,990,600,140,
				TextureManager.getTextures("musicOn.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(545,1270,600,140,
				TextureManager.getTextures("soundOn.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(1, tempArrayList);
	}
	//帮助界面--第1个
	public void initStepThree()
	{
		TextureManager.loadingTexture(MySurfaceView.this,12,3);
		TextureManager.loadingTexture(MySurfaceView.this,32,2);
		//2帮助界面--第1个
		tempArrayList=new ArrayList<BNObject>();
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("help.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(540,1050,700,1150,
				TextureManager.getTextures("tip1.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		//测试用==========================start===============================
		bn=new BNView(1240,1050,700,1150,
				TextureManager.getTextures("tip2.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());

		bn=new BNView(1940,1050,700,1150,
				TextureManager.getTextures("tip3.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());

		//测试用==========================end===============================
		bn=new BNView(430,1675,38,38,
				TextureManager.getTextures("point_white.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(2, tempArrayList);
	}
	//第一大关选关界面
	public void initStepFour()
	{
		TextureManager.loadingTexture(MySurfaceView.this,15,6);
		tempArrayList=new ArrayList<BNObject>();
		//3加载第一大关选关界面的内容===================start==========================
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("level_bg.jpg"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(400,600,650,300,
				TextureManager.getTextures("set1_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(650,1000,650,300,
				TextureManager.getTextures("set2_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(400,1400,650,300,
				TextureManager.getTextures("set3_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(900,150,200,150,
				TextureManager.getTextures("back.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(3, tempArrayList);
		//加载第一大关选关界面的内容===================end==========================
	}
	//第一小关选关界面
	public void initStepFive()
	{
		TextureManager.loadingTexture(MySurfaceView.this,21,3);
		tempArrayList=new ArrayList<BNObject>();
		//4加载第一小关选关界面的内容===================start==========================
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("set1-2.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(400,700,250,400,
				TextureManager.getTextures("s_01_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(640,1230,250,400,
				TextureManager.getTextures("s_02_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(900,150,200,150,
				TextureManager.getTextures("back.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(4, tempArrayList);
		//加载第一小关选关界面的内容===================end==========================
	}
	//第二小关选关界面
	public void initStepSix()
	{
		TextureManager.loadingTexture(MySurfaceView.this,24,3);
		tempArrayList=new ArrayList<BNObject>();
		//5加载第二小关选关界面的内容===================start==========================
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("set2-2.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(670,800,300,480,
				TextureManager.getTextures("s_03_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(380,1380,250,400,
				TextureManager.getTextures("s_04_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(900,150,200,150,
				TextureManager.getTextures("back.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(5, tempArrayList);
		//加载第二小关选关界面的内容===================end==========================
	}
	//第三小关选关界面
	public void initStepSeven()
	{
		TextureManager.loadingTexture(MySurfaceView.this,27,3);
		tempArrayList=new ArrayList<BNObject>();
		//6加载第三小关选关界面的内容===================start==========================
		bn=new BNView(540,960,1080,1920,
				TextureManager.getTextures("set3-2.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(400,800,300,480,
				TextureManager.getTextures("s_05_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(680,1400,250,400,
				TextureManager.getTextures("s_06_a.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		bn=new BNView(900,150,200,150,
				TextureManager.getTextures("back.png"),ShaderManager.getShader(0));
		tempArrayList.add(bn.getBNObject());
		alBNPO.add(6, tempArrayList);
		//加载第三小关选关界面的内容===================end==========================
	}
	//火花图片
	public void initStepEight()
	{
		TextureManager.loadingTexture(MySurfaceView.this,34,41);
		for(int i=0;i<=12;i++)
		{
			FireID[i]=TextureManager.getTextures("spark_"+i+".png");
		}
	}
	//绘制火花
	public void drawFire()
	{
		synchronized(lockA)
		{
			Knifefire.drawSelf(FireID[tempIndex]);//绘制火花
			tempIndex++;
		}
		if(tempIndex==13)//绘制最后一张图片时
		{
			tempIndex=0;
			isCutRigid=false;//是否切到刚边的标志位设为false
			Knifefire=null;
		}
	}
	//绘制胜利界面
	public void drawWinView()
	{
		synchronized(lockB)
		{
			for(BNObject win:GameData.get(6))//绘制胜利界面
			{
				win.drawSelf();
			}
		}
	}
	//绘制开始界面或者选关界面
	public void drawFirstView()
	{
		synchronized(lockC)
		{
			if(switchIndex.equals(SwitchIndex.HelpFrame))
			{
				for(int i=0;i<alBNPO.get(switchIndex.ordinal()).size();i++)
				{
					if(i==1||i==2||i==3)
					{
						GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
						GLES20.glScissor(
								(int)Constant.fromStandardScreenXToRealScreenX(190),
								(int)Constant.fromStandardScreenXToRealScreenX(300),
								(int)Constant.fromStandardScreenSizeToRealScreenSize(700),
								(int)Constant.fromStandardScreenSizeToRealScreenSize(1150)
						);
						alBNPO.get(switchIndex.ordinal()).get(i).drawSelf();
					}else
					{
						GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
						alBNPO.get(switchIndex.ordinal()).get(i).drawSelf();
					}
				}
			}else
			{
				for(BNObject bn:alBNPO.get(switchIndex.ordinal()))
				{
					bn.drawSelf();
				}
			}
		}
		if(isDrawSnow)//绘制雪花
		{
			for(int i=0;i<fps.size();i++)
			{
				fps.get(i).drawSelf();
			}
		}
	}
	//绘制游戏界面
	public void drawGameView()
	{
		if(tryAgain)//重玩
		{
			MatrixState.pushMatrix();//保护场景
			isPush=true;//push置为true
			MatrixState.scale(0.02f*isAgain,0.02f*isAgain, 1);//缩放
			isAgain++;
		}
		synchronized(lockB)
		{
			for(int i=1;i<=4;i++)
			{
				if(GameData.get(i)!=null)
				{
					for(BNObject data:GameData.get(i))//遍历游戏界面的数据对象
					{
						data.drawSelf();//绘制
					}
				}
			}
		}
		synchronized(lockD)
		{
			for(BNObject ball:alBNBall)//遍历绘制球列表
			{
				ball.drawSelf();//绘制球
				if(Math.abs(ball.body.getLinearVelocity().x)<4f&&Math.abs(ball.body.getLinearVelocity().y)<4f)
				{
					isDrawWin=true;
				}else
				{
					isDrawWin=false;
				}
			}
		}
		synchronized(lockA)
		{
			for(BNObject fly:alFlyPolygon)
			{
				fly.drawSelf(1.0f);//飞走的木块
			}
		}
		if((isLine||isLight)&&line!=null)//绘制切割线
		{
			drawCutLine();
		}
		if(isPause)//绘制暂停界面
		{
			drawPauseView();
		}
		if(isDrawWin&&isWin)//绘制胜利界面
		{
			drawWinView();
		}
		isWorldStep=worldStep();//一直进行判断是否进行模拟
		if(isWorldStep)//允许进行模拟
		{
			if(!isPause&&!isWin)//不处于暂停界面或者胜利界面
			{
				world.step(TIME_STEP, Vec_ITERA,POSITON_ITERA);//开始模拟
			}
		}else
		{
			deletePolygon();//在物理世界中删除包围框
			addPolygon(cpData);//在物理世界中添加包围框
			if(isJudgeBall)
			{
				deleteBall();//在物理世界中删除球
				addBall(MyFCData.ballData);//在物理世界中添加球
				isJudgeBall=false;//将标志位置为false
			}
			isJudgePolygon=false;//将标志位置为false
		}
		if(isPush)//如果push的话
		{
			MatrixState.popMatrix();//必须pop
			if(isAgain>50)
			{
				isAgain=0;
				tryAgain=false;
			}
			isPush=false;
		}

	}
	public void drawCutLine()
	{//绘制切割线
		if(twinkle)
		{
			if(lineIndex%17==0)
			{
				line.drawSelf();//绘制线和刀光
			}
			if(lineIndex==612)
			{
				isLine=false;
				twinkle=false;
			}
			lineIndex=lineIndex+18;
		}else
		{
			line.drawSelf();//绘制线和刀光
		}
	}
	//绘制暂停界面
	public void drawPauseView()
	{
		synchronized(lockB)
		{
			if(isFirstPause)//旋转出现暂停界面
			{
				MatrixState.pushMatrix();//保护场景
				MatrixState.rotate(10*pauseDegree, 0, 0, 1);//旋转
				MatrixState.scale(0.028f*pauseDegree,0.028f*pauseDegree, 1);//缩放

				pauseDegree++;
				for(BNObject pause:GameData.get(5))//绘制旋转出来的暂停界面
				{
					pause.drawSelf();
				}
				if(pauseDegree>=36)
				{
					pauseDegree=0;
					isFirstPause=false;
				}
				MatrixState.popMatrix();//恢复场景
			}
			else
			{
				for(BNObject pause:GameData.get(5))//绘制暂停界面
				{
					pause.drawSelf();
				}
			}
		}
	}
	//初始化雪
	public void initSnow()
	{
		fps.clear();//清空雪花列表
		int count=ParticleConstant.START_COLOR.length;//雪花种类的个数
		fpfd=new ParticleForDraw[count];//4组绘制着，4种颜色
		//创建粒子系统
		for(int i=0;i<count;i++)
		{
			ParticleConstant.CURR_INDEX=i;
			fpfd[i]=new ParticleForDraw(ParticleConstant.RADIS[ParticleConstant.CURR_INDEX],ShaderManager.getShader(1),TextureManager.getTextures("snow.png"));
			//创建对象,将雪花的初始位置传给构造器
			fps.add(new ParticleSystem(ParticleConstant.positionFireXZ[i][0],ParticleConstant.positionFireXZ[i][1],fpfd[i]));
		}
	}
	//初始化GameView
	public void initGameView()
	{
		synchronized(lockD)
		{
			deleteBall();
			addBall(MyFCData.ballData);
		}
		isOwnAxe=false;
		isCut=false;
		isWin=false;//游戏胜利界面的标志位
		isDrawWin=false;
		isLight=false;//停止绘制刀光
		isCutRigid=false;//停止绘制火花
		synchronized(lockA)
		{
			alFlyPolygon.clear();//飞走的列表清空
		}

		cpData=MyFCData.data[CheckpointIndex];//将多边形顶点数据赋给float数组
		isJudgePolygon=true;//允许删除或者创建包围框
		isJudgeBall=true;//允许删除或者创建球刚体

		ArrayList<ArrayList<BNObject>> bn=new ArrayList<ArrayList<BNObject>>();//临时列表

		ArrayList<BNObject> tempArrayList=new ArrayList<BNObject>();
		//===========start============创建lable对象
		tempArrayList=MyFCData.getLableObject();//获得游戏界面的lable对象
		bn.add(tempArrayList);
		//==========end=============

		//===========start============//创建切割物体
		BNPolyObject bnpo=new BNPolyObject//创建切割物体
				(
						MySurfaceView.this,
						TextureManager.getTextures(MyFCData.gamePicName[CheckpointIndex]),
						ShaderManager.getShader(0),
						MyFCData.data[CheckpointIndex],
						1080,
						1920,
						0,
						0
				);
		tempArrayList=new ArrayList<BNObject>();
		tempArrayList.add(bnpo);
		bn.add(tempArrayList);
		//==========end=============

		//===========start============//获得当前剩余的面积
		AllArea=(float)tempArrayList.get(0).cp.GetArea();//获得总的原始面积
		AreaSize=0;
		tempArrayList=new ArrayList<BNObject>();
		tempArrayList=MyFCData.getCurrentData(getAreaPercent(),850,80,40,50);//获得当前剩余的面积
		bn.add(tempArrayList);
		//==========end=============

		//===========start============//初始化当前关木块需切割的面积
		tempArrayList=new ArrayList<BNObject>();
		tempArrayList=MyFCData.getData(CheckpointIndex);//初始化当前关木块需切割的面积
		bn.add(tempArrayList);
		//==========end=============

		//===========start============//暂停界面
		tempArrayList=new ArrayList<BNObject>();
		tempArrayList=MyFCData.getPauseView();//暂停界面
		bn.add(tempArrayList);
		//==========end=============


		//===========start============//胜利界面
		tempArrayList=new ArrayList<BNObject>();
		tempArrayList=MyFCData.WinView();//胜利界面
		bn.add(tempArrayList);

		synchronized(lockB)
		{
			GameData.clear();//清空游戏界面的数据
			for(int i=1;i<=6;i++)
			{
				GameData.put(i, bn.get(i-1));
			}
		}
		//==========end=============
		gameTime=0;//游戏时间
		gameST=0;//游戏开始的时间
		pauseTime=0;//游戏暂停
		kniftNum=0;//切割刀数
		isPause=false;//初始化游戏界面，将暂停界面的标志位设为false

	}
	//播放按键音
	public void setPressSoundEffect(String music)
	{
		if(SoundEffect)
		{
			activity.sm.playSound(music,0);
		}
	}
	//获得切割的百分比
	public int getAreaPercent()
	{
		int AreaPercent = 100;
		if(AreaSize==0.0f)
		{
			AreaPercent=100;
		}
		else
		{
			AreaPercent=(int) (AreaSize/AllArea*AreaPercent);
		}
		return AreaPercent;
	}
	//创建包围框=====================start=========================
	public void addPolygon(float[] cpData)
	{
		alBNBody.clear();
		for(int j=0;j<cpData.length/2;j++)//创建包围框
		{
			float[] data=MyFCData.getData(cpData,j);
			Body bd=Box2DUtil.createEdge//重新创建包围框
					(
							data,
							world,
							true,
							0,
							0,
							0,
							-1
					);
			alBNBody.add(bd);//将创建的包围框添加进列表中
		}
	}
	//创建包围框=====================end=========================
	//删除包围框=====================start=========================
	public void deletePolygon()
	{
		for(int i=0;i<alBNBody.size();i++)//销毁alBNBody列表中的包围框Body
		{
			world.destroyBody(alBNBody.get(i));
		}
	}
	//删除包围框=====================end=========================
	//创建球=====================start=========================
	public void addBall(float[][] ballData){
		alBNBall.clear();
		alBNBall=MyFCData.getBall(MySurfaceView.this,world,ballData);//获得球列表
	}
	//创建球=====================end=========================
	//删除球=====================start=========================
	public void deleteBall(){
		for(int i=0;i<BallBody.size();i++)//销毁alBNBody列表中的包围框Body
		{
			world.destroyBody(BallBody.get(i));
		}
		BallBody.clear();
	}
	//删除球=====================end=========================
	//判断是否模拟===================start========================
	public boolean worldStep()
	{
		if(isJudgePolygon)//不允许物理世界模拟
		{
			return false;
		}else//允许物理世界模拟
		{
			return true;
		}
	}
}
