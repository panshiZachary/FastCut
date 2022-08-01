package com.bn.object;

import static com.bn.util.constant.Constant.RATE;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.jbox2d.dynamics.Body;
import uk.co.geolib.geopolygons.C2DPolygon;
import android.opengl.GLES20;

import com.bn.fastcut.MySurfaceView;
import com.bn.util.constant.Constant;
import com.bn.util.constant.GeoLibUtil;
import com.bn.util.constant.MatrixState;

public class BNObject
{
	public C2DPolygon cp;//创建多边形对象
	public FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
	public FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲
	int muMVPMatrixHandle;//总变换矩阵引用id
	int maPositionHandle;//顶点位置属性引用id
	int maTexCoorHandle;//顶点纹理坐标属性引用id

	int muSjFactor;//衰减因子引用id

	int programId;//自定义渲染管线程序id
	int texId;//纹理图片名
	int vCount;//顶点个数
	boolean initFlag=false;//判断是否初始化着色器
	public Body body;//创建Body对象
	float x;//需要平移的x坐标
	float y;//需要平移的y坐标
	public float ballPositionX;
	public float ballPositionY;
	boolean isArea=false;
	int num=0;//判断为第几个数字
	MySurfaceView mv;
	boolean isLine=false;//是否绘制线条--true绘制
	int index=1;//0-线条 1-刀光
	float angle=0;//绘制线条时旋转的角度

	public BNObject(MySurfaceView mv,Body body,float x,float y,float picWidth,float picHeight,int texId,int programId,int index)
	{
		this.mv=mv;
		this.x=x;
		this.y=y;
		this.body=body;//获得Body对象
		mv.BallBody.add(body);//将body 对象添加到MySurfaceView中的列表
		this.body.setUserData(index);//设置body对象的ID
		this.texId=texId;//获得对应的纹理ID
		this.programId=programId;//获得对应的程序ID
		initVertexData(picWidth,picHeight);//初始化顶点数据
	}
	public BNObject(float sx,float sy,float ex,float ey,int texId,int programId,boolean isLine,int index)
	{//绘制切割线
		float length=(float)Math.sqrt((ex-sx)*(ex-sx)+(ey-sy)*(ey-sy));//线条的长度
		float halfx=0;//线条的半宽
		float halfy=0;//线条的半高
		this.isLine=isLine;//绘制线条
		this.angle=(float)Math.toDegrees(Math.atan((ex-sx)/(ey-sy)));//获得需旋转的角度
		if(sx<=ex&&sy<=ey)//左上斜向右下
		{
			halfx=sx+Math.abs(ex-sx)/2;
			halfy=sy+Math.abs(ey-sy)/2;
		}
		else if(sx>=ex&&sy>=ey)//右下斜向左上
		{
			halfx=ex+Math.abs(ex-sx)/2;
			halfy=ey+Math.abs(ey-sy)/2;
		}
		else if(sx>=ex&&sy<=ey)//右上斜向左下
		{
			halfx=ex+Math.abs(ex-sx)/2;
			halfy=sy+Math.abs(ey-sy)/2;
		}
		else if(sx<=ex&&sy>=ey)//左下斜向右上
		{
			halfx=sx+Math.abs(ex-sx)/2;
			halfy=ey+Math.abs(ey-sy)/2;
		}
		this.x=Constant.fromScreenXToNearX(halfx);//将屏幕x转换成视口x坐标
		this.y=Constant.fromScreenYToNearY(halfy);//将屏幕y转换成视口y坐标
		this.texId=texId;
		this.programId=programId;
		this.index=index;
		if(index==0)//切割线
		{
			initVertexData(8,length);//初始化顶点数据
		}else
		{
			initVertexData(50,length);//初始化顶点数据
		}
	}
	public BNObject(float x,float y,float picWidth,float picHeight,int texId,int programId)
	{
		this.x=Constant.fromScreenXToNearX(x);//将屏幕x转换成视口x坐标
		this.y=Constant.fromScreenYToNearY(y);//将屏幕y转换成视口y坐标
		this.texId=texId;
		this.programId=programId;
		initVertexData(picWidth,picHeight);//初始化顶点数据
	}
	public BNObject(float x,float y,float picWidth,float picHeight,int texId,int programId,int num)
	{
		this.num=num;
		isArea=true;//是切割面积数据对象
		this.x=Constant.fromScreenXToNearX(x);//将屏幕x转换成视口x坐标
		this.y=Constant.fromScreenYToNearY(y);//将屏幕y转换成视口y坐标
		this.texId=texId;
		this.programId=programId;
		initVertexData(picWidth,picHeight);//初始化顶点数据
	}
	public BNObject(int texId,int programId,float[] vData,float yswidth,float ysheight)
	{
		this.texId=texId;//获得纹理图片名称
		this.programId=programId;//获得使用哪套程序
		cp=GeoLibUtil.createPoly(vData);//创建多边形
	}
	public void initVertexData(float width,float height)//初始化顶点数据
	{
		vCount=4;//顶点个数

		float degree=1;//切割线条纹理图片的T值
		width=Constant.fromPixSizeToNearSize(width);//屏幕宽度转换成视口宽度
		height=Constant.fromPixSizeToNearSize(height);//屏幕高度转换成视口高度

		if(isLine&&index==0)//绘制线条
		{
			degree=height;
		}
		//初始化顶点坐标数据
		float vertices[]=new float[]
				{
						-width/2,height/2,0,
						-width/2,-height/2,0,
						width/2,height/2,0,
						width/2,-height/2,0
				};
		ByteBuffer vbb=ByteBuffer.allocateDirect(vertices.length*4);//创建顶点坐标数据缓冲
		vbb.order(ByteOrder.nativeOrder());//设置字节顺序
		mVertexBuffer=vbb.asFloatBuffer();//转换为Float型缓冲
		mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
		mVertexBuffer.position(0);//设置缓冲区起始位置
		float[] texCoor=new float[12];//初始化纹理坐标数据
		if(isLine)//切割线条
		{
			texCoor=new float[]
					{
							0,0,
							0,degree,
							1,0,
							1,degree,
							1,0,
							0,degree
					};
		}else if(!isArea)
		{					//其他图形的纹理坐标
			texCoor=new float[]
					{
							0,0,
							0,1,
							1,0,
							1,1,
							1,0,
							0,1
					};
		}
		else//切割面积数据
		{
			float rate=0.1f*num;
			texCoor=new float[]
					{
							0+rate,0,
							0+rate,1,
							1*0.1f+rate,0,
							1*0.1f+rate,1,
							1*0.1f+rate,0,
							0+rate,1
					};
		}
		ByteBuffer cbb=ByteBuffer.allocateDirect(texCoor.length*4);//创建顶点纹理坐标数据缓冲
		cbb.order(ByteOrder.nativeOrder());//设置字节顺序
		mTexCoorBuffer=cbb.asFloatBuffer();//转换为Float型缓冲
		mTexCoorBuffer.put(texCoor);//向缓冲区中放入顶点着色数据
		mTexCoorBuffer.position(0);//设置缓冲区起始位置
	}
	//初始化着色器
	public void initShader()
	{
		//获取程序中顶点位置属性引用id
		maPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
		//获取程序中顶点纹理坐标属性引用id
		maTexCoorHandle= GLES20.glGetAttribLocation(programId, "aTexCoor");
		//获取程序中总变换矩阵引用id
		muMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
	}

