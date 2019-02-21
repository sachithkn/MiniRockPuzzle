package com.sachithkn.stonegame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,WifiP2pManager.PeerListListener{


    ImageButton button1,button2,button3,button4,button5,button6,button7,button8,button9,restartButton,buttonlast;
    Button difficultyButton,playersButton;
    TextView messageTextView,player1TextView,player2TextView;
    public  enum PositionValue{emtpy,player1,player2};
    public  enum NextMove{player1,player2};
    public  enum Difficulty{easy,normal,hard};
    public  enum Players{oneplayer,twoplayer};
    static int DIALOG_PLAYER =0;
    static int DIALOG_DIFFICULTY =1 ;
    static Difficulty difficulty;
    static Players players;
    float winanimationX,winanimationY;
    float buttonAnimationXstart,buttonAnimationYstart;
    boolean buttonAnimation,winanimation,moveanimation ;
    float buttonAnimationXfinish,buttonAnimationYfinish;
    float moveanimationXstart,moveanimationYstart,moveanimationXfinish,moveanimationYfinish,moveanimationDY,moveanimationDX;
    int buttonAnimationCount,winAnimationCount;
    ConstraintLayout constraintLayout;
    StonePathView stonePathView;
    ImageButton buttonswap;
    private List<WifiP2pDevice> peers;

    public PositionValue position[];
    NextMove nextMove;
    int movecount,buttonclicked,buttonclickedswap;
    PositionValue playerwon;
    boolean touched;
    int numberRandom1 ;
    int numberRandom2 ;
    int numberRandom3 ;
    int numberRandom4 ;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    //obtain a peer from the WifiP2pDeviceList
    boolean computermove ;
    boolean nextmovewincheck ;
    boolean swapNotfailes;
    boolean avaoidrepeatfirstclick;
    WifiManager wifiManager ;
    HotspotManager hotspotManager;
    boolean isAppEnabledWifi = false;
    boolean isAppEnabledHotspot = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Declare();
        SetClickListners();
//        showDialogPlayers();
        showDialogDifficulty();
        playersButton.setText("One Player");
        difficultyButton.setEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimaryDark));
        }
    }
/*
    public void switchWifi(boolean value){
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(value);
        }
    }*/

    public  void switchHotSpot(boolean value){
        hotspotManager = new HotspotManager();
        if((!hotspotManager.isApOn(getApplicationContext()) && value)
                || (hotspotManager.isApOn(getApplicationContext()) && !value)){
            hotspotManager.configApState(getApplicationContext());
        }
    }

    public void StartWifiConnection(){
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(MainActivity.this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.

            }
        });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

        Collection<WifiP2pDevice> collection = peers.getDeviceList();
        if(collection.size()>0) {
            WifiP2pDevice device =(WifiP2pDevice) collection.toArray()[0];
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
        //    Toast.makeText(getApplicationContext(), device.deviceAddress, Toast.LENGTH_SHORT).show();

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Connection success ", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Connection Failed ", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(getApplicationContext(), "No device found ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        if(mReceiver != null && mIntentFilter != null)
            registerReceiver(mReceiver, mIntentFilter);
    }



    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            if(mReceiver!=null )
                unregisterReceiver(mReceiver);
        } catch(IllegalArgumentException e) {e.printStackTrace();}

        if(hotspotManager != null && hotspotManager.isApOn(getApplicationContext())){
            hotspotManager.configApState(getApplicationContext());
        }
        if(wifiManager != null && wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }
    }

    private void Declare() {
        button1 = (ImageButton)findViewById(R.id.button1);
        button2 = (ImageButton)findViewById(R.id.button2);
        button3 = (ImageButton)findViewById(R.id.button3);
        button4 = (ImageButton)findViewById(R.id.button4);
        button5 = (ImageButton)findViewById(R.id.button5);
        button6 = (ImageButton)findViewById(R.id.button6);
        button7 = (ImageButton)findViewById(R.id.button7);
        button8 = (ImageButton)findViewById(R.id.button8);
        button9 = (ImageButton)findViewById(R.id.button9);
        difficultyButton = (Button)findViewById(R.id.difficultyButton);
        playersButton = (Button)findViewById(R.id.playersButton);
        restartButton = (ImageButton)findViewById(R.id.restartButton);
        //       restartButton.setBackgroundResource(R.drawable.buttonbackground);
        player1TextView  = (TextView)findViewById(R.id.player1TextView);
        player2TextView  = (TextView)findViewById(R.id.player2TextView);
        messageTextView  = (TextView)findViewById(R.id.messageTextView);
        constraintLayout = (ConstraintLayout)findViewById(R.id.relativeLayout);
        players  = Players.oneplayer;
    }

    private void Initialize() {

        movecount =0;
        nextMove = NextMove.player1;
        buttonAnimation = false;
        winanimation = false;
        winanimationX = 0 ;
        winanimationY = 0;
        buttonAnimationXstart = 0;
        buttonAnimationYstart = 0;
        buttonAnimationXfinish = 0;
        buttonAnimationYfinish = 0;
        buttonAnimationCount = 100;
        winAnimationCount = 0;
        moveanimation = false;
        moveanimationXstart = 0;
        moveanimationYstart = 0 ;
        moveanimationXfinish = 0;
        moveanimationYfinish =0 ;
        moveanimationDX =0 ;
        moveanimationDY =0 ;
        computermove =false;
        nextmovewincheck = true;
        avaoidrepeatfirstclick =false;
        swapNotfailes = false;


        position = new PositionValue[]{PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy,
                PositionValue.emtpy};

        player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorGreen));
        player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
        playerwon =PositionValue.emtpy;
        buttonclicked = 0;
        buttonclickedswap=0;
        buttonswap = null;
        messageTextView.setText("Click on one of the box to start");
        messageTextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));

        numberRandom1 = 0;
        numberRandom2 = 0;
        numberRandom3 = 0;
        numberRandom4 = 0;

        button1.setTag("0");
        button2.setTag("0");
        button3.setTag("0");
        button4.setTag("0");
        button5.setTag("0");
        button6.setTag("0");
        button7.setTag("0");
        button8.setTag("0");
        button9.setTag("0");

        button1.setImageDrawable(null);
        button2.setImageDrawable(null);
        button3.setImageDrawable(null);
        button4.setImageDrawable(null);
        button5.setImageDrawable(null);
        button6.setImageDrawable(null);
        button7.setImageDrawable(null);
        button8.setImageDrawable(null);
        button9.setImageDrawable(null);

