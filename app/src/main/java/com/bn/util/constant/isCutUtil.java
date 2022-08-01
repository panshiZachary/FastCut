package com.bn.util.constant;

import java.util.ArrayList;

import com.bn.fastcut.MySurfaceView;
import com.bn.object.BNObject;
import uk.co.geolib.geolib.CGrid;
import uk.co.geolib.geopolygons.C2DHoledPolygon;
import uk.co.geolib.geopolygons.C2DPolygon;

public class isCutUtil{
	//判断手划过的线段与图形中的线段是否相交
	public static boolean isIntersect(float x1,float y1,float x2,float y2,float x3,float y3,float x4,float y4)
	{
		if(x1==x2||x3==x4)//x1=x2 或者x3=x4直接返回false
		{
			return false;
		}
		float k1 = (float)(y1-y2)/(float)(x1-x2);//求第一条直线的斜率
		float b1 = (float)(x1*y2 - x2*y1)/(float)(x1-x2);
		float k2 = (float)(y3-y4)/(float)(x3-x4);//求第二条直线的斜率
		float b2 = (float)(x3*y4 - x4*y3)/(float)(x3-x4);
		if(k1==k2)//如果斜率相同
		{
			return false;//直接返回false
		}else
		{
			float x = (float)(b2-b1) / (float)(k1-k2);
			//差0.1即认为相等  相当于模糊计算
			if((((x+0.1)>=x1)&&((x-0.1)<=x2))||(((x+0.1)>=x2)&&(x-0.1)<=x1))
			{
				if((((x+0.1)>=x3)&&((x-0.1)<=x4))||(((x+0.1)>=x4)&&(x-0.1)<=x3))
				{
					return true;
				}
			}
			return false;
		}
	}
	//获得两条线段的交点
	public static float[] getIntersectPoint(float x1,float y1,float x2,float y2,float x3,float y3,float x4,float y4)
	{
		float[] result=new float[2];
		result[0] = (((x1 - x2) * (x3 * y4 - x4 * y3) - (x3 - x4) * (x1 * y2 - x2 * y1))
				/ ((x3 - x4) * (y1 - y2) - (x1 - x2) * (y3 - y4)));

		result[1] = ((y1 - y2) * (x3 * y4 - x4 * y3) - (x1 * y2 - x2 * y1) * (y3 - y4))
				/ ((y1 - y2) * (x3 - x4) - (x1 - x2) * (y3 - y4));
		return result;
	}
	//判断是否划到了多边形
	public static boolean isCutPolygon(MySurfaceView mv,C2DPolygon cp,float x1,float y1,float x2,float y2)
	{
		int indexTemp=MySurfaceView.CheckpointIndex;
		float[] polygonData=GeoLibUtil.fromC2DPolygonToVData(cp);//获取现在多边形性状的点坐标
		boolean[] isIntersect=new boolean[polygonData.length/2];//存取线段是否相交的数组
		int boolCount=0;//数组变量自加值
		int falseCount=0;//不相交线段的数量
		int index[]=new int[2];
		int findCount=0;
		for(int j=0;j<polygonData.length/2;j++)
		{
			float[] data=MyFCData.getData(polygonData,j);//获取一条边的x、y坐标
			isIntersect[boolCount]=isIntersect(x1, y1, x2, y2,
					data[0], data[1], data[2], data[3]);//判断两条线段是否相交
			//判断是否划到了 不可切割的边=========================start==========================
			if(isIntersect[boolCount])//如果线段与线段相交
			{
				for(int k=0;k<MyFCData.data[indexTemp].length/2;k++)//循环最初数据的数组
				{
					if(((int)data[0]==MyFCData.data[indexTemp][k*2])&&((int)data[1]==MyFCData.data[indexTemp][k*2+1]))//如果获得的边的第一个x坐标在最初数据里能够找到
					{
						index[0]=k;//记录此点在最初数据中的位置
						findCount++;//计数器加1
					}
					if(((int)data[2]==MyFCData.data[indexTemp][k*2])&&((int)data[3]==MyFCData.data[indexTemp][k*2+1]))//如果获得的边的第二个x坐标在最初数据里能够找到
					{
						index[1]=k;//记录此点在最初数据中的位置
						findCount++;//计数器加1
					}
				}
				if(findCount>0&&findCount%2==0)//如果是偶数个
				{
					if(index[0]>index[1])
					{
						if(index[1]==0)//如果是最大值-0  则判断最大值的边的boolean值
						{
							if(!MyFCData.dataBool[indexTemp][index[0]])//判断该条边是否可以切割
							{
								mv.isCutRigid=true;
								mv.intersectPoint=getIntersectPoint(x1, y1, x2, y2,
										data[0], data[1], data[2], data[3]);
								return false;//如果不能切割  则直接返回false
							}
						}else
						{
							if(!MyFCData.dataBool[indexTemp][index[1]])//判断该条边是否可以切割
							{
								mv.isCutRigid=true;
								mv.intersectPoint=getIntersectPoint(x1, y1, x2, y2,
										data[0], data[1], data[2], data[3]);
								return false;//如果不能切割  则直接返回false
							}
						}
					}else
					{
						if(!MyFCData.dataBool[indexTemp][index[0]])//判断该条边是否可以切割
						{
							mv.isCutRigid=true;
							mv.intersectPoint=getIntersectPoint(x1, y1, x2, y2,
									data[0], data[1], data[2], data[3]);
							return false;//如果不能切割  则直接返回false
						}
					}
					findCount=0;
				}else
				{
					findCount=0;
				}
			}
			//判断是否划到了 不可切割的边=========================end==========================
			if(isIntersect[boolCount]==false)//如果线段不相交
			{
				falseCount++;//数量加1
			}
			boolCount++;//数组变量自加1
		}
		if((falseCount==isIntersect.length)||(falseCount==isIntersect.length-1))
		{
			return false;
		}else
		{
			return true;
		}
	}
	//获得切分区域已经进行分类的多边形列表
	public static ArrayList<C2DPolygon> getCutPolysArrayList(MySurfaceView mv,ArrayList<BNObject> alBNPO,float lxs,float lys,float lxe,float lye)
	{
		ArrayList<C2DPolygon> onePolygon=new ArrayList<C2DPolygon>();//切割后每个区域只有一个多边形的列表
		ArrayList<C2DPolygon> tempPolygon=new ArrayList<C2DPolygon>();//切割后每个区域内有多个多边形的列表
		ArrayList<ArrayList<float[]>> tal=GeoLibUtil.calParts(lxs, lys, lxe,lye);//分成两个多边形区域
		C2DPolygon[] cpA=GeoLibUtil.createPolys(tal);//创建两个多边形
		for(C2DPolygon cpTemp:cpA)
		{
			ArrayList<C2DHoledPolygon> polys = new ArrayList<C2DHoledPolygon>();
			//获得被切分成的两个多边形与基本图形的重叠部分，并放入polys列表中
			cpTemp.GetOverlaps(alBNPO.get(0).cp, polys, new CGrid());
			if(polys.size()==1)//如果该区域内只有一个多边形
			{
				onePolygon.add(polys.get(0).getRim());//直接加进列表中
			}else//如果该区域内有多个多边形
			{
				for(C2DHoledPolygon chp:polys)//将多个多边形加进列表中
				{
					tempPolygon.add(chp.getRim());
				}
			}
		}
		ArrayList<C2DPolygon> result=getLastPolysArrayList(mv,onePolygon,tempPolygon,lxs, lys, lxe,lye);
		return result;
	}
	//判断切分的具体是哪个多边形 经过合并等操作 返回多边形列表
	public static ArrayList<C2DPolygon> getLastPolysArrayList(MySurfaceView mv,ArrayList<C2DPolygon> onePolygon,ArrayList<C2DPolygon> tempPolygon,float lxs,float lys,float lxe,float lye)
	{
		ArrayList<C2DPolygon> lastPolygons=new ArrayList<C2DPolygon>();//最后根据球来判断切分多边形的列表
		ArrayList<C2DPolygon> canCombineP=new ArrayList<C2DPolygon>();//手没有划到 可以进行合并多边形的列表
		//判断切分的具体是哪个多边形==================start======================
		if(tempPolygon.size()>0)//如果一个区域内有多个多边形
		{
			for(C2DPolygon cp:tempPolygon)//遍历C2DPolygon对象
			{
				if(isCutPolygon(mv,cp, lxs, lys, lxe, lye))//判断手是否划到了该多边形对象
				{
					lastPolygons.add(cp);
				}else
				{
					canCombineP.add(cp);//没划到的多边形放进canCombine列表中
				}
			}
			if(canCombineP.size()>0)//如果存在能够合并的多边形
			{
				C2DPolygon cc=new C2DPolygon();
				for(int i=0;i<canCombineP.size();i++)//遍历能够合并的多边形列表
				{
					if(i==0)//如果是第一次循环
					{
						cc=onePolygon.get(0);//赋初值
					}
					cc=MyPatchUtil.getCombinePolygon//合并多边形
							(
									MyPatchUtil.getPolygonData(canCombineP.get(i), canCombineP.get(i).IsClockwise()),
									MyPatchUtil.getPolygonData(cc, cc.IsClockwise()),
									MyPatchUtil.getPolygonData(canCombineP.get(i), canCombineP.get(i).IsClockwise()).length,
									MyPatchUtil.getPolygonData(cc, cc.IsClockwise()).length
							);
				}
				lastPolygons.add(cc);//将合并后的多边形添加进列表里面
			}else//如果不存在能够合并的多边形
			{
				lastPolygons.add(onePolygon.get(0));//即直接添加进最后的列表中
			}
		}else//如果一个区域内只有一个多边形
		{
			for(int i=0;i<onePolygon.size();i++)
			{
				lastPolygons.add(onePolygon.get(i));//直接添加进列表中
			}
		}
		//判断切分的具体是哪个多边形==================end======================
		return lastPolygons;
	}
}
