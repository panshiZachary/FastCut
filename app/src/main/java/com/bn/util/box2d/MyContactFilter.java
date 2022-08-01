package com.bn.util.box2d;

import org.jbox2d.callbacks.ContactFilter;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;

import com.bn.fastcut.MySurfaceView;

public class MyContactFilter extends ContactFilter//碰撞过滤相关类
{
	public MyContactFilter(MySurfaceView mv){	}//构造器
	@Override
	public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB)///检验是否碰撞的方法
	{
		Body bodyA= fixtureA.getBody();
		Body bodyB= fixtureB.getBody();
		if((Integer)(bodyA.getUserData())==-1||(Integer)(bodyB.getUserData())==-1)//判断参与碰撞的刚体是否地面或墙壁
		{
			return true;
		}else
		{
			return false;
		}
	}
}
