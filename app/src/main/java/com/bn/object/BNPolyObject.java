package com.bn.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.bn.fastcut.MySurfaceView;
import com.bn.util.constant.Constant;
import com.bn.util.constant.GeoLibUtil;
import com.bn.util.constant.GeometryConstant;
import com.bn.util.constant.MatrixState;
import android.opengl.GLES20;

//纹理三角形
public class BNPolyObject extends BNObject
{
	MySurfaceView mv;
	float vx;
	float vy;
	int count=1;
	public BNPolyObject(MySurfaceView mv,int texId,int program,float[] vData,float yswidth,float ysheight,float vx,float vy)
	{
		super(texId,program,vData,yswidth,ysheight);
		this.mv=mv;
		this.vx=vx;
		this.vy=vy;
		initVertexData(vData,yswidth,ysheight);
	}
	//初始化顶点坐标与着色数据的方法
	public void initVertexData(float[] vData,float yswidth,float ysheight)
	{
		//--------管线中接收三角形绘制方式----------
		float[] dd= GeoLibUtil.fromAnyPolyToTris(vData);//将多边形切分成三角形组
		vCount=dd.length/2;
		//顶点坐标数据的初始化================begin============================
		float vertices[]=new float[vCount*3];
		for(int i=0;i<vCount;i++)
		{
			vertices[i*3]=Constant.fromScreenXToNearX(dd[i*2]);//x坐标
			vertices[i*3+1]=Constant.fromScreenYToNearY(dd[i*2+1]);//y坐标
			vertices[i*3+2]=0;//z坐标
		}
		//创建顶点坐标数据缓冲
		//vertices.length*4是因为一个整数四个字节
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
		vbb.order(ByteOrder.nativeOrder());//设置字节顺序
		mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
		mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);//设置缓冲区起始位置
		//特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
		//转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
		//顶点坐标数据的初始化================end============================

		//顶点纹理坐标数据的初始化================begin============================
		float texCoor[]=new float[vCount*2];
		for(int i=0;i<vCount;i++)
		{
			texCoor[i*2]=dd[i*2]/yswidth;//纹理s坐标
			texCoor[i*2+1]=dd[i*2+1]/ysheight;//纹理t坐标
		}
		//创建顶点纹理坐标数据缓冲
		ByteBuffer cbb = ByteBuffer.allocateDirect(texCoor.length*4);
		cbb.order(ByteOrder.nativeOrder());//设置字节顺序
		mTexCoorBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
		mTexCoorBuffer.put(texCoor);//向缓冲区中放入顶点着色数据
		mTexCoorBuffer.position(0);//设置缓冲区起始位置
		//特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
		//转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
		//顶点纹理坐标数据的初始化================end============================
	}

	//绘制木块飞走 逐渐消逝的方法=========================start===============================
	public void drawSelf(float sj)
	{
		if(!initFlag)
		{
			//初始化着色器
			initShader(true);
			initFlag=true;
		}
		if(count>15)
		{
			sj=sj-0.04f*(count-16);
		}
		if(sj<0.7)
		{
			GLES20.glEnable(GLES20.GL_BLEND);//打开混合
			//设置混合因子
			GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE);
		}
		//制定使用某套shader程序
		GLES20.glUseProgram(programId);

		MatrixState.pushMatrix();//保护场景

		//这里供测试用=========================================================
		count++;
		//上面
		if(GeometryConstant.cutDirection==1)
		{
			MatrixState.translate(0, GeometryConstant.calculateDisplacement(1,count), 0);
		}//下面
		else if(GeometryConstant.cutDirection==2)
		{
			MatrixState.translate(0,  GeometryConstant.calculateDisplacement(2,count), 0);
		}//左面
		else if(GeometryConstant.cutDirection==3)
		{
			MatrixState.translate( GeometryConstant.calculateDisplacement(3,count), GeometryConstant.calculateDisplacement(1,count), 0);
		}//右面
		else if(GeometryConstant.cutDirection==4)
		{
			MatrixState.translate( GeometryConstant.calculateDisplacement(4,count), GeometryConstant.calculateDisplacement(1,count), 0);
		}
		MatrixState.rotate(-count*0.5f, 0, 0, 1);//旋转
		//这里供测试用=========================================================

		//将最终变换矩阵传入shader程序
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
		//将衰减因子传入shader程序
		GLES20.glUniform1f(muSjFactor, sj);
		//为画笔指定顶点位置数据
		GLES20.glVertexAttribPointer
				(
						maPositionHandle,
						3,
						GLES20.GL_FLOAT,
						false,
						3*4,
						mVertexBuffer
				);
		//为画笔指定顶点纹理坐标数据
		GLES20.glVertexAttribPointer
				(
						maTexCoorHandle,
						2,
						GLES20.GL_FLOAT,
						false,
						2*4,
						mTexCoorBuffer
				);
		//允许顶点位置数据数组
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maTexCoorHandle);

		//绑定纹理
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

		//绘制纹理矩形
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
		if(sj<0.7)
		{
			//关闭混合
			GLES20.glDisable(GLES20.GL_BLEND);
		}
		MatrixState.popMatrix();//恢复场景
	}
	//绘制木块飞走 逐渐消逝的方法=========================end===============================

	//绘制图形
	public void drawSelf()
	{
		if(!initFlag)
		{
			//初始化着色器
			initShader();
			initFlag=true;
		}
		//制定使用某套shader程序
		GLES20.glUseProgram(programId);
		GLES20.glEnable(GLES20.GL_BLEND);//打开混合
		//设置混合因子
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
		MatrixState.pushMatrix();//保护场景

		//这里供测试用=========================================================
		count++;
		MatrixState.translate(vx*count, vy*count, 0);
		//这里供测试用=========================================================

		//将最终变换矩阵传入shader程序
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
		//为画笔指定顶点位置数据
		GLES20.glVertexAttribPointer
				(
						maPositionHandle,
						3,
						GLES20.GL_FLOAT,
						false,
						3*4,
						mVertexBuffer
				);
		//为画笔指定顶点纹理坐标数据
		GLES20.glVertexAttribPointer
				(
						maTexCoorHandle,
						2,
						GLES20.GL_FLOAT,
						false,
						2*4,
						mTexCoorBuffer
				);
		//允许顶点位置数据数组
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glEnableVertexAttribArray(maTexCoorHandle);

		//绑定纹理
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

		//绘制纹理矩形--三角形法
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
		//关闭混合
		GLES20.glDisable(GLES20.GL_BLEND);

		MatrixState.popMatrix();//恢复场景
	}
}
