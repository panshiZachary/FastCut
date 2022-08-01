package com.bn.util.manager;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.bn.fastcut.MySurfaceView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TextureManager
{
	static String[] texturesName=
			{
					"bg_01.png","option_a.png","help_a.png","play_a.png","exit_a.png",//5
					"option_b.png","help_b.png","play_b.png","exit_b.png",
					"choose.png","musicOn.png","soundOn.png",//0-3
					"help.png","tip1.png","point_white.png",//4-6
					"level_bg.jpg","set1_a.png","set2_a.png","set3_a.png","back.png","snow.png",//7-12
					"set1-2.png","s_01_a.png","s_02_a.png",//13-15
					"set2-2.png","s_03_a.png","s_04_a.png",//16-18
					"set3-2.png","s_05_a.png","s_06_a.png",//19-21
					"soundOff.png","musicOff.png",//22-23
					"tip2.png","tip3.png",//24-25
					"s_01.png","s_02.png","s_03.png","s_04.png","s_05.png","s_06.png",//26-31
					"dartsmall.png","lable1.png",//32-33
					"gg.png","lable2.png","number.png","suspend_bg.png","zhanting.png","guanQia_a.png",//34-39
					"guanQia_b.png","replay_a.png","replay_b.png","resume_a.png","resume_b.png",//40-44
					"win.png","next_a.png","next_b.png","light.png","line.png","spark_0.png","spark_1.png","spark_2.png",//45-52
					"spark_3.png","spark_4.png","spark_5.png","spark_6.png","spark_7.png","spark_8.png","spark_9.png",//53-59
					"spark_10.png","spark_11.png","spark_12.png",//60-62
					"set1_b.png","set2_b.png","set3_b.png","tiebuff.png"//63-66
			};//纹理图的名称

	static boolean[] isRepeat={//设置S T轴的截取方式
			false,false,false,false,false,false,false,false,
			false,false,false,false,false,false,false,false,false,false,
			false,false,false,false,false,false,false,false,false,false,
			false,false,false,false,false,false,false,false,false,false,
			false,false,false,false,false,false,false,false,false,false,
			false,false,false,false,false,false,false,false,false,true,
			false,false,false,false,false,false,false,false,false,false,
			false,false,false,false,false,false,false
	};
	static HashMap<String,Integer> texList=new HashMap<String,Integer>();//放纹理图的列表
	public static int initTexture(MySurfaceView mv,String texName,boolean isRepeat)//生成纹理id
	{
		int[] textures=new int[1];
		GLES20.glGenTextures
				(
						1,//产生的纹理id的数量
						textures,//纹理id的数组
						0//偏移量
				);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);//绑定纹理id
		//设置MAG时为线性采样
		GLES20.glTexParameterf
				(
						GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MAG_FILTER,
						GLES20.GL_LINEAR
				);
		//设置MIN时为最近点采样
		GLES20.glTexParameterf
				(
						GLES20.GL_TEXTURE_2D,
						GLES20.GL_TEXTURE_MIN_FILTER,
						GLES20.GL_NEAREST
				);
		if(isRepeat)
		{
			//设置S轴的拉伸方式为重复拉伸
			GLES20.glTexParameterf
					(
							GLES20.GL_TEXTURE_2D,
							GLES20.GL_TEXTURE_WRAP_S,
							GLES20.GL_REPEAT
					);
			//设置T轴的拉伸方式为重复拉伸
			GLES20.glTexParameterf
					(
							GLES20.GL_TEXTURE_2D,
							GLES20.GL_TEXTURE_WRAP_T,
							GLES20.GL_REPEAT
					);
		}else
		{
			//设置S轴的拉伸方式为截取
			GLES20.glTexParameterf
					(
							GLES20.GL_TEXTURE_2D,
							GLES20.GL_TEXTURE_WRAP_S,
							GLES20.GL_CLAMP_TO_EDGE
					);
			//设置T轴的拉伸方式为截取
			GLES20.glTexParameterf
					(
							GLES20.GL_TEXTURE_2D,
							GLES20.GL_TEXTURE_WRAP_T,
							GLES20.GL_CLAMP_TO_EDGE
					);
		}

		String path="pic/"+texName;//定义图片路径
		InputStream in = null;
		try {
			in = mv.getResources().getAssets().open(path);
		}catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bitmap=BitmapFactory.decodeStream(in);//从流中加载图片内容
		GLUtils.texImage2D
				(
						GLES20.GL_TEXTURE_2D,//纹理类型，在OpenGL ES中必须为GL10.GL_TEXTURE_2D
						0,//纹理的层次，0表示基本图像层，可以理解为直接贴图
						bitmap,//纹理图像
						0//纹理边框尺寸
				);
		bitmap.recycle();//纹理加载成功后释放内存中的纹理图
		return textures[0];
	}
	public static void loadingTexture(MySurfaceView mv,int start,int picNum)//加载所有纹理图
	{
		for(int i=start;i<start+picNum;i++)
		{
			int texture=initTexture(mv,texturesName[i],isRepeat[i]);
			texList.put(texturesName[i],texture);//将数据加入到列表中
		}
	}
	public static int getTextures(String texName)//获得纹理图
	{
		int result=0;
		if(texList.get(texName)!=null)//如果列表中有此纹理图
		{
			result=texList.get(texName);//获取纹理图
		}else
		{
			result=-1;
		}
		return result;
	}
}
