package com.bn.util.box2d;

import org.jbox2d.collision.shapes.*;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.bn.fastcut.MySurfaceView;
import com.bn.object.BNObject;
import com.bn.util.manager.ShaderManager;
import static com.bn.util.constant.Constant.RATE;
//生成物理形状的工具类
public class Box2DUtil
{
	//创建直线
	public static Body createEdge
	(
			float[] data,
			World world,//世界
			boolean isStatic,
			float density,//密度
			float friction,//摩擦系数
			float restitution,
			int index
	)
	{
		//创建刚体描述
		BodyDef bd=new BodyDef();
		//设置是否为可运动刚体
		if(isStatic)
		{
			bd.type=BodyType.STATIC;
		}
		else
		{
			bd.type=BodyType.DYNAMIC;
		}
		float positionX=(data[0]+data[2])/2;
		float positionY=(data[1]+data[3])/2;
		//设置位置
		bd.position.set(positionX/RATE,positionY/RATE);
		//在世界中创建刚体
		Body bodyTemp = null;

		while(bodyTemp==null)
		{
			bodyTemp = world.createBody(bd);
		}
		//在刚体中记录对应的包装对象
		bodyTemp.setUserData(index);
		//创建刚体形状
		EdgeShape ps=new EdgeShape();
		ps.set(new Vec2((data[0]-positionX)/RATE,(data[1]-positionY)/RATE), new Vec2((data[2]-positionX)/RATE,(data[3]-positionY)/RATE));
		//创建刚体物理描述
		FixtureDef fd=new FixtureDef();
		//设置摩擦系数
		fd.friction =friction;
		//设置能量损失率（反弹）
		fd.restitution = restitution;
		//设置密度
		fd.density=density;
		//设置形状
		fd.shape=ps;
		//将刚体物理描述与刚体结合
		if(!isStatic)
		{
			bodyTemp.createFixture(fd);
		}
		else
		{
			bodyTemp.createFixture(ps, 0);//创建密度为0的PolygonShape对象
		}
		return bodyTemp;
	}
	//创建圆形（颜色）
	public static BNObject createCircle
	(
			MySurfaceView mv,
			float x,//x坐标
			float y,//y坐标
			float radius,//半径
			World world,//世界
			int programId,//程序ID
			int texId,//纹理名称
			float density,//密度
			float friction,//摩擦系数
			float restitution,//恢复系数
			int index
	)
	{
		//创建刚体描述
		BodyDef bd=new BodyDef();
		//设置是否为可运动刚体
		bd.type=BodyType.DYNAMIC;
		//设置位置
		bd.position.set(x/RATE,y/RATE);
		//在世界中创建刚体
		Body bodyTemp=null;
		while(bodyTemp==null)
		{
			bodyTemp= world.createBody(bd);
		}
		//创建刚体形状
		CircleShape cs=new CircleShape();
		cs.m_radius=radius/RATE;
		//创建刚体物理描述
		FixtureDef fd=new  FixtureDef();
		//设置密度
		fd.density = density;
		//设置摩擦系数
		fd.friction = friction;
		//设置能量损失率（反弹）
		fd.restitution =restitution;
		//设置形状
		fd.shape=cs;
		//将刚体物理描述与刚体结合
		bodyTemp.createFixture(fd);
		//返回BNObject类对象
		return new BNObject(mv,bodyTemp, x, y, radius*2, radius*2, texId,ShaderManager.getShader(programId),index);
	}
}
