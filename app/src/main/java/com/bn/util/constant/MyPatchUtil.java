package com.bn.util.constant;

import java.util.ArrayList;

import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geopolygons.C2DPolygon;
import android.graphics.Point;

public class MyPatchUtil {//补丁类
	//根据多边形获取Point数组
	public static Point[] getPolygonData(C2DPolygon cp,boolean IsClockwise)
	{
		ArrayList<C2DPoint> pointCopyIn=new ArrayList<C2DPoint>();//创建C2DPoint对象列表
		cp.GetPointsCopy(pointCopyIn);//赋值
		int numsCpTemp=pointCopyIn.size();//获得列表的长度
		Point[] pArray=new Point[numsCpTemp];//创建Point数组
		//顺时针向顶点数组赋值
		if(IsClockwise)
		{
			for(int j=0;j<numsCpTemp;j++)
			{
				C2DPoint tempCP1=pointCopyIn.get(numsCpTemp-1-j);
				pArray[j]=new Point((int)tempCP1.x,(int)tempCP1.y);
			}
		}else//逆时针向顶点数组赋值
		{
			for(int j=0;j<numsCpTemp;j++)
			{
				C2DPoint tempCP1=pointCopyIn.get(j);
				pArray[j]=new Point((int)tempCP1.x,(int)tempCP1.y);
			}
		}
		Point[] answer=new Point[100];
		for(int j=0;j<numsCpTemp;j++)
		{
			answer[j] = pArray[j];//将pArray1数组的数据保存至answer数组中
		}
		return pArray;
	}
	//将两个多边形合并成一个多边形
	public static C2DPolygon getCombinePolygon(Point[] pArray1,Point[] pArray2,int num1,int num2)
	{
		Point[] tempAnswer=new Point[100];//用来记录临时的答案数组
		int indexAnswer = -1;//临时答案数组的数组索引
		for(int ii=0;ii<num1;ii++)//对 当前答案数组 进行遍历
		{
			boolean flag = false;//标志位
			for(int jj=0;jj<num2;jj++)//对 当前多边形的点数组 进行遍历
			{
				if(pArray1[ii].equals(pArray2[jj]))//若 当前答案点  与多边形的当前点  是相同的
				{
					flag = true;
					indexAnswer++;
					tempAnswer[indexAnswer]=pArray1[ii];//把当前点加入 tempAnswer中
					int indexii=0;
					//将indexii赋值为  多边形的当前点的索引的上一索引
					if(jj==0)//若当前点的索引是0，则将indexii赋值为数组长度-1
					{
						indexii = num2-1;
					}else
					{
						indexii = jj - 1;
					}
					if(pArray1[(ii+1)%num1].equals(pArray2[indexii]))//当前点的 下一点  与  多边形的当前点的上一点   是相同的 ，则将多边形的部分点添加到tempAnswer数组中
					{
						for(int kk=(jj+1)%num2;;kk=(kk+1)%num2)//遍历pArray2数组
						{
							if(pArray2[kk].equals(pArray2[indexii]))//这个是for循环的终止条件  （当 多边形的当前点的上一点 与  遍历到的点相同时）
							{
								break;
							}
							indexAnswer++;
							tempAnswer[indexAnswer]=pArray2[kk];//将点添加到答案数组中
						}
					}
				}
			}
			if(flag == false)
			{
				indexAnswer++;//答案数组加1
				tempAnswer[indexAnswer]=pArray1[ii];//给临时答案数组赋值
			}
		}
		ArrayList<C2DPoint> c2d=new ArrayList<C2DPoint>();//创建C2DPoint列表
		for(int p=0;p<tempAnswer.length;p++)//遍历Point数组
		{
			if(tempAnswer[p]!=null)
			{
				c2d.add(new C2DPoint(tempAnswer[p].x,tempAnswer[p].y));//将点加进列表中
			}
		}
		C2DPolygon gon=new C2DPolygon();//创建多边形对象
		gon.Create(c2d,true);//创建
		return gon;
	}
}
