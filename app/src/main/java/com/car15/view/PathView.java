package com.car15.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.car15.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import android.content.SharedPreferences;
import java.io.File;
import org.json.JSONArray;

/**
 * Created by 思宁 on 2015/7/30.
 * 巡逻路线 view
 */
public class PathView extends View {
    //发送给小车
    private int point[][]=new int[50][2];
    private int sendpoint[][]=new int[50][2];

    //其他数据
    Paint p = new Paint();
    private double windowsizeX;
    private double windowsizeY;

    //路径规划有关
    private int mouseX1,mouseY1,mouseX2,mouseY2;
    private int history[][]=new int[100][4];
    private int historyindex[][]=new int[100][4];
    private int num=0;
    private int paintee=20;//20 //绘图精度
    private int positionee; //12   //定位误差  >=0.5*paintee
    private int chooseX,chooseY;
    private int choosehistory[][]=new int[40][2];
    private int choosehistoryij[][]=new int[40][2];
    private int choosenum=0;
    private int deviationee=500;//灵敏度  //500~700
    private int sign_buildtree=0;//是否生成关键点
    private int sign_setobstacle=0;//模式切换
    private int sign_buildpath=0;//是否生成路径

    private int gridmethod=0;//是否用纯栅格法 1为用 0为原来的
    private int gridnum=0;//栅格法每行数目nXn

    ArrayList<Integer> Xn = new ArrayList<Integer>();
    ArrayList<Integer> Yn = new ArrayList<Integer>();
    ArrayList<Integer> OPEN_X = new ArrayList<Integer>();
    ArrayList<Integer> OPEN_Y = new ArrayList<Integer>();
    ArrayList<Integer> OPEN_F = new ArrayList<Integer>();

    private int Xsmall=0;
    private int Ysmall=0;
    private int Xlarge=100;
    private int Ylarge=100;

    private Boolean success=false;
    private int Astarpaint=0;//是否绘图
    // private int isend=0;