	//绘制木块飞走 逐渐消逝的方法=========================start===============================
	public void initShader(boolean isFly)
	{
		//获取程序中顶点位置属性引用id
		maPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
		//获取程序中顶点纹理坐标属性引用id
		maTexCoorHandle= GLES20.glGetAttribLocation(programId, "aTexCoor");
		//获取程序中总变换矩阵引用id
		muMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
		//获取程序中衰减因子引用id
		muSjFactor=GLES20.glGetUniformLocation(programId, "sjFactor");
	}
	public void drawSelf(float sj){}
	//绘制木块飞走 逐渐消逝的方法=========================end===============================

	//绘制火花
	public void drawSelf(int TexID)
	{
		if(!initFlag)
		{
			//初始化着色器
			initShader();
			initFlag=true;
		}
		GLES20.glEnable(GLES20.GL_BLEND);//打开混合
		//设置混合因子
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE);
		//制定使用某套shader程序
		GLES20.glUseProgram(programId);
		MatrixState.pushMatrix();//保护场景
		MatrixState.translate(x,y, 0);//平移
		//将最终变换矩阵传入shader程序
		GLES20.glUniformMatrix4fv
				(
						muMVPMatrixHandle,
						1,
						false,
						MatrixState.getFinalMatrix(),
						0
				);
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
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,TexID);

		//绘制纹理矩形--条带法
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vCount);
		//关闭混合
		GLES20.glDisable(GLES20.GL_BLEND);

		MatrixState.popMatrix();//恢复场景
	}
	//绘制图形
	public void drawSelf()
	{
		if(!initFlag)
		{
			//初始化着色器
			initShader();
			initFlag=true;
		}
		GLES20.glEnable(GLES20.GL_BLEND);//打开混合
		//设置混合因子
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
		//制定使用某套shader程序
		GLES20.glUseProgram(programId);
		if(body!=null)
		{
			x=body.getPosition().x*RATE;//根据刚体获得x坐标
			y=body.getPosition().y*RATE;//根据刚体获得y坐标

			ballPositionX=x;
			ballPositionY=y;
			x=Constant.fromScreenXToNearX(x);//将屏幕x转换成视口x坐标
			y=Constant.fromScreenYToNearY(y);//将屏幕y转换成视口y坐标
		}
		MatrixState.pushMatrix();//保护场景
		MatrixState.translate(x,y, 0);//平移

		if(isLine)//绘制切割线条
		{
			MatrixState.rotate(angle, 0, 0, 1);//旋转
		}
		//将最终变换矩阵传入shader程序
		GLES20.glUniformMatrix4fv
				(
						muMVPMatrixHandle,
						1,
						false,
						MatrixState.getFinalMatrix(),
						0
				);
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
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texId);

		//绘制纹理矩形--条带法
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vCount);
		//关闭混合
		GLES20.glDisable(GLES20.GL_BLEND);

		MatrixState.popMatrix();//恢复场景
	}
}