/*
        button1.setBackgroundResource(R.drawable.buttonbackground);
        button2.setBackgroundResource(R.drawable.buttonbackground);
        button3.setBackgroundResource(R.drawable.buttonbackground);
        button4.setBackgroundResource(R.drawable.buttonbackground);
        button5.setBackgroundResource(R.drawable.buttonbackground);
        button6.setBackgroundResource(R.drawable.buttonbackground);
        button7.setBackgroundResource(R.drawable.buttonbackground);
        button8.setBackgroundResource(R.drawable.buttonbackground);
        button9.setBackgroundResource(R.drawable.buttonbackground);
*/
        button1.setBackgroundResource(android.R.color.transparent);
        button2.setBackgroundResource(android.R.color.transparent);
        button3.setBackgroundResource(android.R.color.transparent);
        button4.setBackgroundResource(android.R.color.transparent);
        button5.setBackgroundResource(android.R.color.transparent);
        button6.setBackgroundResource(android.R.color.transparent);
        button7.setBackgroundResource(android.R.color.transparent);
        button8.setBackgroundResource(android.R.color.transparent);
        button9.setBackgroundResource(android.R.color.transparent);

        stonePathView = findViewById(R.id.stonePathView);


    }

    private void SetClickListners() {

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
        playersButton.setOnClickListener(this);
        difficultyButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button1:
                buttonclicked = 1;
                if (Validmove()) {
                    MakeChanges(button1);
                }
                break;
            case R.id.button2:
                buttonclicked = 2;
                if (Validmove()) {
                    MakeChanges(button2);
                }
                break;
            case R.id.button3:
                buttonclicked = 3;
                if (Validmove()) {
                    MakeChanges(button3);
                }
                break;
            case R.id.button4:
                buttonclicked = 4;
                if (Validmove()) {
                    MakeChanges(button4);
                }
                break;
            case R.id.button5:
                buttonclicked = 5;
                if (Validmove()) {
                    MakeChanges(button5);
                }
                break;
            case R.id.button6:
                buttonclicked = 6;
                if (Validmove()) {
                    MakeChanges(button6);
                }
                break;
            case R.id.button7:
                buttonclicked = 7;
                if (Validmove()) {
                    MakeChanges(button7);
                }
                break;
            case R.id.button8:
                buttonclicked = 8;
                if (Validmove()) {
                    MakeChanges(button8);
                }
                break;
            case R.id.button9:
                buttonclicked = 9;
                if (Validmove()) {
                    MakeChanges(button9);
                }
                break;
            case R.id.restartButton:
                Initialize();
                break;
            case R.id.difficultyButton:
                showDialogDifficulty();
                break;
            case R.id.playersButton:
                showDialogPlayers();
                break;
        }
        if(nextMove == NextMove.player2 && playerwon == PositionValue.emtpy && players == Players.oneplayer) {
            if(movecount<6) {
                ComputerMove();
            }else {
                computermove = true;
            }
        }

    }



    private boolean Validmove() {

        if(playerwon != PositionValue.emtpy) {
            if(nextMove == NextMove.player1) {
                messageTextView.setText("Restart the game to play again");
            }
            return false;
        }else if(movecount>5){
            return true;
        }else if(position[buttonclicked-1]!=PositionValue.emtpy){
            if(nextMove == NextMove.player1) {
                messageTextView.setText("You cant place your there because position is occuped");
            }
            return false;
        }else {
            return true;
        }

    }


    public void MakeChanges(ImageButton button){
        movecount++;

        stonePathView.invalidate();

        PositionValue positionValueTemp = position[buttonclicked - 1];
        NextMove nextMoveTemp = nextMove;
        String nextmovevaluetextTemp="";
        String buttontextTemp=button.getTag().toString();
        Drawable drawableTemp = button.getDrawable();
        if(nextMove==NextMove.player2){
            messageTextView.setText("");
        }


        if(movecount>6){

            if(buttonswap==null) {
                if(position[buttonclicked - 1] == PositionValue.emtpy  ){
                    if(nextMove == NextMove.player1)
                        messageTextView.setText("You cant swap from empty position");
                }else if(position[buttonclicked - 1] == PositionValue.player1 && nextMove == NextMove.player2 ){
                    if(nextMove == NextMove.player1)
                        messageTextView.setText("This is player 2's move");
                }else if(position[buttonclicked - 1] == PositionValue.player2 && nextMove == NextMove.player1){
                    if(nextMove == NextMove.player1)
                        messageTextView.setText("This is player 1's move");
                }else {
//                    setAnimationValues(button);
                    buttonswap = button;
                    buttonswap.setBackgroundColor(Color.parseColor("#FFFF00"));
                    buttonclickedswap = buttonclicked;
                }
            }else {
                if((position[buttonclicked - 1] != PositionValue.emtpy) && buttonclicked!=buttonclickedswap ){
                    if(nextMove == NextMove.player1)
                        messageTextView.setText("You cant swap to this position it is not empty");
                }else if(ChecckisClose()) {
                    setMoveAnimationValues(button);
                    //  buttonswap.setBackgroundResource(R.drawable.buttonbackground);
                    buttonswap.setBackgroundResource(android.R.color.transparent);
                    if (nextMove == NextMove.player1) {
                        position[buttonclicked - 1] = PositionValue.player1;
                        position[buttonclickedswap - 1] = PositionValue.emtpy;
                        nextMove = NextMove.player2;
                        player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                        player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                        button.setTag("1");
//                        button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.stone1));
                        buttonswap.setTag("0");
                        buttonswap.setImageDrawable(null);
                    } else {
                        position[buttonclicked - 1] = PositionValue.player2;
                        position[buttonclickedswap - 1] = PositionValue.emtpy;
                        nextMove = NextMove.player1;
                        player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                        player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
//                        button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gems2));
                        button.setTag("2");
                        buttonswap.setImageDrawable(null);
                        buttonswap.setTag("0");
                    }
                    buttonswap = null;
                    buttonclickedswap = 0;

                }
            }

        }else {
            setAnimationValues(button);

            if (nextMove == NextMove.player1) {
                position[buttonclicked - 1] = PositionValue.player1;
                nextMove = NextMove.player2;
                player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorGreen));
                player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
                button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gems1));
                button.setTag("1");
                nextmovevaluetextTemp = "Player 1";
            } else {
                position[buttonclicked - 1] = PositionValue.player2;
                nextMove = NextMove.player1;
                player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorGreen));
                player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
                button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gems2));
                button.setTag("2");
                nextmovevaluetextTemp = "Player 2";
            }
        }

        if(CheckWin()){
            if(movecount<7){
                position[buttonclicked - 1] = positionValueTemp;
                nextMove = nextMoveTemp;
                if(nextmovevaluetextTemp.equals("Player 1")) {
                    player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                    player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                }
                else {
                    player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                    player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorWhite));
                }
                button.setTag(buttontextTemp);
                button.setImageDrawable(drawableTemp);
                movecount--;
                if( nextMove == NextMove.player1){
                    messageTextView.setText("You cant win in initial moves");
                }
            }else if(playerwon==PositionValue.player1){
                messageTextView.setText("Player one has won");
                messageTextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorGreen));
                winanimation = true;
                winanimationX = messageTextView.getX();
                winanimationY = messageTextView.getY();
                stonePathView.invalidate();
            }else if(playerwon==PositionValue.player2){
                messageTextView.setText("Player two has won");
                messageTextView.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorRed));
                winanimation = true;
                winanimationX = messageTextView.getX();
                winanimationY = messageTextView.getY();
                stonePathView.invalidate();
            }
        }
    }

    private boolean SwapNotFailes(ImageButton button) {


        position[buttonclicked - 1] = PositionValue.player2;
        position[buttonclickedswap - 1] = PositionValue.emtpy;
        if(CheckFail() && findnextmovetrapswap()){
            position[buttonclicked - 1] = PositionValue.emtpy;
            position[buttonclickedswap - 1] = PositionValue.player2;
            return  false;
        }


        return true;
    }


    public void SwapButtons(ImageButton button){

        setMoveAnimationValues(button);

//        buttonswap.setBackgroundResource(R.drawable.buttonbackground);
        buttonswap.setBackgroundResource(android.R.color.transparent);


        position[buttonclicked - 1] = PositionValue.player2;
        position[buttonclickedswap - 1] = PositionValue.emtpy;
        nextMove = NextMove.player1;
        player1TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
        player2TextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
//                        button.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gems2));
        button.setTag("2");
        buttonswap.setImageDrawable(null);
        buttonswap.setTag("0");


        buttonswap = null;
        buttonclickedswap = 0;
    }

    private void setMoveAnimationValues(ImageButton button) {

        buttonswap.setImageDrawable(null);
        button.setImageDrawable(null);
        moveanimation = true;
        buttonlast  = button;
        moveanimationXstart = buttonswap.getX();
        moveanimationYstart = buttonswap.getY();
        moveanimationXfinish = button.getX();
        moveanimationYfinish = button.getY();
        float xchange = moveanimationXfinish - moveanimationXstart ;
        float ychange = moveanimationYfinish - moveanimationYstart ;

        float delayx = 1;
        float delayy = 1;
        float delay = 0.5f;

        if(xchange < 0 )
            delayx = -1 * delay;
        else
            delayx = 1 * delay;

        if(ychange < 0 )
            delayy = -1 * delay;
        else
            delayy = 1 * delay;

        if(Math.abs(ychange)>Math.abs(xchange)) {
            moveanimationDX =( Math.abs(xchange) / Math.abs(ychange))/delayx;
            moveanimationDY = 1/delayy;
        }else {
            moveanimationDX = 1/delayx;
            moveanimationDY =( Math.abs(ychange)/ Math.abs(xchange))/delayy;
        }
        delayx = 1000;
        stonePathView.invalidate();

    }

    public void setAnimationValues(ImageButton button){
        buttonAnimation = true;
        buttonAnimationXstart = button.getX();
        buttonAnimationYstart = button.getY();
        if(buttonswap!=null) {
            buttonAnimationXfinish = buttonswap.getX();
            buttonAnimationYfinish = buttonswap.getY();
        }
    }

    private boolean ChecckisClose() {

        if(buttonclicked == buttonclickedswap){
            //        buttonswap.setBackgroundResource(R.drawable.buttonbackground);
            buttonswap.setBackgroundResource(android.R.color.transparent);
            buttonclickedswap=0;
            buttonswap=null;
            return  false;
        }else if( ( (buttonclicked+1 == buttonclickedswap) &&  (buttonclicked%3!=0) ) ||
                ((buttonclicked-1 == buttonclickedswap ) &&  (buttonclicked%3!=1) ) ||
                (buttonclicked+3 == buttonclickedswap ) ||
                (buttonclicked-3 == buttonclickedswap ) ||
                ((buttonclicked-6 == buttonclickedswap ) && position[(buttonclicked-3)-1] == PositionValue.emtpy) ||
                ((buttonclicked+6 == buttonclickedswap ) && position[(buttonclicked+3)-1] == PositionValue.emtpy) ||
                (buttonclicked == 1 && buttonclickedswap==3  && position[1] == PositionValue.emtpy) ||
                (buttonclicked == 4 && buttonclickedswap==6  && position[4] == PositionValue.emtpy) ||
                (buttonclicked == 7 && buttonclickedswap==9  && position[7] == PositionValue.emtpy) ||
                (buttonclicked == 3 && buttonclickedswap==1  && position[1] == PositionValue.emtpy) ||
                (buttonclicked == 6 && buttonclickedswap==4  && position[4] == PositionValue.emtpy) ||
                (buttonclicked == 9 && buttonclickedswap==7  && position[7] == PositionValue.emtpy) ||
                (buttonclicked == 1 && buttonclickedswap==9  && position[4] == PositionValue.emtpy) ||
                (buttonclicked == 3 && buttonclickedswap==7  && position[4] == PositionValue.emtpy) ||
                (buttonclicked == 9 && buttonclickedswap==1  && position[4] == PositionValue.emtpy) ||
                (buttonclicked == 7 && buttonclickedswap==3  && position[4] == PositionValue.emtpy) ||
                (buttonclickedswap==5 && position[buttonclicked-1] == PositionValue.emtpy) ||
                buttonclicked==5  ) {
            return true;
        }else {
            if( nextMove == NextMove.player1) {
                messageTextView.setText("InvalidMove");
            }
            return  false;
        }

    }

    public boolean CheckFail(){

        //1
        if(position[0]==position[1] && position[0]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            if(PositionValue.player1==position[4]){
                return  true;
            }else if(PositionValue.player1==position[5]){
                return  true;
            }else if(PositionValue.player1==position[6] && position[4] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[8] && position[5] == PositionValue.emtpy){
                return  true;
            }
        }

        //2
        if(position[0]==position[2] && position[0]==PositionValue.player1&& position[1]==PositionValue.emtpy){
            if(PositionValue.player1==position[4]){
                return  true;
            }else if(PositionValue.player1==position[7] && position[4]==PositionValue.emtpy){
                return  true;
            }
        }

        //3
        if(position[1]==position[2] && position[1]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            if(PositionValue.player1==position[6] && position[3] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[3]){
                return  true;
            }else if(PositionValue.player1==position[8] && position[4] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[4]){
                return  true;
            }
        }

        //4
        if(position[3]==position[4] && position[3]==PositionValue.player1 && position[5]==PositionValue.emtpy){
            if(PositionValue.player1==position[2]){
                return  true;
            }else if(PositionValue.player1==position[8]){
                return  true;
            }
        }

        //5
        if(position[4]==position[5] && position[4]==PositionValue.player1 && position[3]==PositionValue.emtpy){
            if(PositionValue.player1==position[0]){
                return  true;
            }else if(PositionValue.player1==position[6]){
                return  true;
            }
        }

        //6
        if(position[3]==position[5] && position[3]==PositionValue.player1  && position[4]==PositionValue.emtpy){
            if(position[0]==PositionValue.player1 ){
                return  true;
            }else if(position[1]==PositionValue.player1){
                return  true;
            }else if(position[2]==PositionValue.player1){
                return  true;
            }else if(position[6]==PositionValue.player1){
                return  true;
            }else if(position[7]==PositionValue.player1){
                return  true;
            }else if(position[8]==PositionValue.player1){
                return  true;
            }
        }

        //7
        if(position[6]==position[7] && position[6]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            if(PositionValue.player1==position[5]){
                return  true;
            }else if(PositionValue.player1==position[2] && position[5]==PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[4]){
                return  true;
            }else if(PositionValue.player1==position[0] && position[4]==PositionValue.emtpy){
                return  true;
            }
        }

        //8
        if(position[7]==position[8] && position[7]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            if(PositionValue.player1==position[3]){
                return  true;
            }else if(PositionValue.player1==position[0] && position[3]==PositionValue.emtpy){
                return  true;
            }else if(position[4]==PositionValue.player1){
                return  true;
            }else if(PositionValue.player1==position[2] && position[4]==PositionValue.emtpy){
                return  true;
            }
        }

        //9
        if(position[6]==position[8] && position[6]==PositionValue.player1 && position[7]==PositionValue.emtpy){
            if(position[4]==PositionValue.player1){
                return  true;
            }else if(position[1]==PositionValue.player1 && position[4]==PositionValue.emtpy){
                return  true;
            }
        }


        //10
        if(position[0]==position[3] && position[0]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            if(PositionValue.player1==position[4]){
                return  true;
            }else if(PositionValue.player1==position[7]){
                return  true;
            }else if(PositionValue.player1==position[2] && position[4]==PositionValue.emtpy ){
                return  true;
            }else if(PositionValue.player1==position[8] && position[7]==PositionValue.emtpy ){
                return  true;
            }
        }

        //11
        if(position[0]==position[6] && position[0]==PositionValue.player1 && position[3]==PositionValue.emtpy){
            if(PositionValue.player1==position[4]){
                return  true;
            }else if(PositionValue.player1==position[5] && position[4]==PositionValue.emtpy ){
                return  true;
            }
        }

        //12
        if(position[3]==position[6] && position[3]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            if(PositionValue.player1==position[1]){
                return  true;
            }else if(PositionValue.player1==position[4]){
                return  true;
            }else if(PositionValue.player1==position[8] && position[4]==PositionValue.emtpy ){
                return  true;
            }else if(PositionValue.player1==position[2] && position[1]==PositionValue.emtpy ){
                return  true;
            }
        }

        //13
        if(position[1]==position[4] && position[1]==PositionValue.player1  && position[7]==PositionValue.emtpy){
            if(PositionValue.player1==position[6]){
                return  true;
            }else if(PositionValue.player1==position[8]){
                return  true;
            }
        }

        //14
        if(position[1]==position[7] && position[1]==PositionValue.player1  && position[4]==PositionValue.emtpy){
            if(PositionValue.player1== position[0] ){
                return  true;
            }else if(PositionValue.player1==position[3]){
                return  true;
            }else if(position[2]==PositionValue.player1){
                return  true;
            }else if(position[6]==PositionValue.player1){
                return  true;
            }else if(position[5]==PositionValue.player1){
                return  true;
            }else if(position[8]==PositionValue.player1){
                return  true;
            }
        }

        //15
        if(position[4]==position[7] && position[4]==PositionValue.player1 && position[1]==PositionValue.emtpy){
            if(position[0]==PositionValue.player1){
                return  true;
            }else if(position[2]==PositionValue.player1){
                return  true;
            }
        }

        //16
        if(position[2]==position[5] && position[2]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            if(PositionValue.player1==position[6] && position[7]==PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[7]){
                return  true;
            }else if(PositionValue.player1==position[0] && position[4]==PositionValue.emtpy ){
                return  true;
            }else if(PositionValue.player1==position[4]){
                return  true;
            }
        }

        //17
        if(position[2]==position[8] && position[2]==PositionValue.player1 && position[5]==PositionValue.emtpy){
            if(PositionValue.player1==position[3] && position[4]==PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[4]){
                return  true;
            }
        }

        //18
        if(position[5]==position[8] && position[5]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            if(PositionValue.player1==position[0] && position[1]==PositionValue.emtpy){
                return  true;
            }else if(position[1]==PositionValue.player1){
                return  true;
            }else if(PositionValue.player1==position[6] && position[4]==PositionValue.emtpy ){
                return  true;
            }else if(PositionValue.player1==position[4]){
                return  true;
            }
        }

        //19
        if(position[0]==position[4] && position[0]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            if(PositionValue.player1==position[2] && position[5] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[5]){
                return  true;
            }else if(PositionValue.player1==position[6] && position[7] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[7]){
                return  true;
            }
        }


        //20
        if(position[0]==position[8] && position[0]==PositionValue.player1 && position[4]==PositionValue.emtpy){
            if(PositionValue.player1==position[3] ){
                return  true;
            }else if(PositionValue.player1==position[6]){
                return  true;
            }else if(PositionValue.player1==position[7] ){
                return  true;
            }else if(PositionValue.player1==position[1]){
                return  true;
            }else if(PositionValue.player1==position[2]){
                return  true;
            }else if(PositionValue.player1==position[5]){
                return  true;
            }
        }

        //21
        if(position[8]==position[4] && position[4]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            if(PositionValue.player1==position[2] && position[1] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[1]){
                return  true;
            }else if(PositionValue.player1==position[6] && position[3] == PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[3]){
                return  true;
            }
        }

        //22
        if(position[2]==position[4] && position[2]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            if(PositionValue.player1==position[0] && position[3]==PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[3]){
                return  true;
            }else if(PositionValue.player1==position[7]){
                return  true;
            }else if(PositionValue.player1==position[8] && position[7]==PositionValue.emtpy){
                return  true;
            }
        }

        //23
        if(position[2]==position[6] && position[2]==PositionValue.player1 && position[4]==PositionValue.emtpy){
            if(position[0]==PositionValue.player1 ){
                return  true;
            }else if(position[1]==PositionValue.player1){
                return  true;
            }else if(position[3]==PositionValue.player1 ){
                return  true;
            }else if(position[7]==PositionValue.player1){
                return  true;
            }else if(position[8]==PositionValue.player1){
                return  true;
            }else if(PositionValue.player1==position[5]){
                return  true;
            }
        }

        //24
        if(position[6]==position[4] && position[4]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            if(PositionValue.player1==position[0] && position[1]==PositionValue.emtpy){
                return  true;
            }else if(PositionValue.player1==position[1]){
                return  true;
            }else if(PositionValue.player1==position[5]){
                return  true;
            }else if(PositionValue.player1==position[8] && position[5]==PositionValue.emtpy){
                return  true;
            }
        }


        return false;

    }


    public boolean CheckWin(){

        if(position[0]==position[1] && position[0]!=PositionValue.emtpy){
            if(position[0]==position[2]){
                if(movecount>6) {
                    button1.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button2.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button3.setBackgroundColor(Color.parseColor("#FFFF00"));

                    if (position[0] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }
        if(position[0]==position[3] && position[0]!=PositionValue.emtpy){
            if(position[0]==position[6]){

                if(movecount>6) {
                    button1.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button4.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button7.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[0] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }
        if(position[0]==position[4] && position[0]!=PositionValue.emtpy){
            if(position[0]==position[8]){
                if(movecount>6) {

                    button1.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button5.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button9.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[0] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }
        if(position[3]==position[4] && position[3]!=PositionValue.emtpy){
            if(position[3]==position[5]){
                if(movecount>6) {

                    button4.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button5.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button6.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[3] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }
        if(position[6]==position[7] && position[6]!=PositionValue.emtpy){
            if(position[6]==position[8]){
                if(movecount>6) {

                    button7.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button9.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button8.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[6] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return  true;
            }
        }
        if(position[1]==position[4] && position[1]!=PositionValue.emtpy){
            if (position[1]==position[7]){
                if(movecount>6) {

                    button2.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button5.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button8.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[1] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }
        if(position[2]==position[5] && position[2]!=PositionValue.emtpy){
            if(position[2]==position[8]){
                if(movecount>6) {

                    button3.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button6.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button9.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[2] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }
        if(position[2]==position[4] && position[2]!=PositionValue.emtpy){
            if(position[2]==position[6]){
                if(movecount>6) {
                    button3.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button5.setBackgroundColor(Color.parseColor("#FFFF00"));
                    button7.setBackgroundColor(Color.parseColor("#FFFF00"));
                    if (position[2] == PositionValue.player1) {
                        playerwon = PositionValue.player1;
                    } else {
                        playerwon = PositionValue.player2;
                    }
                }
                return true;
            }
        }

        playerwon =PositionValue.emtpy;
        return  false;
    }

    public void ComputerMove(){

        boolean check = false;

        if(difficulty == Difficulty.normal && movecount>5 &&nextmovewincheck){
            check = findNextMoveWin();
            if(!check)
                check = findNextMoveFail();
            nextmovewincheck = false;
        }else if(difficulty == Difficulty.hard && movecount>5 &&nextmovewincheck){
            check = findNextMoveWin();
            if(!check)
                check = findnextmovetrap();
            if(!check)
                check = findNextMoveFail();
            nextmovewincheck = false;
        }

        if(!check) {
            ImageButton button = null;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Random random = new Random();
            int  randomnumber = random.nextInt(9) + 1;

            if(movecount == 1){
                while (randomnumber%2 == 0){
                    randomnumber =  random.nextInt(9) + 1;
                }
            }

            buttonclicked = randomnumber;
            if (randomnumber == 1) {
                button = button1;
            } else if (randomnumber == 2) {
                button = button2;
            } else if (randomnumber == 3) {
                button = button3;
            } else if (randomnumber == 4) {
                button = button4;
            } else if (randomnumber == 5) {
                button = button5;
            } else if (randomnumber == 6) {
                button = button6;
            } else if (randomnumber == 7) {
                button = button7;
            } else if (randomnumber == 8) {
                button = button8;
            } else if (randomnumber == 9) {
                button = button9;
            }

            if (movecount <= 6) {

                boolean checklastclick = false;


                if(movecount==3 && (difficulty == Difficulty.normal || difficulty== Difficulty.hard)){
                    if(position[0] == PositionValue.player1 && position[8] == PositionValue.emtpy){
                        button = button9;
                    }else if(position[1] == PositionValue.player1 && position[7] == PositionValue.emtpy){
                        button = button8;
                    }else if(position[2] == PositionValue.player1 && position[6] == PositionValue.emtpy){
                        button = button7;
                    }else if(position[3] == PositionValue.player1 && position[5] == PositionValue.emtpy){
                        button = button6;
                    }else if(position[5] == PositionValue.player1 && position[3] == PositionValue.emtpy){
                        button = button4;
                    }else if(position[6] == PositionValue.player1 && position[2] == PositionValue.emtpy){
                        button = button3;
                    }else if(position[7] == PositionValue.player1 && position[1] == PositionValue.emtpy){
                        button = button2;
                    }else if(position[8] == PositionValue.player1 && position[0] == PositionValue.emtpy){
                        button = button1;
                    }
                }

                if(position[4]==PositionValue.emtpy && (difficulty == Difficulty.normal || difficulty== Difficulty.hard)){
                    button = button5;
                }

                if(movecount==5 && !avaoidrepeatfirstclick){
                    avaoidrepeatfirstclick =true;
                    checklastclick = findNextMoveFailBeforeInitialmove();
                }

                if (button != null && !checklastclick) {
                    button.performClick();
                }
            } else {

                if (buttonswap == null) {

                    if (position[buttonclicked - 1] == PositionValue.emtpy) {
                        if (nextMove == NextMove.player1)
                            messageTextView.setText("You cant swap from empty position");
                    } else if (position[buttonclicked - 1] == PositionValue.player1 && nextMove == NextMove.player2) {
                        messageTextView.setText("This is player 2's move");
                    } else if (position[buttonclicked - 1] == PositionValue.player2 && nextMove == NextMove.player1) {
                        messageTextView.setText("This is player 1's move");
                    } else {
//                    setAnimationValues(button);

                        if (CanMove()) {
                            buttonswap = button;
                            buttonswap.setBackgroundColor(Color.parseColor("#FFFF00"));
                            buttonclickedswap = buttonclicked;
                        }
                    }
                    ComputerMove();
                } else {
                    if ((position[buttonclicked - 1] != PositionValue.emtpy) && buttonclicked != buttonclickedswap) {
                        ComputerMove();
                    } else if (ChecckisClose()) {

                        if(swapNotfailes){
                            swapNotfailes =false;
                            SwapButtons(button);
                        }else {
                            if (difficulty == Difficulty.normal) {
                                if (SwapNotFailes(button)) {
                                    SwapButtons(button);
                                } else {
                                    swapNotfailes = true;
                                    ComputerMove();
                                }
                            } else if (difficulty == Difficulty.hard) {

                                if (SwapNotFailes(button) ) {
                                    SwapButtons(button);
                                } else {
                                    swapNotfailes = true;
                                    ComputerMove();
                                }
                            } else {
                                SwapButtons(button);
                            }
                        }
                        if (CheckWin()) {
                            if (playerwon == PositionValue.player1) {
                                messageTextView.setText("Player one has won");
                                messageTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorGreen));
                            } else if (playerwon == PositionValue.player2) {
                                messageTextView.setText("Player two has won");
                                messageTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
                            }
                            winanimation = true;
                            winanimationX = messageTextView.getX();
                            winanimationY = messageTextView.getY();
                            stonePathView.invalidate();
                        }
                    } else {
                        ComputerMove();
                    }
                }
            }
        }
    }

    private boolean CanMove() {
        boolean value =false;
        for(int buttonclickedswaptemp=1;buttonclickedswaptemp<=9;buttonclickedswaptemp++) {
            if(( ( (buttonclicked+1 == buttonclickedswaptemp) &&  (buttonclicked%3!=0) ) ||
                    ((buttonclicked-1 == buttonclickedswaptemp ) &&  (buttonclicked%3!=1) ) ||
                    (buttonclicked+3 == buttonclickedswaptemp ) ||
                    (buttonclicked-3 == buttonclickedswaptemp ) ||
                    ((buttonclicked-6 == buttonclickedswaptemp ) && position[(buttonclicked-3)-1] == PositionValue.emtpy) ||
                    ((buttonclicked+6 == buttonclickedswaptemp ) && position[(buttonclicked+3)-1] == PositionValue.emtpy) ||
                    (buttonclicked == 1 && buttonclickedswaptemp==3  && position[1] == PositionValue.emtpy) ||
                    (buttonclicked == 4 && buttonclickedswaptemp==6  && position[4] == PositionValue.emtpy) ||
                    (buttonclicked == 7 && buttonclickedswaptemp==9  && position[7] == PositionValue.emtpy) ||
                    (buttonclicked == 3 && buttonclickedswaptemp==1  && position[1] == PositionValue.emtpy) ||
                    (buttonclicked == 6 && buttonclickedswaptemp==4  && position[4] == PositionValue.emtpy) ||
                    (buttonclicked == 9 && buttonclickedswaptemp==7  && position[7] == PositionValue.emtpy) ||
                    (buttonclicked == 1 && buttonclickedswaptemp==9  && position[4] == PositionValue.emtpy) ||
                    (buttonclicked == 3 && buttonclickedswaptemp==7  && position[4] == PositionValue.emtpy) ||
                    (buttonclicked == 9 && buttonclickedswaptemp==1  && position[4] == PositionValue.emtpy) ||
                    (buttonclicked == 7 && buttonclickedswaptemp==3  && position[4] == PositionValue.emtpy) ||
                    (buttonclickedswaptemp==5 && position[buttonclicked-1] == PositionValue.emtpy) ||
                    (buttonclicked==5   ))  && position[buttonclickedswaptemp -1] == PositionValue.emtpy ) {
                value =true;
                break;
            }
        }
        return  value;
    }


    public void onDraw(Canvas canvas) {
/*
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#11FF00FF"));
        paint.setStrokeWidth(5f);
        float value  = button1.getWidth()/2;

        canvas.drawLine(button1.getX()+value,button1.getY() +value,button3.getX()+value,button3.getY()+value,paint);
        canvas.drawLine(button4.getX()+value,button4.getY() +value,button6.getX()+value,button6.getY()+value,paint);
        canvas.drawLine(button7.getX()+value,button7.getY() +value,button9.getX()+value,button9.getY()+value,paint);
        canvas.drawLine(button1.getX()+value,button1.getY() +value,button7.getX()+value,button7.getY()+value,paint);
        canvas.drawLine(button2.getX()+value,button2.getY() +value,button8.getX()+value,button8.getY()+value,paint);
        canvas.drawLine(button3.getX()+value,button3.getY() +value,button9.getX()+value,button9.getY()+value,paint);
        canvas.drawLine(button1.getX()+value,button1.getY() +value,button9.getX()+value,button9.getY()+value,paint);
        canvas.drawLine(button3.getX()+value,button3.getY() +value,button7.getX()+value,button7.getY()+value,paint);
*/

        Paint paintanimation = new Paint();
        if(buttonAnimation)
            StarAnimation(canvas,paintanimation);
        if(winanimation)
            StarWinAnimation(canvas,paintanimation);
        if(moveanimation)
            StartMoveAnimation(canvas,paintanimation);

    }


    private void StartMoveAnimation(Canvas canvas, Paint paint) {

        Bitmap bitmap;
        if(nextMove == NextMove.player1)
            bitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.gems2);
        else
            bitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.gems1);

        numberRandom1 =(int)getResources().getDimension(R.dimen.button_width)/2 - bitmap.getWidth()/2;
        numberRandom2 =(int)getResources().getDimension(R.dimen.button_height)/2 - bitmap.getHeight()/2;

        canvas.drawBitmap(bitmap,moveanimationXstart+numberRandom1,moveanimationYstart+numberRandom2,paint);

        moveanimationXstart += moveanimationDX;
        moveanimationYstart +=moveanimationDY;

        if((moveanimationDX >0 && moveanimationXstart>moveanimationXfinish) ||
                (moveanimationDX < 0 && moveanimationXstart<moveanimationXfinish) ||
                (moveanimationDY > 0 && moveanimationYstart > moveanimationYfinish) ||
                (moveanimationDY < 0 &&moveanimationYstart < moveanimationYfinish )){
            moveanimation = false;
            moveanimationXstart = 0;
            moveanimationYstart = 0 ;
            moveanimationXfinish = 0;
            moveanimationYfinish =0 ;
            moveanimationDX =0 ;
            moveanimationDY =0 ;
            if(nextMove == NextMove.player1)
                buttonlast.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gems2));
            else
                buttonlast.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.gems1));
            if(computermove) {
                ComputerMove();
                computermove = false;
                nextmovewincheck = true;
            }
        }
        stonePathView.invalidate();

    }


    private void StarWinAnimation(Canvas canvas, Paint paint) {

        Random random = new Random();

        if(winAnimationCount == 0) {
            numberRandom1 = random.nextInt(500);
            numberRandom2 = random.nextInt(500);
            numberRandom3 = random.nextInt(500);
            numberRandom4 = random.nextInt(500) * -1;
        }

        Bitmap bitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.star);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom1,winanimationY+numberRandom1+numberRandom2,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom1,winanimationY+numberRandom2,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom1+numberRandom3,winanimationY+numberRandom3,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom1,winanimationY+numberRandom4,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom2+numberRandom2,winanimationY+numberRandom1,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom2+numberRandom3,winanimationY+numberRandom2,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom2,winanimationY+numberRandom3,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom2,winanimationY+numberRandom4,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom3,winanimationY+numberRandom1+numberRandom2,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom3,winanimationY+numberRandom2+numberRandom4,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom3,winanimationY+numberRandom3,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom3,winanimationY+numberRandom4,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom4+numberRandom2,winanimationY+numberRandom3,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom4,winanimationY+numberRandom2,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom4+numberRandom1,winanimationY+numberRandom3,paint);
        canvas.drawBitmap(bitmap,winanimationX+numberRandom4,winanimationY+numberRandom4+numberRandom4,paint);

        winAnimationCount=(winAnimationCount+1)%10 ;
        stonePathView.invalidate();

    }

    private void StarAnimation(Canvas canvas, Paint paint) {

        Random random = new Random();

        if(buttonAnimationCount%20 ==0) {
            numberRandom1 = random.nextInt(100);
            numberRandom2 = random.nextInt(100);
            numberRandom3 = random.nextInt(100);
            numberRandom4 = random.nextInt(100);
        }

        Bitmap bitmap =  BitmapFactory.decodeResource(getResources(), R.drawable.star);
        canvas.drawBitmap(bitmap,buttonAnimationXstart+numberRandom1,buttonAnimationYstart+numberRandom2,paint);
        canvas.drawBitmap(bitmap,buttonAnimationXstart+numberRandom2,buttonAnimationYstart+numberRandom3,paint);
        canvas.drawBitmap(bitmap,buttonAnimationXstart+numberRandom4,buttonAnimationYstart+numberRandom1,paint);
        canvas.drawBitmap(bitmap,buttonAnimationXstart+numberRandom2,buttonAnimationYstart+numberRandom1,paint);
        canvas.drawBitmap(bitmap,buttonAnimationXstart+numberRandom3,buttonAnimationYstart+numberRandom4,paint);

        if(buttonAnimationXfinish!=0 && buttonswap==null) {
            canvas.drawBitmap(bitmap, buttonAnimationXfinish + numberRandom1, buttonAnimationYfinish + numberRandom2, paint);
            canvas.drawBitmap(bitmap, buttonAnimationXfinish + numberRandom2, buttonAnimationYfinish + numberRandom3, paint);
            canvas.drawBitmap(bitmap, buttonAnimationXfinish + numberRandom4, buttonAnimationYfinish + numberRandom1, paint);
            canvas.drawBitmap(bitmap, buttonAnimationXfinish + numberRandom2, buttonAnimationYfinish + numberRandom1, paint);
            canvas.drawBitmap(bitmap, buttonAnimationXfinish + numberRandom3, buttonAnimationYfinish + numberRandom4, paint);
        }
        if(buttonAnimationCount<0) {
            buttonAnimation = false;
            buttonAnimationCount=200;
            stonePathView.invalidate();
        }else{
            stonePathView.invalidate();
            buttonAnimationCount--;
        }

    }

    void showDialogDifficulty() {
        DialogFragment newFragment = DifficultyDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    void showDialogPlayers() {
        DialogFragment newFragment = PlayersDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void onDialogDismiss(int dialog) {
        if(dialog == DIALOG_PLAYER){
            if(players == Players.oneplayer) {
                showDialogDifficulty();
                playersButton.setText("One Player");
                difficultyButton.setEnabled(true);
                difficultyButton.setVisibility(View.VISIBLE);
            }else if(players == Players.twoplayer){
                playersButton.setText("Two Player");
                StartWifiConnection();
                difficultyButton.setEnabled(false);
                difficultyButton.setVisibility(View.INVISIBLE);
            }
        }else if(dialog == DIALOG_DIFFICULTY){
            if(difficulty == Difficulty.easy) {
                difficultyButton.setText("Easy");
            }else if(difficulty == Difficulty.normal) {
                difficultyButton.setText("Normal");
            }else if(difficulty == Difficulty.hard) {
                difficultyButton.setText("Hard");
            }
        }
        Initialize();
    }

    public static class DifficultyDialogFragment extends DialogFragment {
        static DifficultyDialogFragment newInstance() {
            return new DifficultyDialogFragment();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            MainActivity callingActivity = (MainActivity) getActivity();
            if(callingActivity!=null)
                callingActivity.onDialogDismiss(DIALOG_DIFFICULTY);
            super.onDismiss(dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_fragment_difficulty, container, false);
            Button easyButton =(Button) v.findViewById(R.id.buttonEasy);
            Button normalButton =(Button) v.findViewById(R.id.buttonNormal);
            Button hardButton =(Button) v.findViewById(R.id.buttonHard);

            easyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    difficulty  = Difficulty.easy;
                    dismiss();
                }
            });

            normalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    difficulty  = Difficulty.normal;
                    dismiss();
                }
            });

            hardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    difficulty  = Difficulty.hard;
                    dismiss();
                }
            });
            return v;
        }
    }

    public static class PlayersDialogFragment extends DialogFragment {
        static PlayersDialogFragment newInstance() {
            return new PlayersDialogFragment();
        }


        @Override
        public void onDismiss(DialogInterface dialog) {
            MainActivity callingActivity = (MainActivity) getActivity();
            if(callingActivity!=null)
                callingActivity.onDialogDismiss(DIALOG_PLAYER);
            super.onDismiss(dialog);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.dialog_fragment_players, container, false);
            Button buttonOnePlayer =(Button) v.findViewById(R.id.buttonOnePlayer);
            Button buttonTwoPlayer =(Button) v.findViewById(R.id.buttonTwoPlayer);

            buttonOnePlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    players  = Players.oneplayer;
                    dismiss();
                }
            });

            buttonTwoPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    players  = Players.twoplayer;
                    dismiss();
                }
            });

            return v;
        }
    }

    public static class FileServerAsyncTask extends AsyncTask<Void,Void,String> {

        private Context context;
        private TextView statusText;

        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a JPEG file
                 */
                final File f = new File(Environment.getExternalStorageDirectory() + "/"
                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                        + ".jpg");

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();
                InputStream inputstream = client.getInputStream();
                serverSocket.close();
                return f.getAbsolutePath();
            } catch (IOException e) {
                Log.e("Exception", e.getMessage());
                return null;
            }
        }

        /**
         * Start activity that can handle the JPEG image
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            }
        }
    }

    public boolean findNextMoveWin(){
        //1
        if(position[0]==position[1] && position[0]==PositionValue.player2 && position[2]==PositionValue.emtpy){
            if(position[0]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 3;
                buttonswap = button5;
                MakeChanges(button3);
                return  true;
            }else if(position[0]==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 3;
                buttonswap = button6;
                MakeChanges(button3);
                return  true;
            }else if(position[0]==position[6] && position[4] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 3;
                buttonswap = button7;
                MakeChanges(button3);
                return  true;
            }else if(position[0]==position[8] && position[5] == PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 9;
                buttonswap = button9;
                MakeChanges(button3);
                return  true;
            }
        }

        //2
        if(position[0]==position[2] && position[0]==PositionValue.player2&& position[1]==PositionValue.emtpy){
            if(position[0]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 2;
                buttonswap = button5;
                MakeChanges(button2);
                return  true;
            }else if(position[0]==position[7] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 8;
                buttonclicked = 2;
                buttonswap = button8;
                MakeChanges(button2);
                return  true;
            }
        }

        //3
        if(position[1]==position[2] && position[1]==PositionValue.player2 && position[0]==PositionValue.emtpy){
            if(position[1]==position[6] && position[3] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 1;
                buttonswap = button7;
                MakeChanges(button1);
                return  true;
            }else if(position[1]==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 1;
                buttonswap = button4;
                MakeChanges(button1);
                return  true;
            }else if(position[1]==position[8] && position[4] == PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 1;
                buttonswap = button9;
                MakeChanges(button1);
                return  true;
            }else if(position[1]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 1;
                buttonswap = button5;
                MakeChanges(button1);
                return  true;
            }
        }

        //4
        if(position[3]==position[4] && position[3]==PositionValue.player2 && position[5]==PositionValue.emtpy){
            if(position[3]==position[2]){
                buttonclickedswap = 3;
                buttonclicked = 6;
                buttonswap = button3;
                MakeChanges(button6);
                return  true;
            }else if(position[3]==position[8]){
                buttonclickedswap = 9;
                buttonclicked = 6;
                buttonswap = button9;
                MakeChanges(button6);
                return  true;
            }
        }

        //5
        if(position[4]==position[5] && position[4]==PositionValue.player2 && position[3]==PositionValue.emtpy){
            if(position[4]==position[0]){
                buttonclickedswap = 1;
                buttonclicked = 4;
                buttonswap = button1;
                MakeChanges(button4);
                return  true;
            }else if(position[4]==position[6]){
                buttonclickedswap = 7;
                buttonclicked = 4;
                buttonswap = button7;
                MakeChanges(button4);
                return  true;
            }
        }

        //6
        if(position[3]==position[5] && position[3]==PositionValue.player2  && position[4]==PositionValue.emtpy){
            if(position[0]== position[3] ){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(position[1]==position[3]){
                buttonclickedswap = 2;
                buttonclicked = 5;
                buttonswap = button2;
                MakeChanges(button5);
                return  true;
            }else if(position[2]==position[3]){
                buttonclickedswap = 3;
                buttonclicked = 5;
                buttonswap = button3;
                MakeChanges(button5);
                return  true;
            }else if(position[6]==position[3]){
                buttonclickedswap = 7;
                buttonclicked = 5;
                buttonswap = button7;
                MakeChanges(button5);
                return  true;
            }else if(position[7]==position[3]){
                buttonclickedswap = 8;
                buttonclicked = 5;
                buttonswap = button8;
                MakeChanges(button5);
                return  true;
            }else if(position[8]==position[3]){
                buttonclickedswap = 9;
                buttonclicked = 5;
                buttonswap = button9;
                MakeChanges(button5);
                return  true;
            }
        }

        //7
        if(position[6]==position[7] && position[6]==PositionValue.player2 && position[8]==PositionValue.emtpy){
            if(position[6]==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 9;
                buttonswap = button6;
                MakeChanges(button9);
                return  true;
            }else if(position[6]==position[2] && position[5]==PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 9;
                buttonswap = button3;
                MakeChanges(button9);
                return  true;
            }else if(position[6]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 9;
                buttonswap = button5;
                MakeChanges(button9);
                return  true;
            }else if(position[6]==position[0] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 9;
                buttonswap = button1;
                MakeChanges(button9);
                return  true;
            }
        }

        //8
        if(position[7]==position[8] && position[7]==PositionValue.player2 && position[6]==PositionValue.emtpy){
            if(position[7]==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 7;
                buttonswap = button4;
                MakeChanges(button7);
                return  true;
            }else if(position[7]==position[0] && position[3]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 7;
                buttonswap = button1;
                MakeChanges(button7);
                return  true;
            }else if(position[4]==position[7]){
                buttonclickedswap = 5;
                buttonclicked = 7;
                buttonswap = button5;
                MakeChanges(button7);
                return  true;
            }else if(position[7]==position[2] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 7;
                buttonswap = button3;
                MakeChanges(button7);
                return  true;
            }
        }

        //9
        if(position[6]==position[8] && position[6]==PositionValue.player2 && position[7]==PositionValue.emtpy){
            if(position[4]==position[6]){
                buttonclickedswap = 5;
                buttonclicked = 8;
                buttonswap = button5;
                MakeChanges(button8);
                return  true;
            }else if(position[1]==position[6] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 2;
                buttonclicked = 8;
                buttonswap = button2;
                MakeChanges(button8);
                return  true;
            }
        }


        //10
        if(position[0]==position[3] && position[0]==PositionValue.player2 && position[6]==PositionValue.emtpy){
            if(position[0]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 7;
                buttonswap = button5;
                MakeChanges(button7);
                return  true;
            }else if(position[0]==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 7;
                buttonswap = button8;
                MakeChanges(button7);
                return  true;
            }else if(position[0]==position[2] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 3;
                buttonclicked = 7;
                buttonswap = button3;
                MakeChanges(button7);
                return  true;
            }else if(position[0]==position[8] && position[7]==PositionValue.emtpy ){
                buttonclickedswap = 9;
                buttonclicked = 7;
                buttonswap = button9;
                MakeChanges(button7);
                return  true;
            }
        }

        //11
        if(position[0]==position[6] && position[0]==PositionValue.player2 && position[3]==PositionValue.emtpy){
            if(position[0]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 4;
                buttonswap = button5;
                MakeChanges(button4);
                return  true;
            }else if(position[0]==position[5] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 6;
                buttonclicked = 4;
                buttonswap = button6;
                MakeChanges(button4);
                return  true;
            }
        }

        //12
        if(position[3]==position[6] && position[3]==PositionValue.player2 && position[0]==PositionValue.emtpy){
            if(position[3]==position[1]){
                buttonclickedswap = 2;
                buttonclicked = 1;
                buttonswap = button2;
                MakeChanges(button1);
                return  true;
            }else if(position[3]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 1;
                buttonswap = button4;
                MakeChanges(button1);
                return  true;
            }else if(position[3]==position[8] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 9;
                buttonclicked = 1;
                buttonswap = button9;
                MakeChanges(button1);
                return  true;
            }else if(position[3]==position[2] && position[1]==PositionValue.emtpy ){
                buttonclickedswap = 3;
                buttonclicked = 3;
                buttonswap = button3;
                MakeChanges(button3);
                return  true;
            }
        }

        //13
        if(position[1]==position[4] && position[1]==PositionValue.player2  && position[7]==PositionValue.emtpy){
            if(position[1]==position[6]){
                buttonclickedswap = 7;
                buttonclicked = 8;
                buttonswap = button7;
                MakeChanges(button8);
                return  true;
            }else if(position[1]==position[8]){
                buttonclickedswap = 9;
                buttonclicked = 8;
                buttonswap = button9;
                MakeChanges(button8);
                return  true;
            }
        }

        //14
        if(position[1]==position[7] && position[1]==PositionValue.player2  && position[4]==PositionValue.emtpy){
            if(position[1]== position[0] ){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(position[1]==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 5;
                buttonswap = button4;
                MakeChanges(button5);
                return  true;
            }else if(position[2]==position[1]){
                buttonclickedswap = 3;
                buttonclicked = 5;
                buttonswap = button3;
                MakeChanges(button5);
                return  true;
            }else if(position[6]==position[1]){
                buttonclickedswap = 7;
                buttonclicked = 5;
                buttonswap = button7;
                MakeChanges(button5);
                return  true;
            }else if(position[5]==position[1]){
                buttonclickedswap = 6;
                buttonclicked = 5;
                buttonswap = button6;
                MakeChanges(button5);
                return  true;
            }else if(position[8]==position[1]){
                buttonclickedswap = 9;
                buttonclicked = 5;
                buttonswap = button9;
                MakeChanges(button5);
                return  true;
            }
        }

        //15
        if(position[4]==position[7] && position[4]==PositionValue.player2 && position[1]==PositionValue.emtpy){
            if(position[0]==position[4]){
                buttonclickedswap = 1;
                buttonclicked = 2;
                buttonswap = button1;
                MakeChanges(button2);
                return  true;
            }else if(position[2]==position[4]){
                buttonclickedswap = 3;
                buttonclicked = 2;
                buttonswap = button3;
                MakeChanges(button2);
                return  true;
            }
        }

        //16
        if(position[2]==position[5] && position[2]==PositionValue.player2 && position[8]==PositionValue.emtpy){
            if(position[2]==position[6] && position[7]==PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 9;
                buttonswap = button7;
                MakeChanges(button9);
                return  true;
            }else if(position[2]==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 9;
                buttonswap = button8;
                MakeChanges(button9);
                return  true;
            }else if(position[2]==position[0] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 1;
                buttonclicked = 9;
                buttonswap = button1;
                MakeChanges(button9);
                return  true;
            }else if(position[2]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 9;
                buttonswap = button5;
                MakeChanges(button9);
                return  true;
            }
        }

        //17
        if(position[2]==position[8] && position[2]==PositionValue.player2 && position[5]==PositionValue.emtpy){
            if(position[2]==position[3] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 4;
                buttonclicked = 6;
                buttonswap = button4;
                MakeChanges(button6);
                return  true;
            }else if(position[2]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 6;
                buttonswap = button5;
                MakeChanges(button6);
                return  true;
            }
        }

        //18
        if(position[5]==position[8] && position[5]==PositionValue.player2 && position[2]==PositionValue.emtpy){
            if(position[5]==position[0] && position[1]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 3;
                buttonswap = button1;
                MakeChanges(button3);
                return  true;
            }else if(position[1]==position[8]){
                buttonclickedswap = 2;
                buttonclicked = 3;
                buttonswap = button2;
                MakeChanges(button3);
                return  true;
            }else if(position[8]==position[6] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 7;
                buttonclicked = 3;
                buttonswap = button7;
                MakeChanges(button3);
                return  true;
            }else if(position[8]==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 3;
                buttonswap = button5;
                MakeChanges(button3);
                return  true;
            }
        }

        //19
        if(position[0]==position[4] && position[0]==PositionValue.player2 && position[8]==PositionValue.emtpy){
            if(position[0]==position[2] && position[5] == PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 9;
                buttonswap = button3;
                MakeChanges(button9);
                return  true;
            }else if(position[0]==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 9;
                buttonswap = button6;
                MakeChanges(button9);
                return  true;
            }else if(position[0]==position[6] && position[7] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 9;
                buttonswap = button7;
                MakeChanges(button9);
                return  true;
            }else if(position[0]==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 9;
                buttonswap = button8;
                MakeChanges(button9);
                return  true;
            }
        }


        //20
        if(position[0]==position[8] && position[0]==PositionValue.player2 && position[4]==PositionValue.emtpy){
            if(position[0]==position[3] ){
                buttonclickedswap = 4;
                buttonclicked = 5;
                buttonswap = button4;
                MakeChanges(button5);
                return  true;
            }else if(position[0]==position[6]){
                buttonclickedswap = 7;
                buttonclicked = 5;
                buttonswap = button7;
                MakeChanges(button5);
                return  true;
            }else if(position[0]==position[7] ){
                buttonclickedswap = 8;
                buttonclicked = 5;
                buttonswap = button8;
                MakeChanges(button5);
                return  true;
            }else if(position[0]==position[1]){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(position[0]==position[2]){
                buttonclickedswap = 3;
                buttonclicked = 5;
                buttonswap = button3;
                MakeChanges(button5);
                return  true;
            }else if(position[0]==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 5;
                buttonswap = button6;
                MakeChanges(button5);
                return  true;
            }
        }

        //21
        if(position[8]==position[4] && position[4]==PositionValue.player2 && position[0]==PositionValue.emtpy){
            if(position[4]==position[2] && position[1] == PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 1;
                buttonswap = button3;
                MakeChanges(button1);
                return  true;
            }else if(position[4]==position[1]){
                buttonclickedswap = 2;
                buttonclicked = 1;
                buttonswap = button2;
                MakeChanges(button1);
                return  true;
            }else if(position[4]==position[6] && position[3] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 1;
                buttonswap = button7;
                MakeChanges(button1);
                return  true;
            }else if(position[4]==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 1;
                buttonswap = button4;
                MakeChanges(button1);
                return  true;
            }
        }

        //22
        if(position[2]==position[4] && position[2]==PositionValue.player2 && position[6]==PositionValue.emtpy){
            if(position[2]==position[0] && position[3]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 7;
                buttonswap = button1;
                MakeChanges(button7);
                return  true;
            }else if(position[2]==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 7;
                buttonswap = button4;
                MakeChanges(button7);
                return  true;
            }else if(position[2]==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 7;
                buttonswap = button8;
                MakeChanges(button7);
                return  true;
            }else if(position[2]==position[8] && position[7]==PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 7;
                buttonswap = button9;
                MakeChanges(button7);
                return  true;
            }
        }

        //23
        if(position[2]==position[6] && position[2]==PositionValue.player2 && position[4]==PositionValue.emtpy){
            if(position[0]==position[6] ){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(position[1]==position[6]){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button2;
                MakeChanges(button5);
                return  true;
            }else if(position[3]==position[6] ){
                buttonclickedswap = 4;
                buttonclicked = 5;
                buttonswap = button4;
                MakeChanges(button5);
                return  true;
            }else if(position[7]==position[6]){
                buttonclickedswap = 8;
                buttonclicked = 5;
                buttonswap = button8;
                MakeChanges(button5);
                return  true;
            }else if(position[8]==position[6]){
                buttonclickedswap = 9;
                buttonclicked = 5;
                buttonswap = button9;
                MakeChanges(button5);
                return  true;
            }else if(position[6]==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 5;
                buttonswap = button6;
                MakeChanges(button5);
                return  true;
            }
        }

        //24
        if(position[6]==position[4] && position[4]==PositionValue.player2 && position[2]==PositionValue.emtpy){
            if(position[4]==position[0] && position[1]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 3;
                buttonswap = button1;
                MakeChanges(button3);
                return  true;
            }else if(position[4]==position[1]){
                buttonclickedswap = 2;
                buttonclicked = 3;
                buttonswap = button2;
                MakeChanges(button3);
                return  true;
            }else if(position[4]==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 3;
                buttonswap = button6;
                MakeChanges(button3);
                return  true;
            }else if(position[4]==position[8] && position[5]==PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 3;
                buttonswap = button9;
                MakeChanges(button3);
                return  true;
            }
        }

        playerwon =PositionValue.emtpy;
        return  false;

    }

    public boolean findNextMoveFailBeforeInitialmove(){


        //1
        if(position[0]==position[1] && position[0]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            button3.performClick();
            return  true;
        }

        //2
        if(position[0]==position[2] && position[0]==PositionValue.player1&& position[1]==PositionValue.emtpy){
            button2.performClick();
            return  true;

        }

        //3
        if(position[1]==position[2] && position[1]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            button1.performClick();
            return  true;

        }

        //4
        if(position[3]==position[4] && position[3]==PositionValue.player1 && position[5]==PositionValue.emtpy){
            button6.performClick();
            return  true;

        }

        //5
        if(position[4]==position[5] && position[4]==PositionValue.player1 && position[3]==PositionValue.emtpy){
            button4.performClick();
            return  true;

        }

        //6
        if(position[3]==position[5] && position[3]==PositionValue.player1  && position[4]==PositionValue.emtpy){
            button5.performClick();
            return  true;

        }

        //7
        if(position[6]==position[7] && position[6]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            button9.performClick();
            return  true;

        }

        //8
        if(position[7]==position[8] && position[7]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            button7.performClick();
            return  true;

        }

        //9
        if(position[6]==position[8] && position[6]==PositionValue.player1 && position[7]==PositionValue.emtpy){
            button8.performClick();
            return  true;

        }


        //10
        if(position[0]==position[3] && position[0]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            button7.performClick();
            return  true;

        }

        //11
        if(position[0]==position[6] && position[0]==PositionValue.player1 && position[3]==PositionValue.emtpy){
            button4.performClick();
            return  true;

        }

        //12
        if(position[3]==position[6] && position[3]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            button1.performClick();
            return  true;

        }

        //13
        if(position[1]==position[4] && position[1]==PositionValue.player1  && position[7]==PositionValue.emtpy){
            button8.performClick();
            return  true;

        }

        //14
        if(position[1]==position[7] && position[1]==PositionValue.player1  && position[4]==PositionValue.emtpy){
            button5.performClick();
            return  true;

        }

        //15
        if(position[4]==position[7] && position[4]==PositionValue.player1 && position[1]==PositionValue.emtpy){
            button2.performClick();
            return  true;

        }

        //16
        if(position[2]==position[5] && position[2]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            button9.performClick();
            return  true;

        }

        //17
        if(position[2]==position[8] && position[2]==PositionValue.player1 && position[5]==PositionValue.emtpy){
            button6.performClick();
            return  true;

        }

        //18
        if(position[5]==position[8] && position[5]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            button3.performClick();
            return  true;

        }

        //19
        if(position[0]==position[4] && position[0]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            button9.performClick();
            return  true;

        }


        //20
        if(position[0]==position[8] && position[0]==PositionValue.player1 && position[4]==PositionValue.emtpy){
            button5.performClick();
            return  true;

        }

        //21
        if(position[8]==position[4] && position[4]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            button1.performClick();
            return  true;

        }

        //22
        if(position[2]==position[4] && position[2]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            button7.performClick();
            return  true;

        }

        //23
        if(position[2]==position[6] && position[2]==PositionValue.player1 && position[4]==PositionValue.emtpy){
            button5.performClick();
            return  true;

        }

        //24
        if(position[6]==position[4] && position[4]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            button3.performClick();
            return  true;

        }


        return false;

    }


    public boolean findNextMoveFail(){


        //1
        if(position[0]==position[1] && position[0]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 3;
                buttonswap = button5;
                MakeChanges(button3);

                return  true;
            }else if(PositionValue.player2==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 3;
                buttonswap = button6;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[6] && position[4] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 3;
                buttonswap = button7;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[8] && position[5] == PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 9;
                buttonswap = button9;
                MakeChanges(button3);
                return  true;
            }
        }

        //2
        if(position[0]==position[2] && position[0]==PositionValue.player1&& position[1]==PositionValue.emtpy){
            if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 2;
                buttonswap = button5;
                MakeChanges(button2);
                return  true;
            }else if(PositionValue.player2==position[7] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 8;
                buttonclicked = 2;
                buttonswap = button8;
                MakeChanges(button2);
                return  true;
            }
        }

        //3
        if(position[1]==position[2] && position[1]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            if(PositionValue.player2==position[6] && position[3] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 1;
                buttonswap = button7;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 1;
                buttonswap = button4;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[8] && position[4] == PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 1;
                buttonswap = button9;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 1;
                buttonswap = button5;
                MakeChanges(button1);
                return  true;
            }
        }

        //4
        if(position[3]==position[4] && position[3]==PositionValue.player1 && position[5]==PositionValue.emtpy){
            if(PositionValue.player2==position[2]){
                buttonclickedswap = 3;
                buttonclicked = 6;
                buttonswap = button3;
                MakeChanges(button6);
                return  true;
            }else if(PositionValue.player2==position[8]){
                buttonclickedswap = 9;
                buttonclicked = 6;
                buttonswap = button9;
                MakeChanges(button6);
                return  true;
            }
        }

        //5
        if(position[4]==position[5] && position[4]==PositionValue.player1 && position[3]==PositionValue.emtpy){
            if(PositionValue.player2==position[0]){
                buttonclickedswap = 1;
                buttonclicked = 4;
                buttonswap = button1;
                MakeChanges(button4);
                return  true;
            }else if(PositionValue.player2==position[6]){
                buttonclickedswap = 7;
                buttonclicked = 4;
                buttonswap = button7;
                MakeChanges(button4);
                return  true;
            }
        }

        //6
        if(position[3]==position[5] && position[3]==PositionValue.player1  && position[4]==PositionValue.emtpy){
            if(position[0]==PositionValue.player2 ){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(position[1]==PositionValue.player2){
                buttonclickedswap = 2;
                buttonclicked = 5;
                buttonswap = button2;
                MakeChanges(button5);
                return  true;
            }else if(position[2]==PositionValue.player2){
                buttonclickedswap = 3;
                buttonclicked = 5;
                buttonswap = button3;
                MakeChanges(button5);
                return  true;
            }else if(position[6]==PositionValue.player2){
                buttonclickedswap = 7;
                buttonclicked = 5;
                buttonswap = button7;
                MakeChanges(button5);
                return  true;
            }else if(position[7]==PositionValue.player2){
                buttonclickedswap = 8;
                buttonclicked = 5;
                buttonswap = button8;
                MakeChanges(button5);
                return  true;
            }else if(position[8]==PositionValue.player2){
                buttonclickedswap = 9;
                buttonclicked = 5;
                buttonswap = button9;
                MakeChanges(button5);
                return  true;
            }
        }

        //7
        if(position[6]==position[7] && position[6]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            if(PositionValue.player2==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 9;
                buttonswap = button6;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[2] && position[5]==PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 9;
                buttonswap = button3;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 9;
                buttonswap = button5;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[0] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 9;
                buttonswap = button1;
                MakeChanges(button9);
                return  true;
            }
        }

        //8
        if(position[7]==position[8] && position[7]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            if(PositionValue.player2==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 7;
                buttonswap = button4;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[0] && position[3]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 7;
                buttonswap = button1;
                MakeChanges(button7);
                return  true;
            }else if(position[4]==PositionValue.player2){
                buttonclickedswap = 5;
                buttonclicked = 7;
                buttonswap = button5;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[2] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 7;
                buttonswap = button3;
                MakeChanges(button7);
                return  true;
            }
        }

        //9
        if(position[6]==position[8] && position[6]==PositionValue.player1 && position[7]==PositionValue.emtpy){
            if(position[4]==PositionValue.player2){
                buttonclickedswap = 5;
                buttonclicked = 8;
                buttonswap = button5;
                MakeChanges(button8);
                return  true;
            }else if(position[1]==PositionValue.player2 && position[4]==PositionValue.emtpy){
                buttonclickedswap = 2;
                buttonclicked = 8;
                buttonswap = button2;
                MakeChanges(button8);
                return  true;
            }
        }


        //10
        if(position[0]==position[3] && position[0]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 7;
                buttonswap = button5;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 7;
                buttonswap = button8;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[2] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 3;
                buttonclicked = 7;
                buttonswap = button3;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[8] && position[7]==PositionValue.emtpy ){
                buttonclickedswap = 9;
                buttonclicked = 7;
                buttonswap = button9;
                MakeChanges(button7);
                return  true;
            }
        }

        //11
        if(position[0]==position[6] && position[0]==PositionValue.player1 && position[3]==PositionValue.emtpy){
            if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 4;
                buttonswap = button5;
                MakeChanges(button4);
                return  true;
            }else if(PositionValue.player2==position[5] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 6;
                buttonclicked = 4;
                buttonswap = button6;
                MakeChanges(button4);
                return  true;
            }
        }

        //12
        if(position[3]==position[6] && position[3]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            if(PositionValue.player2==position[1]){
                buttonclickedswap = 2;
                buttonclicked = 1;
                buttonswap = button2;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 1;
                buttonswap = button4;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[8] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 9;
                buttonclicked = 1;
                buttonswap = button9;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[2] && position[1]==PositionValue.emtpy ){
                buttonclickedswap = 3;
                buttonclicked = 3;
                buttonswap = button3;
                MakeChanges(button3);
                return  true;
            }
        }

        //13
        if(position[1]==position[4] && position[1]==PositionValue.player1  && position[7]==PositionValue.emtpy){
            if(PositionValue.player2==position[6]){
                buttonclickedswap = 7;
                buttonclicked = 8;
                buttonswap = button7;
                MakeChanges(button8);
                return  true;
            }else if(PositionValue.player2==position[8]){
                buttonclickedswap = 9;
                buttonclicked = 8;
                buttonswap = button9;
                MakeChanges(button8);
                return  true;
            }
        }

        //14
        if(position[1]==position[7] && position[1]==PositionValue.player1  && position[4]==PositionValue.emtpy){
            if(PositionValue.player2== position[0] ){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 5;
                buttonswap = button4;
                MakeChanges(button5);
                return  true;
            }else if(position[2]==PositionValue.player2){
                buttonclickedswap = 3;
                buttonclicked = 5;
                buttonswap = button3;
                MakeChanges(button5);
                return  true;
            }else if(position[6]==PositionValue.player2){
                buttonclickedswap = 7;
                buttonclicked = 5;
                buttonswap = button7;
                MakeChanges(button5);
                return  true;
            }else if(position[5]==PositionValue.player2){
                buttonclickedswap = 6;
                buttonclicked = 5;
                buttonswap = button6;
                MakeChanges(button5);
                return  true;
            }else if(position[8]==PositionValue.player2){
                buttonclickedswap = 9;
                buttonclicked = 5;
                buttonswap = button9;
                MakeChanges(button5);
                return  true;
            }
        }

        //15
        if(position[4]==position[7] && position[4]==PositionValue.player1 && position[1]==PositionValue.emtpy){
            if(position[0]==PositionValue.player2){
                buttonclickedswap = 1;
                buttonclicked = 2;
                buttonswap = button1;
                MakeChanges(button2);
                return  true;
            }else if(position[2]==PositionValue.player2){
                buttonclickedswap = 3;
                buttonclicked = 2;
                buttonswap = button3;
                MakeChanges(button2);
                return  true;
            }
        }

        //16
        if(position[2]==position[5] && position[2]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            if(PositionValue.player2==position[6] && position[7]==PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 9;
                buttonswap = button7;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 9;
                buttonswap = button8;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[0] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 1;
                buttonclicked = 9;
                buttonswap = button1;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 9;
                buttonswap = button5;
                MakeChanges(button9);
                return  true;
            }
        }

        //17
        if(position[2]==position[8] && position[2]==PositionValue.player1 && position[5]==PositionValue.emtpy){
            if(PositionValue.player2==position[3] && position[4]==PositionValue.emtpy){
                buttonclickedswap = 4;
                buttonclicked = 6;
                buttonswap = button4;
                MakeChanges(button6);
                return  true;
            }else if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 6;
                buttonswap = button5;
                MakeChanges(button6);
                return  true;
            }
        }

        //18
        if(position[5]==position[8] && position[5]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            if(PositionValue.player2==position[0] && position[1]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 3;
                buttonswap = button1;
                MakeChanges(button3);
                return  true;
            }else if(position[1]==PositionValue.player2){
                buttonclickedswap = 2;
                buttonclicked = 3;
                buttonswap = button2;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[6] && position[4]==PositionValue.emtpy ){
                buttonclickedswap = 7;
                buttonclicked = 3;
                buttonswap = button7;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[4]){
                buttonclickedswap = 5;
                buttonclicked = 3;
                buttonswap = button5;
                MakeChanges(button3);
                return  true;
            }
        }

        //19
        if(position[0]==position[4] && position[0]==PositionValue.player1 && position[8]==PositionValue.emtpy){
            if(PositionValue.player2==position[2] && position[5] == PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 9;
                buttonswap = button3;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 9;
                buttonswap = button6;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[6] && position[7] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 9;
                buttonswap = button7;
                MakeChanges(button9);
                return  true;
            }else if(PositionValue.player2==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 9;
                buttonswap = button8;
                MakeChanges(button9);
                return  true;
            }
        }


        //20
        if(position[0]==position[8] && position[0]==PositionValue.player1 && position[4]==PositionValue.emtpy){
            if(PositionValue.player2==position[3] ){
                buttonclickedswap = 4;
                buttonclicked = 5;
                buttonswap = button4;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[6]){
                buttonclickedswap = 7;
                buttonclicked = 5;
                buttonswap = button7;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[7] ){
                buttonclickedswap = 8;
                buttonclicked = 5;
                buttonswap = button8;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[1]){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[2]){
                buttonclickedswap = 3;
                buttonclicked = 5;
                buttonswap = button3;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 5;
                buttonswap = button6;
                MakeChanges(button5);
                return  true;
            }
        }

        //21
        if(position[8]==position[4] && position[4]==PositionValue.player1 && position[0]==PositionValue.emtpy){
            if(PositionValue.player2==position[2] && position[1] == PositionValue.emtpy){
                buttonclickedswap = 3;
                buttonclicked = 1;
                buttonswap = button3;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[1]){
                buttonclickedswap = 2;
                buttonclicked = 1;
                buttonswap = button2;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[6] && position[3] == PositionValue.emtpy){
                buttonclickedswap = 7;
                buttonclicked = 1;
                buttonswap = button7;
                MakeChanges(button1);
                return  true;
            }else if(PositionValue.player2==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 1;
                buttonswap = button4;
                MakeChanges(button1);
                return  true;
            }
        }

        //22
        if(position[2]==position[4] && position[2]==PositionValue.player1 && position[6]==PositionValue.emtpy){
            if(PositionValue.player2==position[0] && position[3]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 7;
                buttonswap = button1;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[3]){
                buttonclickedswap = 4;
                buttonclicked = 7;
                buttonswap = button4;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[7]){
                buttonclickedswap = 8;
                buttonclicked = 7;
                buttonswap = button8;
                MakeChanges(button7);
                return  true;
            }else if(PositionValue.player2==position[8] && position[7]==PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 7;
                buttonswap = button9;
                MakeChanges(button7);
                return  true;
            }
        }

        //23
        if(position[2]==position[6] && position[2]==PositionValue.player1 && position[4]==PositionValue.emtpy){
            if(position[0]==PositionValue.player2 ){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button1;
                MakeChanges(button5);
                return  true;
            }else if(position[1]==PositionValue.player2){
                buttonclickedswap = 1;
                buttonclicked = 5;
                buttonswap = button2;
                MakeChanges(button5);
                return  true;
            }else if(position[3]==PositionValue.player2 ){
                buttonclickedswap = 4;
                buttonclicked = 5;
                buttonswap = button4;
                MakeChanges(button5);
                return  true;
            }else if(position[7]==PositionValue.player2){
                buttonclickedswap = 8;
                buttonclicked = 5;
                buttonswap = button8;
                MakeChanges(button5);
                return  true;
            }else if(position[8]==PositionValue.player2){
                buttonclickedswap = 9;
                buttonclicked = 5;
                buttonswap = button9;
                MakeChanges(button5);
                return  true;
            }else if(PositionValue.player2==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 5;
                buttonswap = button6;
                MakeChanges(button5);
                return  true;
            }
        }

        //24
        if(position[6]==position[4] && position[4]==PositionValue.player1 && position[2]==PositionValue.emtpy){
            if(PositionValue.player2==position[0] && position[1]==PositionValue.emtpy){
                buttonclickedswap = 1;
                buttonclicked = 3;
                buttonswap = button1;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[1]){
                buttonclickedswap = 2;
                buttonclicked = 3;
                buttonswap = button2;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[5]){
                buttonclickedswap = 6;
                buttonclicked = 3;
                buttonswap = button6;
                MakeChanges(button3);
                return  true;
            }else if(PositionValue.player2==position[8] && position[5]==PositionValue.emtpy){
                buttonclickedswap = 9;
                buttonclicked = 3;
                buttonswap = button9;
                MakeChanges(button3);
                return  true;
            }
        }


        return false;

    }

    public boolean findnextmovetrapswap(){

        if(position[0] != PositionValue.emtpy && position[1] != PositionValue.emtpy && position[2] != PositionValue.emtpy){
            if(position[3] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[5] == PositionValue.emtpy) {
                if(position[8] == PositionValue.player1 ){
                    return  true;
                }
            }else if(position[3] != PositionValue.emtpy && position[5] != PositionValue.emtpy&& position[4] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player1 || position[8] == PositionValue.player1 || position[6] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[3] == PositionValue.emtpy) {
                if(position[6] == PositionValue.player1){
                    return  true;
                }
            }
        }else if(position[0] != PositionValue.emtpy && position[3] != PositionValue.emtpy && position[6] != PositionValue.emtpy) {
            if(position[1] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[7] == PositionValue.emtpy) {
                if(position[8] == PositionValue.player1){
                    return  true;
                }
            }else if(position[1] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[5] == PositionValue.player1 || position[2] == PositionValue.player1 || position[8] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[1] == PositionValue.emtpy) {
                if(position[2] == PositionValue.player1){
                    return  true;
                }
            }
        }else if(position[6] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[8] != PositionValue.emtpy) {
            if(position[3] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[5] == PositionValue.emtpy) {
                if(position[2] == PositionValue.player1){
                    return  true;
                }
            }else if(position[3] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[2] == PositionValue.player1 || position[1] == PositionValue.player1 || position[0] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[3] == PositionValue.emtpy) {
                if(position[0] == PositionValue.player1){
                    return  true;
                }
            }
        }else if(position[2] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[8] != PositionValue.emtpy) {
            if (position[1] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[7] == PositionValue.emtpy) {
                if (position[6] == PositionValue.player1) {
                    return true;
                }
            } else if (position[1] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if (position[0] == PositionValue.player1 || position[3] == PositionValue.player1 || position[6] == PositionValue.player1) {
                    return true;
                }
            } else if (position[4] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[1] == PositionValue.emtpy) {
                if (position[0] == PositionValue.player1) {
                    return true;
                }
            }
        }

        if(position[0] != PositionValue.emtpy && position[1] != PositionValue.emtpy && position[3] != PositionValue.emtpy) {
            if(position[6] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[2] == PositionValue.emtpy) {
                if(position[5] == PositionValue.player1){
                    return  true;
                }
            }else if(position[6] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player1 || position[8] == PositionValue.player1 || position[5] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[6] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player1 || (position[8] == PositionValue.player1 && position[7] == PositionValue.emtpy)){
                    return  true;
                }
            }
        }else if(position[5] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[7] != PositionValue.emtpy) {
            if(position[6] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[2] == PositionValue.emtpy) {
                if(position[1] == PositionValue.player1 || (position[0] == PositionValue.player1 && position[1] == PositionValue.emtpy)){
                    return  true;
                }
            }else if(position[6] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[0] == PositionValue.player1 || position[1] == PositionValue.player1 || position[3] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[6] == PositionValue.emtpy) {
                if(position[3] == PositionValue.player1 || (position[0] == PositionValue.player1 && position[3] == PositionValue.emtpy)){
                    return  true;
                }
            }
        }else if(position[3] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[6] != PositionValue.emtpy) {
            if(position[0] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[8] == PositionValue.emtpy) {
                if(position[5] == PositionValue.player1 || (position[2] == PositionValue.player1 && position[5] == PositionValue.emtpy)){
                    return  true;
                }
            }else if(position[0] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[1] == PositionValue.player1 || position[2] == PositionValue.player1 || position[5] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[0] == PositionValue.emtpy) {
                if(position[1] == PositionValue.player1 || (position[2] == PositionValue.player1 && position[1] == PositionValue.emtpy)){
                    return  true;
                }
            }
        }else if(position[1] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[5] != PositionValue.emtpy) {
            if(position[0] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[8] == PositionValue.emtpy || (position[6] == PositionValue.player1 && position[7] == PositionValue.emtpy)) {
                if(position[7] == PositionValue.player1){
                    return  true;
                }
            }else if(position[0] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[3] == PositionValue.player1 || position[6] == PositionValue.player1 || position[7] == PositionValue.player1){
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[0] == PositionValue.emtpy) {
                if(position[3] == PositionValue.player1 || (position[6] == PositionValue.player1 && position[3] == PositionValue.emtpy)){
                    return  true;
                }
            }
        }
        return false;
    }

    public boolean findnextmovetrap(){

        if(position[0] != PositionValue.emtpy && position[1] != PositionValue.emtpy && position[2] != PositionValue.emtpy){
            if(position[3] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[5] == PositionValue.emtpy) {
                if(position[8] == PositionValue.player2){
                    buttonclickedswap = 9;
                    buttonclicked = 6;
                    buttonswap = button9;
                    MakeChanges(button6);
                    return  true;
                }
            }else if(position[3] != PositionValue.emtpy && position[5] != PositionValue.emtpy&& position[4] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player2){
                    buttonclickedswap = 8;
                    buttonclicked = 5;
                    buttonswap = button8;
                    MakeChanges(button5);
                    return  true;
                }else if(position[8] == PositionValue.player2){
                    buttonclickedswap = 9;
                    buttonclicked = 5;
                    buttonswap = button9;
                    MakeChanges(button5);
                    return  true;
                }else if(position[6] == PositionValue.player2){
                    buttonclickedswap = 7;
                    buttonclicked = 5;
                    buttonswap = button7;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[3] == PositionValue.emtpy) {
                if(position[6] == PositionValue.player2){
                    buttonclickedswap = 7;
                    buttonclicked = 4;
                    buttonswap = button7;
                    MakeChanges(button4);
                    return  true;
                }
            }
        }else if(position[0] != PositionValue.emtpy && position[3] != PositionValue.emtpy && position[6] != PositionValue.emtpy) {
            if(position[1] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[7] == PositionValue.emtpy) {
                if(position[8] == PositionValue.player2){
                    buttonclickedswap = 9;
                    buttonclicked = 8;
                    buttonswap = button9;
                    MakeChanges(button8);
                    return  true;
                }
            }else if(position[1] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[5] == PositionValue.player2){
                    buttonclickedswap = 6;
                    buttonclicked = 5;
                    buttonswap = button6;
                    MakeChanges(button5);
                    return  true;
                }else if(position[2] == PositionValue.player2){
                    buttonclickedswap = 3;
                    buttonclicked =5;
                    buttonswap = button3;
                    MakeChanges(button5);
                    return  true;
                }else if(position[8] == PositionValue.player2){
                    buttonclickedswap = 9;
                    buttonclicked =5;
                    buttonswap = button9;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[1] == PositionValue.emtpy) {
                if(position[2] == PositionValue.player2){
                    buttonclickedswap = 3;
                    buttonclicked =2;
                    buttonswap = button3;
                    MakeChanges(button2);
                    return  true;
                }
            }
        }else if(position[6] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[8] != PositionValue.emtpy) {
            if(position[3] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[5] == PositionValue.emtpy) {
                if(position[2] == PositionValue.player2){
                    buttonclickedswap = 3;
                    buttonclicked = 6;
                    buttonswap = button3;
                    MakeChanges(button6);
                    return  true;
                }
            }else if(position[3] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[2] == PositionValue.player2){
                    buttonclickedswap = 3;
                    buttonclicked =5;
                    buttonswap = button3;
                    MakeChanges(button5);
                    return  true;
                }else if(position[1] == PositionValue.player2){
                    buttonclickedswap = 2;
                    buttonclicked =5;
                    buttonswap = button2;
                    MakeChanges(button5);
                    return  true;
                }else if(position[0] == PositionValue.player2){
                    buttonclickedswap = 1;
                    buttonclicked =5;
                    buttonswap = button1;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[3] == PositionValue.emtpy) {
                if(position[0] == PositionValue.player2){
                    buttonclickedswap = 1;
                    buttonclicked = 4;
                    buttonswap = button1;
                    MakeChanges(button4);
                    return  true;
                }
            }
        }else if(position[2] != PositionValue.emtpy && position[5] != PositionValue.emtpy && position[8] != PositionValue.emtpy) {
            if(position[1] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[7] == PositionValue.emtpy) {
                if(position[6] == PositionValue.player2){
                    buttonclickedswap = 7;
                    buttonclicked =8;
                    buttonswap = button7;
                    MakeChanges(button8);
                    return  true;
                }
            }else if(position[1] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[0] == PositionValue.player2){
                    buttonclickedswap = 1;
                    buttonclicked = 5;
                    buttonswap = button1;
                    MakeChanges(button5);
                    return  true;
                }else if(position[3] == PositionValue.player2){
                    buttonclickedswap = 4;
                    buttonclicked =5;
                    buttonswap = button4;
                    MakeChanges(button5);
                    return  true;
                }else if(position[6] == PositionValue.player2){
                    buttonclickedswap = 7;
                    buttonclicked =5;
                    buttonswap = button7;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[1] == PositionValue.emtpy) {
                if(position[0] == PositionValue.player2){
                    buttonclickedswap = 1;
                    buttonclicked = 2;
                    buttonswap = button1;
                    MakeChanges(button2);
                    return  true;
                }
            }

            //diagonal
        }

        if(position[0] != PositionValue.emtpy && position[1] != PositionValue.emtpy && position[3] != PositionValue.emtpy) {
            if(position[6] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[2] == PositionValue.emtpy) {
                if(position[5] == PositionValue.player2){
                    buttonclickedswap = 6;
                    buttonclicked =3;
                    buttonswap = button6;
                    MakeChanges(button3);
                    return  true;
                }else if(position[5] == PositionValue.player2 && position[5] == PositionValue.emtpy){
                    buttonclickedswap = 9;
                    buttonclicked =3;
                    buttonswap = button9;
                    MakeChanges(button3);
                    return  true;
                }
            }else if(position[6] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player2){
                    buttonclickedswap = 8;
                    buttonclicked = 5;
                    buttonswap = button8;
                    MakeChanges(button5);
                    return  true;
                }else if(position[8] == PositionValue.player2){
                    buttonclickedswap = 9;
                    buttonclicked =5;
                    buttonswap = button9;
                    MakeChanges(button5);
                    return  true;
                }else if(position[5] == PositionValue.player2){
                    buttonclickedswap = 6;
                    buttonclicked =5;
                    buttonswap = button6;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[6] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player2){
                    buttonclickedswap = 8;
                    buttonclicked = 7;
                    buttonswap = button8;
                    MakeChanges(button7);
                    return  true;
                }else if(position[8] == PositionValue.player2 && position[7] == PositionValue.emtpy){
                    buttonclickedswap = 8;
                    buttonclicked = 7;
                    buttonswap = button8;
                    MakeChanges(button7);
                    return  true;
                }
            }
        }else if(position[5] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[7] != PositionValue.emtpy) {
            if(position[6] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[2] == PositionValue.emtpy) {
                if(position[1] == PositionValue.player2){
                    buttonclickedswap = 2;
                    buttonclicked =3;
                    buttonswap = button2;
                    MakeChanges(button3);
                    return  true;
                }else if(position[0] == PositionValue.player2 && position[1] == PositionValue.emtpy){
                    buttonclickedswap = 1;
                    buttonclicked =3;
                    buttonswap = button1;
                    MakeChanges(button3);
                    return  true;
                }
            }else if(position[6] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[0] == PositionValue.player2){
                    buttonclickedswap = 1;
                    buttonclicked = 5;
                    buttonswap = button1;
                    MakeChanges(button5);
                    return  true;
                }else if(position[1] == PositionValue.player2){
                    buttonclickedswap = 2;
                    buttonclicked =5;
                    buttonswap = button2;
                    MakeChanges(button5);
                    return  true;
                }else if(position[3] == PositionValue.player2){
                    buttonclickedswap = 4;
                    buttonclicked =5;
                    buttonswap = button4;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[6] == PositionValue.emtpy) {
                if(position[3] == PositionValue.player2){
                    buttonclickedswap = 4;
                    buttonclicked = 7;
                    buttonswap = button4;
                    MakeChanges(button7);
                    return  true;
                }else if(position[0] == PositionValue.player2 && position[3] == PositionValue.emtpy){
                    buttonclickedswap = 1;
                    buttonclicked = 7;
                    buttonswap = button1;
                    MakeChanges(button7);
                    return  true;
                }
            }
        }else if(position[3] != PositionValue.emtpy && position[7] != PositionValue.emtpy && position[6] != PositionValue.emtpy) {
            if(position[0] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[8] == PositionValue.emtpy) {
                if(position[5] == PositionValue.player2){
                    buttonclickedswap = 6;
                    buttonclicked =9;
                    buttonswap = button6;
                    MakeChanges(button9);
                    return  true;
                }else if(position[2] == PositionValue.player2 && position[5] == PositionValue.emtpy){
                    buttonclickedswap = 3;
                    buttonclicked =9;
                    buttonswap = button3;
                    MakeChanges(button9);
                    return  true;
                }
            }else if(position[0] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[1] == PositionValue.player2){
                    buttonclickedswap = 2;
                    buttonclicked = 5;
                    buttonswap = button2;
                    MakeChanges(button5);
                    return  true;
                }else if(position[2] == PositionValue.player2){
                    buttonclickedswap = 3;
                    buttonclicked =5;
                    buttonswap = button3;
                    MakeChanges(button5);
                    return  true;
                }else if(position[5] == PositionValue.player2){
                    buttonclickedswap = 6;
                    buttonclicked =5;
                    buttonswap = button6;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[0] == PositionValue.emtpy) {
                if(position[1] == PositionValue.player2){
                    buttonclickedswap = 2;
                    buttonclicked = 1;
                    buttonswap = button2;
                    MakeChanges(button1);
                    return  true;
                }else if(position[2] == PositionValue.player2 && position[1] == PositionValue.emtpy){
                    buttonclickedswap = 3;
                    buttonclicked = 1;
                    buttonswap = button3;
                    MakeChanges(button1);
                    return  true;
                }
            }
        }else if(position[1] != PositionValue.emtpy && position[2] != PositionValue.emtpy && position[5] != PositionValue.emtpy) {
            if(position[0] != PositionValue.emtpy && position[4] != PositionValue.emtpy && position[8] == PositionValue.emtpy) {
                if(position[7] == PositionValue.player2){
                    buttonclickedswap = 8;
                    buttonclicked =9;
                    buttonswap = button8;
                    MakeChanges(button9);
                    return  true;
                }else if(position[6] == PositionValue.player2 && position[7] == PositionValue.emtpy){
                    buttonclickedswap = 7;
                    buttonclicked =9;
                    buttonswap = button7;
                    MakeChanges(button9);
                    return  true;
                }
            }else if(position[0] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[4] == PositionValue.emtpy) {
                if(position[3] == PositionValue.player2){
                    buttonclickedswap = 4;
                    buttonclicked = 5;
                    buttonswap = button4;
                    MakeChanges(button5);
                    return  true;
                }else if(position[6] == PositionValue.player2){
                    buttonclickedswap = 7;
                    buttonclicked =5;
                    buttonswap = button7;
                    MakeChanges(button5);
                    return  true;
                }else if(position[7] == PositionValue.player2){
                    buttonclickedswap = 8;
                    buttonclicked =5;
                    buttonswap = button8;
                    MakeChanges(button5);
                    return  true;
                }
            }else if(position[4] != PositionValue.emtpy && position[8] != PositionValue.emtpy && position[0] == PositionValue.emtpy) {
                if(position[3] == PositionValue.player2){
                    buttonclickedswap = 4;
                    buttonclicked = 1;
                    buttonswap = button4;
                    MakeChanges(button1);
                    return  true;
                }else if(position[6] == PositionValue.player2 && position[3] == PositionValue.emtpy){
                    buttonclickedswap = 7;
                    buttonclicked = 1;
                    buttonswap = button7;
                    MakeChanges(button1);
                    return  true;
                }
            }
        }

        return false;
    }

}