    public PathView(Context context){

        super(context);

        try {
            File f = new File("/sdcard/car_history.txt");
            if(f.isFile()&&f.exists()){
                InputStreamReader read = new InputStreamReader(new FileInputStream(f),"UTF-8");
                BufferedReader reader=new BufferedReader(read);

                String data[]=new String[50];

                String strLine = null;
                int i=0;
                while((strLine =  reader.readLine()) != null)
                {
                    data[i]=strLine;
                    i++;
                }
                read.close();
                num=Integer.parseInt(data[0]);
                i=1;
                for(int j=0;j<num;j++)
                {
                    history[j][0]=Integer.parseInt(data[i]);i++;
                    history[j][1]=Integer.parseInt(data[i]);i++;
                    history[j][2]=Integer.parseInt(data[i]);i++;
                    history[j][3]=Integer.parseInt(data[i]);i++;
                }

            }
        } catch (Exception e) {
            System.out.println("读取文件内容操作出错");
            e.printStackTrace();
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // isend=0;
        Log.i("ondraw-->", "---start---");
        // Log.i("windowsizeX-->",windowsizeX+"");
        p.setStrokeWidth(5);
        p.setColor(R.drawable.btn_pressed_blue);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect((int) (Xsmall * windowsizeX), (int) (Ysmall * windowsizeY), (int) (Xlarge * windowsizeX), (int) (Ylarge * windowsizeY), p);

        for(int i=0;i<num;i++){
            // System.out.println(history[i][0]+"|"+history[i][1]+"|"+history[i][2]+"|"+history[i][3]);
            p.setColor(Color.LTGRAY);
            canvas.drawRect(history[i][0],
                    history[i][1],
                    history[i][2],
                    history[i][3], p);
            Log.i("history[i][0]-->",history[i][0]+";"+history[i][2]);
        }
        System.out.println("");

        p.setColor(R.drawable.btn_pressed_blue);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect((int) (Xsmall*windowsizeX), (int) (Ysmall*windowsizeY), (int) (Xlarge * windowsizeX), (int) (Ylarge* windowsizeY), p);
        if(sign_buildtree==1){
            int bool[][]=new int[Yn.size()-1][Xn.size()-1];
            int weightbool[][]=new int[Yn.size()-1][Xn.size()-1];
            int POINTlocation[][][]=new int[Yn.size()-1][Xn.size()-1][2];


            for(int i=0;i<num;i++)
            {
                for(int j=historyindex[i][0];j<=historyindex[i][2]-1;j++)
                {
                    for(int k=historyindex[i][1];k<=historyindex[i][3]-1;k++)
                    {
                        bool[k][j]=1;
                    }
                }
            }

            weightbool=bool;
//            System.out.println("\nbool1");
//            for(int i=0;i<Yn.size()-1;i++)
//            {
//                for(int j=0;j<Xn.size()-1;j++)
//                {System.out.print(bool[i][j]+"|");}
//            }
//            System.out.println("\nbool2");

            for(int i=0;i<Yn.size()-1;i++)
            {
                for(int j=0;j<Xn.size()-1;j++)
                {
                    int x,y;
                    if(i<Yn.size()-1)
                        y=(Yn.get(i)+Yn.get(i+1))/2;
                    else
                        y=(Yn.get(i)+(int)(Ylarge*windowsizeY))/2;//300

                    if(j<Xn.size()-1)
                        x=(Xn.get(j)+Xn.get(j+1))/2;
                    else
                        x=(Xn.get(j)+(int)(Xlarge*windowsizeX))/2;//250
                    POINTlocation[i][j][0]=x;
                    POINTlocation[i][j][1]=y;
                    if(bool[i][j]!=1)
                    {
                        p.setColor(R.drawable.gray_red);
                        p.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(x, y, 5, p);
                    }
                }
            }
            System.out.println("\nhhscolorend");
            //选中节点
            if(sign_setobstacle==1){
                int usefulchoosenum=0;
                System.out.print("\nchoosenum="+choosenum+"\n");

                for(int k=0;k<choosenum;k++){
                    int end=0;
                    for(int i=0;i<Yn.size()-1;i++)
                    {
                        if(end==1) break;
                        for(int j=0;j<Xn.size()-1;j++)
                        {
                            if(end==1) break;
                            if(bool[i][j]!=1)
                            {
                                int chooseXdeviation,chooseYdeviation;
                                chooseXdeviation=choosehistory[k][0]-POINTlocation[i][j][0];
                                chooseYdeviation=choosehistory[k][1]-POINTlocation[i][j][1];

                                if(chooseXdeviation*chooseXdeviation
                                        +chooseYdeviation*chooseYdeviation<deviationee)
                                {
                                    p.setColor(R.drawable.btn_blue);
                                    p.setStyle(Paint.Style.STROKE);
                                    canvas.drawCircle(POINTlocation[i][j][0] , POINTlocation[i][j][1] , 10, p);
                                    choosehistoryij[k][0]=j;//x
                                    choosehistoryij[k][1]=i;//y
                                    //weightbool[i][j]=65535;
                                    usefulchoosenum++;
                                    end=1;

                                }
                                // else k--;
                            }
                        }
                    }
                }
                choosenum=usefulchoosenum;

                //要加的有加权权值，k*障碍物距离+p*路程+l*拐弯权值（避免多次转弯）
                //int weightbool[][]=new int[Yn.size()-1][Xn.size()-1];
                if(sign_buildpath==1)//生成路径
                {
                    int sendpointsign=0;
                    int pointi=0;

                    for(int ijnum=0;ijnum<choosenum-1;ijnum++){

                        int xbegin=0,ybegin=0,xend=0,yend=0;

                        ybegin= choosehistoryij[ijnum][1];
                        xbegin= choosehistoryij[ijnum][0];
                        yend= choosehistoryij[ijnum+1][1];
                        xend= choosehistoryij[ijnum+1][0];


                        // Weightorientation.clear();
                        OPEN_X.clear();
                        OPEN_Y.clear();
                        OPEN_F.clear();


//                    	System.out.println("choosenum="+choosenum);
//                        for(int iii=0;iii<choosenum;iii++){
//                        	System.out.println("choosehistoryij[iii][xy]="+choosehistoryij[iii][0]);
//                        	System.out.println("choosehistoryij[iii][xy]="+choosehistoryij[iii][1]);
//                        }
//                		System.out.println("choosehistoryij[ijnum][1]="+choosehistoryij[ijnum][1]);
//                		System.out.println("choosehistoryij[ijnum][0]="+ choosehistoryij[ijnum][0]);
//                		System.out.println("choosehistoryij[ijnum+1][1]="+ choosehistoryij[ijnum+1][1]);
//                		System.out.println(" choosehistoryij[ijnum+1][0]="+ choosehistoryij[ijnum+1][0]);
//                		System.out.println("choosehistoryij[ijnum+2][1]="+ choosehistoryij[ijnum+2][1]);
//                		System.out.println(" choosehistoryij[ijnum+2][0]="+ choosehistoryij[ijnum+2][0]);
//                 		System.out.println("choosehistoryij[ijnum+2][1]="+ choosehistoryij[ijnum+2][1]);
//                		System.out.println(" choosehistoryij[ijnum+2][0]="+ choosehistoryij[ijnum+2][0]);
//
                        System.out.println("");

                        System.out.print("ybegin="+ybegin+"|");
                        System.out.print("xbegin="+xbegin+"|");
                        System.out.print("yend="+yend+"|");
                        System.out.print("xend="+xend+"|");
                        System.out.println("");

//                    for(ybegin=0;ybegin<Yn.size()-1;ybegin++)//查找起始点
//                    {
//                        for (xbegin=0;xbegin<Xn.size()-1;xbegin++)
//                        {
//                            if(weightbool[ybegin][xbegin]==65535) end=1;
//                            if(end==0) continue;
//                            else if(end==1) break;
//                        }
//
//                        if(end==0) continue;
//                        else if(end==1) break;
//                    }
//                    weightbool[ybegin][xbegin]=0;
//                    end=0;
//                    for(yend=0;yend<Yn.size()-1;yend++)//查找终止点
//                    {
//
//                        for (xend=0;xend<Xn.size()-1;xend++)
//                        {
//                            if(weightbool[yend][xend]==65535) end=1;
//                            if(end==0) continue;
//                            else if(end==1) break;
//                        }
//
//                        if(end==0) continue;
//                        else if(end==1) break;
//                    }
//                    weightbool[yend][xend]=0;


                        //A-star优化算法 F=G+H
                        //问题S
                        int sorttemp;

                        Astarpaint=0;//是否绘图
                        double Astark=0.5;//1   if值比小就赋值生成 else淘汰（撞墙）
                        //自适应Astark
                        if(Xn.size()<20) Astark=0.5;
                        else if(Xn.size()<30) Astark=0.8;
                        else if(Xn.size()>=30) Astark=1.0;
                        //weightbool转换

                        for(int i=0;i<Yn.size()-1;i++)
                        {
                            for(int j=0;j<Xn.size()-1;j++)
                            {
                                if(weightbool[i][j]==0)
                                    weightbool[i][j]=-2;//未探知
                                else if (weightbool[i][j]==1)
                                    weightbool[i][j]=-1;//障碍物
                            }
                        }

                        System.out.print("\n");

//                  System.out.println("\nweightbooltemp");
//                  for(int i=0;i<Yn.size()-1;i++)
//                  {
//                      for(int j=0;j<Xn.size()-1;j++)
//                      {System.out.print(weightbooltemp[i][j]+"|");}
//                  }
//                  System.out.println("\nweightbooltemp");


                        weightbool[yend][xend]=0;//距离权值
                        OPEN_X.add(xend);
                        OPEN_Y.add(yend);

                        OPEN_F.add((int)(Astark*(Math.abs(ybegin-yend)+
                                Math.abs(xbegin-xend) )));


                        //排序
                        for(;;){
                            if(OPEN_X.size()==0){ //无解


                                // JOptionPane.showMessageDialog(null, "提示：找不到一条合适的路径！");
                                break;
                            }
                            else if(OPEN_X.size()>1){
                                for(int i=0;i<OPEN_F.size();i++)//这里用冒泡排序(从小到大)
                                {
                                    for(int j=0;j<OPEN_F.size()-i-1;j++)
                                    {
                                        if(OPEN_F.get(j)>OPEN_F.get(j+1))  //相等了也换一下，增大随机性 //=
                                        {///////////////////////
                                            sorttemp=OPEN_F.get(j);//OPEN_F
                                            OPEN_F.set(j, OPEN_F.get(j+1));
                                            OPEN_F.set(j+1, sorttemp);
                                            sorttemp=OPEN_X.get(j);//OPEN_X
                                            OPEN_X.set(j, OPEN_X.get(j+1));
                                            OPEN_X.set(j+1, sorttemp);
                                            sorttemp=OPEN_Y.get(j);//OPEN_Y
                                            OPEN_Y.set(j, OPEN_Y.get(j+1));
                                            OPEN_Y.set(j+1, sorttemp);
                                        }
                                    }
                                }
                            }
                            //取出OPEN表中的第一个坐标
                            if(OPEN_X.get(0)!=0)//左
                            {
                                if(OPEN_Y.get(0)==ybegin&&(OPEN_X.get(0)-1)==xbegin)//判断是不是起始点
                                {
                                    weightbool[OPEN_Y.get(0)][OPEN_X.get(0)-1]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;

                                    Astarpaint=1;//找到一条路径，绘图
                                    break;
                                }

                                else if( weightbool[OPEN_Y.get(0)][OPEN_X.get(0)-1]==-2||
                                        weightbool[OPEN_Y.get(0)][OPEN_X.get(0)-1]>0&&
                                                weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1<weightbool[OPEN_Y.get(0)][OPEN_X.get(0)-1])
                                {
                                    //G
                                    weightbool[OPEN_Y.get(0)][OPEN_X.get(0)-1]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;
                                    OPEN_X.add(OPEN_X.get(0)-1);
                                    OPEN_Y.add(OPEN_Y.get(0));
                                    //H
                                    OPEN_F.add((int)(Astark*(Math.abs(ybegin-OPEN_Y.get(0))+
                                            Math.abs(xbegin-(OPEN_X.get(0)-1)) ))+
                                            weightbool[OPEN_Y.get(0)][OPEN_X.get(0)-1]);
                                }
                            }
                            if(OPEN_Y.get(0)!=0)//上
                            {
                                if(OPEN_Y.get(0)-1==ybegin&&OPEN_X.get(0)==xbegin)//判断是不是起始点
                                {
                                    weightbool[OPEN_Y.get(0)-1][OPEN_X.get(0)]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;

                                    Astarpaint=1;//找到一条路径，绘图
                                    break;
                                }
                                else if(weightbool[OPEN_Y.get(0)-1][OPEN_X.get(0)]==-2||
                                        weightbool[OPEN_Y.get(0)-1][OPEN_X.get(0)]>0&&
                                                weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1< weightbool[OPEN_Y.get(0)-1][OPEN_X.get(0)])
                                {
                                    //G
                                    weightbool[OPEN_Y.get(0)-1][OPEN_X.get(0)]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;

                                    OPEN_X.add(OPEN_X.get(0));
                                    OPEN_Y.add(OPEN_Y.get(0)-1);
                                    //H
                                    OPEN_F.add((int)(Astark*(Math.abs(ybegin-(OPEN_Y.get(0)-1))+
                                            Math.abs(xbegin-(OPEN_X.get(0))) ))+
                                            weightbool[OPEN_Y.get(0)-1][OPEN_X.get(0)]); }
                            }
                            if(OPEN_X.get(0)<Xn.size()-2)//右
                            {
                                if(OPEN_Y.get(0)==ybegin&&(OPEN_X.get(0)+1)==xbegin)//判断是不是起始点
                                {
                                    weightbool[OPEN_Y.get(0)][OPEN_X.get(0)+1]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;

                                    Astarpaint=1;//找到一条路径，绘图
                                    break;
                                }
                                else if( weightbool[OPEN_Y.get(0)][OPEN_X.get(0)+1]==-2||
                                        weightbool[OPEN_Y.get(0)][OPEN_X.get(0)+1]>0&&
                                                weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1< weightbool[OPEN_Y.get(0)][OPEN_X.get(0)+1])

                                {
                                    //G
                                    weightbool[OPEN_Y.get(0)][OPEN_X.get(0)+1]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;
                                    OPEN_X.add(OPEN_X.get(0)+1);
                                    OPEN_Y.add(OPEN_Y.get(0));
                                    //H
                                    OPEN_F.add((int)(Astark*(Math.abs(ybegin-OPEN_Y.get(0))+
                                            Math.abs(xbegin-(OPEN_X.get(0)+1)) ))+
                                            weightbool[OPEN_Y.get(0)][OPEN_X.get(0)+1]);
                                }
                            }
                            if(OPEN_Y.get(0)<Yn.size()-2)//下
                            {
                                if(OPEN_Y.get(0)+1==ybegin&&OPEN_X.get(0)==xbegin)//判断是不是起始点
                                {
                                    weightbool[OPEN_Y.get(0)+1][OPEN_X.get(0)]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;

                                    Astarpaint=1;//找到一条路径，绘图
                                    break;
                                }
                                else if(weightbool[OPEN_Y.get(0)+1][OPEN_X.get(0)]==-2||
                                        weightbool[OPEN_Y.get(0)+1][OPEN_X.get(0)]>0&&
                                                weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1<  weightbool[OPEN_Y.get(0)+1][OPEN_X.get(0)])
                                {
                                    //G
                                    weightbool[OPEN_Y.get(0)+1][OPEN_X.get(0)]=weightbool[OPEN_Y.get(0)][OPEN_X.get(0)]+1;
                                    OPEN_X.add(OPEN_X.get(0));
                                    OPEN_Y.add(OPEN_Y.get(0)+1);
                                    //H
                                    OPEN_F.add((int)(Astark*(Math.abs(ybegin-(OPEN_Y.get(0)+1))+
                                            Math.abs(xbegin-(OPEN_X.get(0))) ))+
                                            weightbool[OPEN_Y.get(0)+1][OPEN_X.get(0)]); }
                            }
                            //OPEN释放第一个元素
                            OPEN_X.remove(0);
                            OPEN_Y.remove(0);
                            OPEN_F.remove(0);
                        }
                        if(Astarpaint==1)//Astarpaint绘图（多种情况随机生成）
                        {
                            if(sendpointsign==0)
                            {
                                point[pointi][0]=POINTlocation[ybegin][xbegin][0];//x
                                point[pointi][1]=POINTlocation[ybegin][xbegin][1];//y
                                pointi++;
                                sendpointsign=1;
                            }

                            int ytemp,xtemp;
                            ytemp=ybegin;
                            xtemp=xbegin;


                            //右下左上
                            for(int i=weightbool[ybegin][xbegin]-1;i>=0;i--)
                            {

                                if(xtemp<Xn.size()-2)//右
                                {
                                    if(weightbool[ytemp][xtemp+1]==i)
                                    {
                                        //画线
                                        p.setColor(Color.RED);
                                        p.setStyle(Paint.Style.STROKE);
                                        canvas.drawLine(POINTlocation[ytemp][xtemp][0] , POINTlocation[ytemp][xtemp][1] ,
                                                POINTlocation[ytemp][xtemp + 1][0] , POINTlocation[ytemp][xtemp + 1][1] , p);

                                        point[pointi][0]= POINTlocation[ytemp][xtemp + 1][0];//x
                                        point[pointi][1]= POINTlocation[ytemp][xtemp + 1][1];//y
                                        pointi++;

                                        xtemp++;
                                        continue;
                                    }
                                }
                                if(ytemp<Yn.size()-2)//下
                                {
                                    if(weightbool[ytemp+1][xtemp]==i)
                                    {
                                        //画线
                                        p.setColor(Color.RED);
                                        p.setStyle(Paint.Style.STROKE);
                                        canvas.drawLine(POINTlocation[ytemp][xtemp][0] , POINTlocation[ytemp][xtemp][1] ,
                                                POINTlocation[ytemp + 1][xtemp][0] , POINTlocation[ytemp + 1][xtemp][1] , p);

                                        point[pointi][0]= POINTlocation[ytemp+1][xtemp][0];//x
                                        point[pointi][1]= POINTlocation[ytemp+1][xtemp][1];//y
                                        pointi++;

                                        ytemp++;
                                        continue;
                                    }
                                }
                                if(xtemp!=0)//左
                                {
                                    if(weightbool[ytemp][xtemp-1]==i)
                                    {
                                        //画线
                                        p.setColor(Color.RED);
                                        p.setStyle(Paint.Style.STROKE);
                                        canvas.drawLine(POINTlocation[ytemp][xtemp][0], POINTlocation[ytemp][xtemp][1] ,
                                                POINTlocation[ytemp][xtemp - 1][0] , POINTlocation[ytemp][xtemp - 1][1] , p);

                                        point[pointi][0]= POINTlocation[ytemp][xtemp - 1][0];//x
                                        point[pointi][1]= POINTlocation[ytemp][xtemp - 1][1];//y
                                        pointi++;

                                        xtemp--;
                                        continue;
                                    }
                                }
                                if(ytemp!=0)//上
                                {
                                    if(weightbool[ytemp-1][xtemp]==i)
                                    {
                                        //画线
                                        p.setColor(Color.RED);
                                        p.setStyle(Paint.Style.STROKE);
                                        canvas.drawLine(POINTlocation[ytemp][xtemp][0] , POINTlocation[ytemp][xtemp][1] ,
                                                POINTlocation[ytemp - 1][xtemp][0] , POINTlocation[ytemp - 1][xtemp][1] , p);

                                        point[pointi][0]=   POINTlocation[ytemp - 1][xtemp][0];//x
                                        point[pointi][1]=  POINTlocation[ytemp - 1][xtemp][1];//y
                                        pointi++;

                                        ytemp--;
                                        continue;
                                    }
                                }
                            }

                        }

                        for(int i=0;i<Yn.size()-1;i++)
                        {
                            for(int j=0;j<Xn.size()-1;j++)
                            {
                                if(weightbool[i][j]>0)
                                    weightbool[i][j]=-2;

                            }
                        }

                        //JOptionPane.showMessageDialog(null, "提示：已生成一条路径！");
                    }


                    point[pointi][0]=-1;//x
                    point[pointi][1]=-1;//y
                    pointi++;
                    datedeal();//生成只含拐点以及起点终点的sendPoint[][];

                }
            }
        }

        //isend=1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_DOWN:
                if(sign_setobstacle==0){
                    mouseX1=(int)event.getX();
                    mouseY1=(int)event.getY();
                    // System.out.println("1");
                    //repaint();
                    //Log.i("ACTION_DOWN-->",mouseX1+"");
                }
                if(sign_setobstacle==1)
                {
                    chooseX=(int)event.getX();
                    chooseY=(int)event.getY();
                    Choosecalculate();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(sign_setobstacle==0)
                {
                    mouseX2=(int)event.getX();
                    mouseY2=(int)event.getY();
                    Calculate();
                    invalidate();
                    //Log.i("ACTION_UP-->",mouseX2+"");
                }
                break;
        }
        return  true;
    }

    private void Choosecalculate(){
        choosehistory[choosenum][0]=chooseX;
        choosehistory[choosenum][1]=chooseY;
        choosenum++;
    }
    private void Calculate() {
        history[num][0]=mouseX1<mouseX2?mouseX1:mouseX2;
        history[num][1]=mouseY1<mouseY2?mouseY1:mouseY2;
        history[num][2]=mouseX1>mouseX2?mouseX1:mouseX2;
        history[num][3]=mouseY1>mouseY2?mouseY1:mouseY2;
        num++;
    }

    //撤销
    public void undo(){
        //if(sign_setobstacle==0) num--;
        //choose.setEnabled(true);
        sign_setobstacle=0;//0:摆放障碍物 1：路径规划
        sign_buildtree=0;//是否生成关键点
        sign_buildpath=0;//是否生成路径
        if(num!=0) num--;//障碍物-1
        choosenum=0;
        //重置：数组初始化
        for(int i=0;i<20;i++)
        {
            for(int j=0;j<2;j++)
            {
                choosehistory[i][j]=0;
                choosehistoryij[i][j]=0;
            }
        }
        Xn.clear();Yn.clear();
        //  Weightorientation.clear();
        OPEN_X.clear();
        OPEN_Y.clear();
        OPEN_F.clear();
        invalidate();
    }

    //重置
    public void reset(){
        //if(sign_setobstacle==0) num--;
        //choose.setEnabled(true);
        sign_setobstacle=0;//0:摆放障碍物 1：路径规划
        sign_buildtree=0;//是否生成关键点
        sign_buildpath=0;//是否生成路径
        num=0;//障碍物为0

        choosenum=0;
        //重置：数组初始化
        for(int i=0;i<50;i++)
        {
            for(int j=0;j<4;j++)
            {
                history[i][j]=0;
                historyindex[i][j]=0;
            }
        }
        for(int i=0;i<20;i++)
        {
            for(int j=0;j<2;j++)
            {
                choosehistory[i][j]=0;
                choosehistoryij[i][j]=0;
            }
        }
        Xn.clear();Yn.clear();
        //Weightorientation.clear();
        OPEN_X.clear();
        OPEN_Y.clear();
        OPEN_F.clear();
        invalidate();


    }
    //完成障碍物摆放
    public  void  setObstacle(){

        File file = new File("/sdcard/car_history.txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter writer = new OutputStreamWriter(fos,"UTF-8");

            writer.write(toString().valueOf(num));
            writer.write("\n");
            for(int i=0;i<num;i++){
                writer.write(toString().valueOf(history[i][0]));
                writer.write("\n");
                writer.write(toString().valueOf(history[i][1]));
                writer.write("\n");
                writer.write(toString().valueOf(history[i][2]));
                writer.write("\n");
                writer.write(toString().valueOf(history[i][3]));
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }



        if(sign_setobstacle==0){
            //请选择点进行路径规划
            BuildTree();
            invalidate();
            sign_buildtree=1;
            sign_setobstacle=1;
            choosenum=0;
        }
//        else if(sign_setobstacle==1){
//            sign_buildpath=1;//生成路径使能
//            invalidate();
//        }
    }
    //路径规划
    public void plan_path(){
//        sign_setobstacle=1;
//        choosenum=0;
        if(sign_setobstacle==1){
            sign_buildpath=1;//生成路径使能
            invalidate();
        }
        // findViewById(R.id.path).setEnabled(false);
    }

    private void BuildTree() {

        if(gridmethod==1)//纯栅格法
        {
            //转换 开根号
            int upbound=20;//上限10X10
            int i;
            int isqrt=0;
            for(i=1;i<=upbound;i++)
            {
                if(gridnum-(i*i+i)<=0)
                {
                    isqrt=i;
                    break;
                }
            }
            if(i==upbound+1){
                isqrt=upbound;
            }
            for(int j=0;j<=isqrt;j++){
                Xn.add(   (int) ((Xsmall * windowsizeX)+positionee)*(isqrt-j)/(isqrt)
                                +(int) ((Xlarge * windowsizeX)-positionee)*(j)      /(isqrt)
                );
                Yn.add(   (int) ((Ysmall * windowsizeY)+positionee)*(isqrt-j)/(isqrt)
                                +(int) ((Ylarge * windowsizeY)-positionee)*(j)      /(isqrt)
                );
            }

        }
        else if(gridmethod==0)
        {
            for (int i = 0; i < num; i++) {
                Xn.add(history[i][0]-positionee);
                Yn.add(history[i][1]-positionee);
                Xn.add(history[i][2]+positionee);
                Yn.add(history[i][3]+positionee);
            }
            //有时候700有时候7
            if(windowsizeX%100==0)
                windowsizeX/=100;
            if(windowsizeY%100==0)
                windowsizeY/=100;



            Xn.add((int) (Xsmall * windowsizeX)+positionee);//windowsizeX
            Xn.add((int) (Xlarge * windowsizeX)-positionee);
            Yn.add((int) (Ysmall * windowsizeY)+positionee);
            Yn.add((int) (Ylarge * windowsizeY)-positionee);
            //1.障碍物扩大方式

            //排序
            Collections.sort(Xn);
            Collections.sort(Yn);
            for (int i = Xn.size() - 1; i >= 0; i--) {

                //Xn限高
                if( i < Xn.size() - 1&&Xn.get(i+1)-Xn.get(i)>((Xlarge * windowsizeX-2*positionee)/3))
                {
                    Xn.add(i+1,(Xn.get(i+1)+Xn.get(i))/2);
                }

                if (Xn.get(i) <Xsmall * windowsizeX+positionee) {
                    if (Xsmall * windowsizeX+positionee - Xn.get(i) < paintee && i < Xn.size() - 1 && Xn.get(i + 1) != Ysmall * windowsizeY+positionee) {
                        Xn.remove(i);
                        Xn.add(i, (int) (Xsmall * windowsizeX)+positionee);
                    } else Xn.remove(i);

                } else if (Xn.get(i) > Xlarge * windowsizeX-positionee) {
                    Xn.remove(i);
                    if (Xn.get(i - 1) < Xlarge * windowsizeX-positionee) {
                        Xn.add(i, (int) (Xlarge * windowsizeX)-positionee);
                    }
                } else if (i >= 1 && Xn.get(i) - Xn.get(i - 1) < paintee) {
                    Xn.add(i-1,(Xn.get(i-1)+Xn.get(i))/2 );
                    Xn.remove(i+1);
                    Xn.remove(i);

                }



            }
            for (int i = Yn.size() - 1; i >= 0; i--) {

                //Yn限高
                if( i < Yn.size() - 1&&Yn.get(i+1)-Yn.get(i)> ((Ylarge * windowsizeY-2*positionee)/3))
                {
                    Yn.add(i+1,(Yn.get(i+1)+Yn.get(i))/2);
                }


                if (Yn.get(i) < Ysmall * windowsizeY+positionee) {

                    if (Ysmall * windowsizeY+positionee - Yn.get(i) < paintee && i < Yn.size() - 1 && Yn.get(i + 1) != Ysmall * windowsizeY+positionee) {
                        Yn.remove(i);
                        Yn.add(i, (int) (Ysmall * windowsizeY)+positionee);
                    } else Yn.remove(i);
                } else if (Yn.get(i) > Ylarge * windowsizeY-positionee) {
                    Yn.remove(i);
                    if (Yn.get(i - 1) < Ylarge * windowsizeY-positionee) {
                        Yn.add(i, (int) (Ylarge * windowsizeY)-positionee);
                    }
                } else if (i >= 1 && Yn.get(i) - Yn.get(i - 1) < paintee) {//remove i
                    Yn.add(i-1,(Yn.get(i-1)+Yn.get(i))/2 );
                    Yn.remove(i+1);
                    Yn.remove(i);
                }


            }

            System.out.print("windowsizeY:"+windowsizeY);
            //Show
            System.out.print("\nX:");
            for(int i=0;i<Xn.size();i++)
            {
                System.out.print(Xn.get(i)+"|");
            }
            System.out.print("\nY:");
            for(int i=0;i<Yn.size();i++)
            {
                System.out.print(Yn.get(i)+"|");
            }
            System.out.print("End");

        }

        createPoint();



    }

    //根据障碍物生成点
    public void createPoint(){
        //System.out.println("\nhhsgridmethod="+gridmethod+"---gridnum="+gridnum);

        ////////////////////////////////////////////
        //从history中找出障碍并生成bool数组

        int kmin;
        for(int k=0;k<num;k++)
        {
            //左上角
            kmin=999999;
            for(int i=0;i<Yn.size();i++)
            {
                for(int j=0;j<Xn.size();j++)
                {
                    int s;
                    s=(history[k][0]-positionee-Xn.get(j))*(history[k][0]-positionee-Xn.get(j))
                            +(history[k][1]-positionee-Yn.get(i))*(history[k][1]-positionee-Yn.get(i));
                    if(s<kmin){
                        kmin=s;
                        historyindex[k][0]=j;
                        historyindex[k][1]=i;
                    }
                }

            }


            //右下角
            kmin=999999;
            for(int i=0;i<Yn.size();i++)
            {
                for(int j=0;j<Xn.size();j++)
                {
                    int s;
                    s=(history[k][2]+positionee-Xn.get(j))*(history[k][2]+positionee-Xn.get(j))
                            +(history[k][3]+positionee-Yn.get(i))*(history[k][3]+positionee-Yn.get(i));
                    if(s<kmin){
                        kmin=s;
                        historyindex[k][2]=j;
                        historyindex[k][3]=i;
                    }
                }

            }
        }

        ///////////////////////////////////////////
    }
    public void setWindowsizeX(double windowsizeX) {
        this.windowsizeX = windowsizeX;
    }

    public void setWindowsizeY(double windowsizeY) {
        this.windowsizeY = windowsizeY;
    }
    public void setPositionee(int positionee) {
        this.positionee = positionee;
    }

    public int isornotsuccess() {
        return this.Astarpaint;
    }

    //    public int isornotend() {
//        return this.isend;
//     }
    public void setdeviationee(int deviationee) {
        this.deviationee = deviationee*100;
    }
    public void setgridnum(int gridnum){

        this.gridnum=gridnum;
    }
    public void setgridmethod(int gridmethod){
        this.gridmethod=gridmethod;
    }


    //处理数据
    public void datedeal(){
        int deali=0;
        int xsign=1;
        int ysign=1;

        sendpoint[deali][0]=point[0][0];
        sendpoint[deali][1]=point[0][1];
        deali++;

        int i;
        for(i=1;point[i][0]!=-1;i++)
        {
            if(point[i][0]==point[i-1][0]){

                if(xsign==0||i!=1&&point[i][0]==point[i-2][0]&&point[i][1]==point[i-2][1])
                {
                    sendpoint[deali][0]=point[i-1][0];
                    sendpoint[deali][1]=point[i-1][1];
                    deali++;
                }



                xsign=1;
                ysign=0;

            }

            if(point[i][1]==point[i-1][1]){
                if(ysign==0||i!=1&&point[i][0]==point[i-2][0]&&point[i][1]==point[i-2][1])
                {
                    sendpoint[deali][0]=point[i-1][0];
                    sendpoint[deali][1]=point[i-1][1];
                    deali++;
                }
                xsign=0;
                ysign=1;
            }
        }

        sendpoint[deali][0]=point[i-1][0];
        sendpoint[deali][1]=point[i-1][1];
        deali++;

        sendpoint[deali][0]=-1;
        sendpoint[deali][1]=-1;
        deali++;
    }





    public double getWindowsizeX(){
        return windowsizeX;
    }
    public double getWindowsizeY(){
        return windowsizeY;
    }

    public int[][] getSendpoint() {
        return sendpoint;
    }


    public int[][] history() {
        return history;
    }
    public int num() {
        return num;
    }



}



