package com.bn.util.constant;

import android.opengl.GLES20;

public class ParticleConstant {
	//粒子系统-雪===================start==================
	//雪的初始总位置
	public static float distancesFireXZ=0f;
	public static float[][] positionFireXZ={{0,distancesFireXZ},{0,distancesFireXZ}};
	//当前索引
	public static int CURR_INDEX=0;
	//起始颜色
	public static final float[][] START_COLOR=
			{
					{0.9f,0.9f,0.9f,1.0f},
					{0.9f,0.9f,0.9f,1.0f}
			};
	//终止颜色
	public static final float[][] END_COLOR=
			{
					{1.0f,1.0f,1.0f,0.0f},
					{1.0f,1.0f,1.0f,0.0f}
			};

	public static final float[] FLY_START_COLOR=
			{
					0.9f,0.9f,0.9f,1.0f
			};
	public static final float[] FLY_END_COLOR=
			{
					1.0f,1.0f,1.0f,0.0f
			};

	//源混合因子
	public static final int[] SRC_BLEND=
			{
					GLES20.GL_SRC_ALPHA,
					GLES20.GL_SRC_ALPHA
			};
	//目标混合因子
	public static final int[] DST_BLEND=
			{
					GLES20.GL_ONE_MINUS_SRC_ALPHA,
					GLES20.GL_ONE_MINUS_SRC_ALPHA
			};
	//混合方式
	public static final int[] BLEND_FUNC=
			{
					GLES20.GL_FUNC_ADD,
					GLES20.GL_FUNC_ADD
			};
	//单个粒子半径
	public static final float[] RADIS=
			{
					0.018f,
					0.013f
			};

	//粒子最大生命期
	public static final float[] MAX_LIFE_SPAN=
			{
					4f,
					4f
			};

	//粒子生命周期步进
	public static final float[] LIFE_SPAN_STEP=
			{
					0.02f,
					0.02f
			};

	//粒子发射的X左右范围
	public static final float[] X_RANGE=
			{
					0.7f,
					0.6f
			};

	//每次喷发发射的数量
	public static final int[] GROUP_COUNT=
			{
					1,
					2,
					3
			};

	//粒子Y方向升腾的速度
	public static final float[] VY=
			{
					-0.005f,
					-0.004f
			};
	//粒子更新物理线程休息时间
	public static final int[] THREAD_SLEEP=
			{
					15,
					15
			};
	//粒子系统-雪===================end==================

}
