package com.bn.util.snow;

import com.bn.util.constant.MatrixState;

public class ParticleSingle
{
    public float x;
    public float y;
    public float vx;
    public float vy;
    public float lifeSpan;

    ParticleForDraw fpfd;

    public ParticleSingle(float x,float y,float vx,float vy,ParticleForDraw fpfd)
    {
        this.x=x;
        this.y=y;
        this.vx=vx;
        this.vy=vy;
        this.fpfd=fpfd;
    }

    public void go(float lifeSpanStep)
    {
        //粒子进行移动的方法，同时岁数增大的方法
        y=y+vy;
        lifeSpan+=lifeSpanStep;
    }

    public void drawSelf(float[] startColor,float[] endColor,float maxLifeSpan){
        MatrixState.pushMatrix();//保护现场
        MatrixState.translate(x, y, 0);
        float sj=(maxLifeSpan-lifeSpan)/maxLifeSpan;//衰减因子在逐渐的变小，最后变为0
        fpfd.drawSelf(sj,startColor,endColor);//绘制单个粒子
        MatrixState.popMatrix();//恢复现场
    }
}
