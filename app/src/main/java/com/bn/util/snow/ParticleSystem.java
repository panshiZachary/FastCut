package com.bn.util.snow;

import java.util.*;

import com.bn.util.constant.MatrixState;
import static com.bn.util.constant.ParticleConstant.*;
import android.opengl.GLES20;

public class ParticleSystem implements Comparable<ParticleSystem>
{
	//用于存放所有的粒子
	public ArrayList<ParticleSingle> alFsp=new ArrayList<ParticleSingle>();
	//-用于存放需要删除的粒子
	ArrayList<ParticleSingle> alFspForDel=new ArrayList<ParticleSingle>();
	//用于转存所有的粒子，每次都要情况/
	public ArrayList<ParticleSingle> alFspForDraw=new ArrayList<ParticleSingle>();
	//用于绘制的所有粒子
	public ArrayList<ParticleSingle> alFspForDrawTemp=new ArrayList<ParticleSingle>();
	//资源锁
	Object lock=new Object();
	//起始颜色
	public float[] startColor;
	//终止颜色
	public float[] endColor;
	//源混合因子
	public int srcBlend;
	//目标混合因子
	public int dstBlend;
	//混合方式
	public int blendFunc;
	//粒子最大生命期
	public float maxLifeSpan;
	//粒子生命期步进
	public float lifeSpanStep;
	//粒子更新线程休眠时间间隔
	public int sleepSpan;
	//每次喷发的例子数量
	public int groupCount;
	//基础发射点
	public float sx;
	public float sy;
	//绘制位置
	float positionX;
	float positionZ;
	//发射点变化范围
	public float xRange;
	//粒子发射的速度
	public float vx;
	public float vy;
	//绘制者
	ParticleForDraw fpfd;
	//工作标志位
	boolean flag=true;
	public ParticleSystem(float positionx,float positionz,ParticleForDraw fpfd)
	{
		this.positionX=positionx;
		this.positionZ=positionz;
		this.startColor=START_COLOR[CURR_INDEX];
		this.endColor=END_COLOR[CURR_INDEX];
		this.srcBlend=SRC_BLEND[CURR_INDEX];
		this.dstBlend=DST_BLEND[CURR_INDEX];
		this.blendFunc=BLEND_FUNC[CURR_INDEX];
		this.maxLifeSpan=MAX_LIFE_SPAN[CURR_INDEX];
		this.lifeSpanStep=LIFE_SPAN_STEP[CURR_INDEX];
		this.groupCount=GROUP_COUNT[CURR_INDEX];
		this.sleepSpan=THREAD_SLEEP[CURR_INDEX];
		this.sx=0;
		this.sy=0;
		this.xRange=X_RANGE[CURR_INDEX];
		this.vx=0;
		this.vy=VY[CURR_INDEX];
		this.fpfd=fpfd;

		new Thread()//开启线程
		{
			public void run()
			{
				while(flag)
				{
					update();
					try
					{
						Thread.sleep(sleepSpan);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	public void drawSelf()
	{
		//开启混合
		GLES20.glEnable(GLES20.GL_BLEND);
		//设置混合方式
		GLES20.glBlendEquation(blendFunc);
		//设置混合因子
		GLES20.glBlendFunc(srcBlend,dstBlend);

		//因为每次进行绘制粒子的个数已经对象是不断变化的，所以需要不断地更新
		alFspForDrawTemp.clear();
		synchronized(lock)
		{
			//加锁的目的是为了保证添加和情况不同时进行，也就是保证了每次add都会有对象
			for(int i=0;i<alFspForDraw.size();i++)
			{
				alFspForDrawTemp.add(alFspForDraw.get(i));
			}
		}
		MatrixState.pushMatrix();

		MatrixState.translate(positionX, 1, positionZ);
		for(ParticleSingle fsp:alFspForDrawTemp)
		{
			fsp.drawSelf(startColor,endColor,maxLifeSpan);
		}
		//关闭混合
		GLES20.glDisable(GLES20.GL_BLEND);

		MatrixState.popMatrix();
	}

	public void update()
	{
		//喷发新粒子
		for(int i=0;i<groupCount;i++)
		{
			//在中心附近产生产生粒子的位置------**/
			float px=(float) (sx+xRange*(Math.random()*2-1.0f));

			float vx=(sx-px)/150;
			//x方向的速度很小,所以就产生了拉长的火焰粒子
			ParticleSingle fsp=new ParticleSingle(px,0.01f,vx,vy,fpfd);
			alFsp.add(fsp);
		}

		//清空缓冲的粒子列表，此列表主要存储需要删除的粒子
		alFspForDel.clear();
		for(ParticleSingle fsp:alFsp)
		{
			//对每个粒子执行运动操作
			fsp.go(lifeSpanStep);
			//如果粒子已经存在的时间已经足够了，就把它添加到需要删除的粒子列表
			if(fsp.lifeSpan>this.maxLifeSpan)
			{
				alFspForDel.add(fsp);
			}
		}

		//删除过期粒子
		for(ParticleSingle fsp:alFspForDel)
		{
			alFsp.remove(fsp);
		}
		//alFsp列表中存放了所有的粒子对象，其他的列表为其服务，他可以添加粒子，同时也可以删除某些过期的粒子对象
		//更新绘制列表
		synchronized(lock)
		{
			alFspForDraw.clear();
			for(int i=0;i<alFsp.size();i++)
			{
				alFspForDraw.add(alFsp.get(i));
			}
		}
	}
	@Override
	public int compareTo(ParticleSystem another) {
		return 0;
	}
}
