package com.bn.util.constant;

import static com.bn.util.constant.Constant.StandardScreenHeight;
import static com.bn.util.constant.Constant.StandardScreenWidth;

import java.util.ArrayList;
import uk.co.geolib.geolib.C2DPoint;
import uk.co.geolib.geopolygons.C2DPolygon;

public class GeoLibUtil
{
	static final float xmin=0;
	static final float xmax=StandardScreenWidth;
	static final float ymin=0;
	static final float ymax=StandardScreenHeight;
	public static C2DPolygon createPoly(float[] polyData)//根据顶点数据创建多边形
	{
		ArrayList<C2DPoint> al=new ArrayList<C2DPoint>();//创建ArrayList<C2DPoint>对象
		for(int i=0;i<polyData.length/2;i++)//遍历顶点数据
		{
			C2DPoint tempP=new C2DPoint(polyData[i*2],polyData[i*2+1]);//创建C2DPoint对象（x，y）
			al.add(tempP);//将C2DPoint对象放入ArrayList中
		}
		C2DPolygon p = new C2DPolygon();//创建C2DPolygon对象
		p.Create(al, true);//创建可选的重新排序的多边形点
		return p;
	}
	//创建切分后的两个多边形对象
	public static C2DPolygon[] createPolys(ArrayList<ArrayList<float[]>> alIn)
	{
		C2DPolygon[] cps=new C2DPolygon[2];
		int index=0;
		for(ArrayList<float[]> p:alIn)
		{
			cps[index]=new C2DPolygon();
			ArrayList<C2DPoint> al=new ArrayList<C2DPoint>();
			for(float[] fa:p)
			{
				C2DPoint tempP=new C2DPoint(fa[0],fa[1]);
				al.add(tempP);
			}
			cps[index].Create(al, true);
			index++;
		}
		return cps;
	}
	public static ArrayList<Float> fromConvexToTris(ArrayList<float[]> points)//将凸多边形数据转换成三角形数据
	{
		ArrayList<Float> result=new ArrayList<Float>();
		int count=points.size();
		for(int i=0;i<count-2;i++)
		{
			float[] d1=points.get(0);//获得三角形的第一个顶点数据
			float[] d2=points.get(i+1);//获得三角形的第二个顶点数据
			float[] d3=points.get(i+2);//获得三角形的第三个顶点数据

			result.add(d1[0]);result.add(d1[1]);//将三角形的第一个顶点数据放进列表中
			result.add(d2[0]);result.add(d2[1]);//将三角形的第二个顶点数据放进列表中
			result.add(d3[0]);result.add(d3[1]);//将三角形的第三个顶点数据放进列表中
		}
		return result;
	}
	public static float[] fromAnyPolyToTris(float[] vdata)//将多边形数据转成三角形顶点数据
	{
		C2DPolygon cp=createPoly(vdata);//将多边形的顶点数据组成C2DPolygon对象
		ArrayList<C2DPolygon> subAreas = new ArrayList<C2DPolygon>();
		cp.ClearConvexSubAreas();//清除凸子区域
		cp.CreateConvexSubAreas();//创建凸子区域
		cp.GetConvexSubAreas(subAreas);//获得凸子区域
		ArrayList<Float> resultData=new ArrayList<Float>();
		for(C2DPolygon cpTemp:subAreas)//遍历C2DPolygon对象
		{
			ArrayList<float[]> points=new ArrayList<float[]>();
			ArrayList<C2DPoint> alp=new ArrayList<C2DPoint>();
			cpTemp.GetPointsCopy(alp);//将多边形的顶点数据拷贝到ArrayList<C2DPoint>中
			for(C2DPoint p:alp)
			{
				float[] fa=new float[]{(float)p.x,(float)p.y};
				points.add(fa);//将C2DPoint转换为float数组
			}
			ArrayList<Float> tempConvex=fromConvexToTris(points);//根据多边形的顶点数据将凸多边形转换成三角形
			for(Float f:tempConvex)
			{
				resultData.add(f);//将三角形顶点数据放进ArrayList<Float>中
			}
		}
		float[] result=new float[resultData.size()];//将ArrayList<Float>转成一维数组

		for(int i=0;i<resultData.size();i++)
		{
			result[i]=resultData.get(i);
		}
		return result;
	}
	public static float[] fromC2DPolygonToVData(C2DPolygon cp)//将C2DPolygon对象转换成顶点数组
	{
		ArrayList<float[]> points=new ArrayList<float[]>();
		ArrayList<C2DPoint> alp=new ArrayList<C2DPoint>();
		cp.GetPointsCopy(alp);//将多边形的顶点数据拷贝到ArrayList<C2DPoint>中
		for(C2DPoint p:alp)//遍历ArrayList<C2DPoint>对象
		{
			float[] fa=new float[]{(float)p.x,(float)p.y};
			points.add(fa);//将C2DPoint转换为float数组
		}
		float[] result=new float[points.size()*2];
		for(int i=0;i<points.size();i++)//将ArrayList<float[]>对象转换成float[]
		{
			float[] p=points.get(i);
			result[i*2]=p[0];
			result[i*2+1]=p[1];
		}
		return result;
	}
	//切分多边形
	public static ArrayList<ArrayList<float[]>> calParts(float sx,float sy,float ex,float ey)
	{
		// 0[xmin,ymin]----------3[xmax,ymin]
		// |                              |
		// |                              |
		// 1[xmin,ymax]----------2[xmax,ymax]
		int currIndex=0;
		ArrayList<float[]> al=new ArrayList<float[]>();
		al.add(new float[]{xmin,ymin});
		currIndex++;
		int jd1Index=-1;
		int jd2Index=-1;

		//求0-1线段与传入切割线的交点 X=xmin
		float t=(xmin-sx)/(ex-sx);
		float y=(ey-sy)*t+sy;
		if(y>ymin&&y<ymax)
		{
			jd1Index=currIndex;
			al.add(new float[]{xmin,y});
			currIndex++;
		}

		al.add(new float[]{xmin,ymax});
		currIndex++;

		//求1-2线段传入切割线的交点 y=ymax
		t=(ymax-sy)/(ey-sy);
		float x=(ex-sx)*t+sx;
		if(x>xmin&&x<xmax)
		{
			if(jd1Index==-1)
			{
				jd1Index=currIndex;
			}
			else
			{
				jd2Index=currIndex;
			}
			al.add(new float[]{x,ymax});
			currIndex++;
		}

		al.add(new float[]{xmax,ymax});
		currIndex++;

		//求2-3线段传入切割线的交点 x=xmax
		t=(xmax-sx)/(ex-sx);
		y=(ey-sy)*t+sy;
		if(y>ymin&&y<ymax)
		{
			if(jd1Index==-1)
			{
				jd1Index=currIndex;
			}
			else
			{
				jd2Index=currIndex;
			}
			al.add(new float[]{xmax,y});
			currIndex++;
		}
		al.add(new float[]{xmax,ymin});
		currIndex++;

		//求3--0线段传入切割线的交点 y=ymin
		t=(ymin-sy)/(ey-sy);
		x=(ex-sx)*t+sx;
		if(x>xmin&&x<xmax)
		{
			if(jd1Index==-1)
			{
				jd1Index=currIndex;
			}
			else
			{
				jd2Index=currIndex;
			}
			al.add(new float[]{x,ymin});
			currIndex++;
		}
		//卷绕第一个多边形
		ArrayList<float[]> p1=new ArrayList<float[]>();
		int startIndex=jd1Index;
		while(true)
		{
			p1.add(al.get(startIndex));
			if(startIndex==jd2Index)
			{
				break;
			}
			startIndex=(startIndex+1)%al.size();
		}
		//卷绕第二个多边形
		ArrayList<float[]> p2=new ArrayList<float[]>();
		startIndex=jd2Index;
		while(true)
		{
			p2.add(al.get(startIndex));
			if(startIndex==jd1Index)
			{
				break;
			}
			startIndex=(startIndex+1)%al.size();
		}
		ArrayList<ArrayList<float[]>> result=new ArrayList<ArrayList<float[]>>();
		result.add(p1);
		result.add(p2);
		return result;
	}
}