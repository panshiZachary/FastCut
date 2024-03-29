package com.bn.util.snow;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.bn.util.constant.MatrixState;

import android.opengl.GLES20;
//纹理三角形
public class ParticleForDraw
{
    int muMVPMatrixHandle;//总变换矩阵引用id
    int muSjFactor;//衰减因子引用id
    int muBj;//半径引用id
    int muStartColor;//起始颜色引用id
    int muEndColor;//终止颜色引用id
    int maPositionHandle; //顶点位置属性引用id
    int maTexCoorHandle; //顶点纹理坐标属性引用id
    String mVertexShader;//顶点着色器
    String mFragmentShader;//片元着色器

    FloatBuffer   mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer   mTexCoorBuffer;//顶点纹理坐标数据缓冲
    int vCount=0;
    float halfSize;
    boolean initFlag=false;//判断是否初始化着色器
    int programId;//自定义渲染管线程序id
    int textureId;//纹理图片Id
    public ParticleForDraw(float halfSize,int programId,int textureId)
    {
        this.halfSize=halfSize;
        this.programId=programId;
        this.textureId=textureId;
        //初始化顶点坐标与着色数据
        initVertexData(halfSize);
    }

    //初始化顶点坐标与着色数据的方法
    public void initVertexData(float halfSize)
    {
        //顶点坐标数据的初始化================begin============================
        vCount=6;
        float vertices[]=new float[]
                {
                        -halfSize,halfSize,0,
                        -halfSize,-halfSize,0,
                        halfSize,halfSize,0,

                        -halfSize,-halfSize,0,
                        halfSize,-halfSize,0,
                        halfSize,halfSize,0
                };

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
        float texCoor[]=new float[]//顶点颜色值数组，每个顶点4个色彩值RGBA
                {
                        0,0, 0,1, 1,0,
                        0,1, 1,1, 1,0
                };
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

    //初始化着色器
    public void initShader()
    {
        //获取程序中顶点位置属性引用id
        maPositionHandle = GLES20.glGetAttribLocation(programId, "aPosition");
        //获取程序中顶点纹理坐标属性引用id
        maTexCoorHandle= GLES20.glGetAttribLocation(programId, "aTexCoor");
        //获取程序中总变换矩阵引用id
        muMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
        //获取程序中衰减因子引用id
        muSjFactor=GLES20.glGetUniformLocation(programId, "sjFactor");
        //获取程序中半径引用id
        muBj=GLES20.glGetUniformLocation(programId, "bj");
        //获取起始颜色引用id
        muStartColor=GLES20.glGetUniformLocation(programId, "startColor");
        //获取终止颜色引用id
        muEndColor=GLES20.glGetUniformLocation(programId, "endColor");
    }

    public void drawSelf(float sj,float[] startColor,float[] endColor)
    {
        if(!initFlag)
        {
            //初始化着色器
            initShader();
            initFlag=true;
        }
        //制定使用某套shader程序
        GLES20.glUseProgram(programId);
        //将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState.getFinalMatrix(), 0);
        //将衰减因子传入shader程序
        GLES20.glUniform1f(muSjFactor, sj);
        //将半径传入shader程序
        GLES20.glUniform1f(muBj, halfSize);
        //将起始颜色送入渲染管线
        GLES20.glUniform4fv(muStartColor, 1, startColor, 0);
        //将终止颜色送入渲染管线
        GLES20.glUniform4fv(muEndColor, 1, endColor, 0);
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        //绘制纹理矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vCount);
    }
}
