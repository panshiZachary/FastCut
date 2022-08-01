package com.bn.object;

import com.bn.object.BNObject;

public class BNView {

	float x;
	float y;
	float width;
	float height;
	int programId;
	int textureId;
	public BNView(float x,float y,float width,float height,int programId,int textureId)
	{
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
		this.programId=programId;
		this.textureId=textureId;
	}
	public BNObject getBNObject()//获取BnObject对象
	{
		BNObject bnObject=new BNObject
				(
						x,y,
						width,height,
						programId,textureId
				);
		return bnObject;
	}
}
