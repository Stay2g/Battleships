package com.example.tom.battleships;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;


public class GameActivity extends Activity implements View.OnClickListener{
    int arrTextViewsUsed[][] = new int[8][5];
    boolean ready = false;
    Handler handler = new Handler();

    int arrAllUsedTextViews[] = new int[24];
    int arrBot[] = new int[100];
    int arrNextShot[] = {-1, -1, -1};                                                               //Erster Treffer, aktueller Schuss,

    int arrTextViewsEnemy[][] = new int[arrTextViewsUsed.length][5];
    TextView arrTextViews[] = new TextView[200];
    ImageView arrShipsPlayer[] = new ImageView[arrTextViewsUsed.length];
    ImageView arrShipsEnemy[] = new ImageView[arrTextViewsUsed.length];
    int textViewSizePlayer, textViewSizeEnemy;
    GridLayout gridLayoutPlayer, gridLayoutEnemy;
    TextView textViewTurn;
    ImageButton buttonLocked;
    ImageView imageViewBackground;

    Button btnBack, btnAgain;

    //int firstHit = -1;
    //int nextShot[] = {-1, 0};                                                                      // arr[0] = nextShot, arr[1] = textViewId + x (x wird gemerkt)
    int playerShots[] = new int[100];
    int enemyShots[] = new int[100];
    boolean server, multiplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gridLayoutPlayer = (GridLayout) findViewById(R.id.gridLayout);
        gridLayoutEnemy = (GridLayout) findViewById(R.id.gridLayoutEnemy);
        textViewTurn = (TextView) findViewById(R.id.textViewTurn);
        buttonLocked = (ImageButton) findViewById(R.id.buttonLocked);
        imageViewBackground = (ImageView) findViewById(R.id.imageViewBackground);

        textViewSizeEnemy = (int) dpToPx(28);
        textViewSizePlayer = (int) dpToPx(20);

        btnBack = (Button) findViewById(R.id.btnBackToMenu);
        btnAgain = (Button) findViewById(R.id.btnAgain);

        btnAgain.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        createViews(gridLayoutPlayer, 100, textViewSizePlayer);
        createViews(gridLayoutEnemy, 0, textViewSizeEnemy);
        initTextView4Game();
        initBackground();
        initGridBackgrounds();
        textViewTurn.postDelayed(new Runnable() {
            @Override
            public void run() {
                initShipsPlayer();
                initShipsEnemy();
            }
        }, 50);

        buttonLocked.postDelayed(new Runnable() {
            @Override
            public void run() {
                ready = true;
            }
        }, 500);
        buttonLocked.setAlpha(0.0f);

        textViewTurn.postDelayed(new Runnable() {
            @Override
            public void run() {
                animShowShips();
            }
        }, 100);

        server = (boolean) getIntent().getSerializableExtra("server");
        multiplayer = (boolean) getIntent().getSerializableExtra("multiplayer");

        if(multiplayer) {
            if (server) {
                MpPreActivity.SERVERTHREAD.setActionCategory(2);
            } else {
                MpPreActivity.CLIENTTHREAD.setActionCategory(2);
            }
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean myTurn;

                if (server) {
                    myTurn = MpPreActivity.SERVERTHREAD.isMyTurn();
                } else {
                    myTurn = MpPreActivity.CLIENTTHREAD.isMyTurn();
                }

                if (ready) {
                    if (myTurn) {
                        gridLayoutEnemy.bringToFront();
                    } else {
                        buttonLocked.bringToFront();
                        enemyShotMultiplayer();
                    }
                }
                handler.postDelayed(this, 50);
            }
        }, 50);
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 100; i++) {
            if (arrTextViews[i].getId() == v.getId()) {
                if(multiplayer) {
                    playerShotMultiplayer(v.getId());
                } else {
                    playerShot(v.getId());
                }
                break;
            }
        }
        switch(v.getId()) {
            case R.id.btnAgain:
                finish();
                break;
            case R.id.btnBackToMenu:
                finish();
                break;
        }
        //if(v.getId() == findViewById(R.id.btnTest).getId()) {        }
    }

    public void createViews(GridLayout gl, int idOffset, int tvSize) {
        gl.setColumnCount(10);
        gl.setRowCount(10);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int i = col + row * 10;
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(tvSize, tvSize);

                arrTextViews[i+idOffset] = new TextView(this);
                arrTextViews[i+idOffset].setId(i+idOffset);
                //arrTextViews[i+idOffset].setBackgroundResource(R.drawable.textview_border);
                 if(idOffset == 0) {
                    arrTextViews[i + idOffset].setOnClickListener(this);
                }
                gl.addView(arrTextViews[i + idOffset], lp);
            }
        }
    }

    private void animShowShips() {
        for (int i = 0; i < arrTextViewsEnemy.length; i++) {
            arrShipsPlayer[i].animate().alpha(1.0f).setDuration(100);
            arrShipsPlayer[i].postDelayed(new Runnable() {@Override public void run() {}}, 100);
        }
    }

    private void initBackground() {
        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.background);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;

        imageViewBackground.setImageBitmap(Bitmap.createScaledBitmap(background, w, h, false));
    }

    private void initGridBackgrounds() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlActivityGame);
        Bitmap image, image2;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        lp.addRule(RelativeLayout.ALIGN_BOTTOM, gridLayoutPlayer.getId());
        lp.addRule(RelativeLayout.ALIGN_END, gridLayoutPlayer.getId());

        lp2.addRule(RelativeLayout.ALIGN_BOTTOM, gridLayoutEnemy.getId());
        lp2.addRule(RelativeLayout.ALIGN_END, gridLayoutEnemy.getId());

        ImageView grid = new ImageView(this);
        ImageView grid2 = new ImageView(this);

        image = BitmapFactory.decodeResource(getResources(), R.drawable.grid_background);
        image2 = BitmapFactory.decodeResource(getResources(), R.drawable.grid_background);

        grid.setImageBitmap(Bitmap.createScaledBitmap(image, textViewSizePlayer*11, textViewSizePlayer*11, false));
        grid2.setImageBitmap(Bitmap.createScaledBitmap(image2, textViewSizeEnemy*11, textViewSizeEnemy*11, false));

        rl.addView(grid, lp);
        rl.addView(grid2, lp2);
    }

    public float dpToPx(int dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private Bitmap scaleShipImage(int size, int shipImage, int group) {
        Bitmap ship = BitmapFactory.decodeResource(getResources(), shipImage);
        int textViewSize;
        if (group == 1) {
            textViewSize = textViewSizeEnemy;
        } else {
            textViewSize = textViewSizePlayer;
        }
        return Bitmap.createScaledBitmap(ship, textViewSize * size, textViewSize, false);
    }

    private void rotate(int i, int id, int l, ImageView[] arrShips) {
        Matrix matrix = new Matrix();
        Bitmap shipImage;
        if (arrShips == arrShipsEnemy) {
            if ((arrTextViewsEnemy[i][1] - arrTextViewsEnemy[i][0]) == 10) {
                shipImage = scaleShipImage(l, id, 1);
                matrix.setRotate(90f);
            } else {
                shipImage = scaleShipImage(l, id, 1);
                matrix.setRotate(180f);
            }
        } else {
            if ((arrTextViewsUsed[i][1] - arrTextViewsUsed[i][0]) == 10) {
                shipImage = scaleShipImage(l, id, 2);
                matrix.setRotate(90f);
            } else {
                shipImage = scaleShipImage(l, id, 2);
                matrix.setRotate(180f);
            }
        }
        Bitmap rotated = Bitmap.createBitmap(shipImage, 0, 0, shipImage.getWidth(), shipImage.getHeight(), matrix, false);
        arrShips[i].setImageBitmap(rotated);
    }

    private void initShipsPlayer() {
        Intent intent = getIntent();
        arrTextViewsUsed = (int[][]) intent.getSerializableExtra("textViewUsedPlayer");

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlActivityGame);
        int extX = (int) gridLayoutPlayer.getX();
        int extY = (int) gridLayoutPlayer.getY();

        for (int i = 0; i < arrTextViewsUsed.length; i++) {
            arrShipsPlayer[i] = new ImageView(this);
            arrShipsPlayer[i].setId(400+i);
            arrShipsPlayer[i].setAlpha(0.0f);
            rl.addView(arrShipsPlayer[i], RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            switch (i) {
                case 0:
                    rotate(i, R.drawable.ship_giant, 5, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 1:
                //case 2:
                    rotate(i, R.drawable.ship_big, 4, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 2: //3
                case 3: //4
                case 4: //5
                    rotate(i, R.drawable.ship_medium, 3, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 5: //6
                case 6: //7
                //case 8:
                case 7: //9
                    rotate(i, R.drawable.ship_small, 2, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
            }
        }
        gridLayoutPlayer.bringToFront();
    }

    private void initShipsEnemy() {
        Intent intent = getIntent();
        arrTextViewsEnemy = (int[][]) intent.getSerializableExtra("textViewUsedEnemy");

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlActivityGame);
        int extX = (int) gridLayoutEnemy.getX();
        int extY = (int) gridLayoutEnemy.getY();

        for (int i = 0; i < arrTextViewsEnemy.length; i++) {
            arrShipsEnemy[i] = new ImageView(this);
            arrShipsEnemy[i].setId(500+i);
            arrShipsEnemy[i].setAlpha(0.0f);
            rl.addView(arrShipsEnemy[i], RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            switch (i) {
                case 0:
                    rotate(i, R.drawable.ship_giant, 5, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
                case 1:
                    //case 2:
                    rotate(i, R.drawable.ship_big, 4, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
                case 2: //3
                case 3: //4
                case 4: //5
                    rotate(i, R.drawable.ship_medium, 3, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
                case 5: //6
                case 6: //7
                    //case 8:
                case 7: //9
                    rotate(i, R.drawable.ship_small, 2, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
            }
        }
    }

    private void initTextView4Game() {
        for(int i = 0; i < 100; i++) {
            enemyShots[i] = i;
            playerShots[i] = i;
            arrBot[i] = i;
        }
    }

    private void setTextViewImage(boolean hit, int id, int size) {
        Bitmap image;
        if(hit) {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.field_hit);
        } else {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.field_miss);
        }
        arrTextViews[id].setBackground(new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(image, size, size, false)));
    }

    private void playerShot(int vId) {
        for (int x = 0; x < 100; x++) {
            if (playerShots[x] == vId) {
                for (int i = 0; i < arrShipsPlayer.length; i++) {
                    int anArrTextViewsEnemy[] = arrTextViewsEnemy[i];
                    for (int k = 0; k < 5; k++) {
                        if (vId == anArrTextViewsEnemy[k]) {
                            //arrTextViews[x].setBackgroundColor(Color.RED);
                            setTextViewImage(true, x, textViewSizeEnemy);
                            playerShots[x] = -1;
                            anArrTextViewsEnemy[k] = -1;
                            for (int u = 0; u < 5; u++) {
                                if (anArrTextViewsEnemy[u] != -1) {
                                    break;
                                }
                                if (u == 4) {
                                    arrShipsEnemy[i].animate().alpha(1.0f).setDuration(100);
                                    arrShipsEnemy[i].bringToFront();
                                }
                            }
                            if (checkIfWon(arrTextViewsEnemy, playerShots)) {
                                gameIsOver(1);
                            }
                            bot();
                            return;
                        }
                    }
                }
                //arrTextViews[x].setBackgroundColor(Color.BLUE);
                setTextViewImage(false, x, textViewSizeEnemy);
                bot();
                playerShots[x] = -1;
                return;
            }
        }
    }

    //Multiplayer

    private void playerShotMultiplayer(int vId) {
        for (int textViewId = 0; textViewId < 100; textViewId++) {                                  //Durchläuft alle TextViews und schaut, ob auf textView noch nicht geschossen wurde
            if (playerShots[textViewId] == vId) {
                for (int ship = 0; ship < arrShipsEnemy.length; ship++) {                           //Wenn nicht, dann wird geschaut, ob es ein Treffer ist
                    int shipTextViews[] = arrTextViewsEnemy[ship];                                  //Array mit textViews eines Schiffs wird erzeugt (For-each-loop)
                    for (int i = 0; i < shipTextViews.length; i++) {                                //und durchlaufen
                        if (vId == shipTextViews[i]) {
                            setTextViewImage(true, textViewId, textViewSizeEnemy);                  //Treffer -> Rot
                            playerShots[textViewId] = -1;                                           //auf -1 gesetzt -> es wurde darauf geschossen
                            shipTextViews[i] = -1;                                                  //selbe wie oben -> kann Schiffversenkt berechnen

                            sendAction(Integer.toString(vId));                                      //Sendet TextViewId an anderes Gerät

                            for (int u = 0; u < 5; u++) {                                           //alle TextViewIds auf -1? Wenn ja, Treffer
                                if (shipTextViews[u] != -1) {
                                    break;
                                }
                                if (u == 4) {                                                       //Schleife komplett durchlaufen -> Schiff versenkt
                                    arrShipsEnemy[i].animate().alpha(1.0f).setDuration(100);
                                    arrShipsEnemy[i].bringToFront();
                                }
                            }
                            if (checkIfWon(arrTextViewsEnemy, playerShots)) {
                                gameIsOver(1);
                            }
                            //enemys turn
                            return;
                        }
                    }
                }
                setTextViewImage(false, textViewId, textViewSizeEnemy);
                playerShots[textViewId] = -1;
                sendAction(Integer.toString(vId));
                //enemys turn
                return;
            }
        }

    }

    private void enemyShotMultiplayer() {

        int vId;
        if(server) {
            vId = MpPreActivity.SERVERTHREAD.getEnemyShot();
        } else {
            vId = MpPreActivity.CLIENTTHREAD.getEnemyShot();
        }

        if(vId != -1) {
            getAllUsedTV();
            enemyShots[vId] = -1;
            boolean hit = checkIfHit(vId);
            setTextViewImage(hit, vId + 100, textViewSizePlayer);

            if (checkIfWon(arrTextViewsUsed, enemyShots)) {
                gameIsOver(1);
                //sagen das er gewonnen hat
            }

            if (server) {
                MpPreActivity.SERVERTHREAD.setMyTurn(false);
            } else {
                MpPreActivity.CLIENTTHREAD.setMyTurn(false);
            }
        }
    }





    private boolean checkIfWon(int arr[][], int shots[]) {
        for (int[] anArr : arr) {
            for (int k = 0; k < 5; k++) {
                for (int i = 0; i < 100; i++) {
                    if ((anArr[k] == shots[i]) & anArr[k] != -1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void gameIsOver(int wonLost) {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlActivityGame);
        RelativeLayout.LayoutParams lpScreen =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams lpText =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lpBtnBack =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams lpBtnAgain =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        TextView gameOverScreen = new TextView(this);
        TextView gameOverText = new TextView(this);

        gameOverScreen.setBackgroundColor(Color.BLACK);
        gameOverScreen.setAlpha(0.7f);


        gameOverText.setTextSize(25);
        gameOverText.setMaxLines(3);
        gameOverText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        gameOverText.setTypeface(null, Typeface.BOLD);
        lpText.addRule(RelativeLayout.CENTER_IN_PARENT);

        if(wonLost == 1) {
            gameOverText.setText(getString(R.string.strWon) + "\n \n" + getString(R.string.strVictory));
        } else {
            gameOverText.setText(getString(R.string.strLost) + "\n \n" + getString(R.string.strGameOver));
        }

        lpBtnBack.addRule(RelativeLayout.ALIGN_PARENT_START);
        lpBtnBack.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lpBtnBack.setMargins((int) dpToPx(40), 0, 0, (int) dpToPx(40));

        lpBtnAgain.addRule(RelativeLayout.ALIGN_PARENT_END);
        lpBtnAgain.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lpBtnAgain.setMargins(0, 0, (int) dpToPx(40), (int) dpToPx(40));

        btnBack.setText(getResources().getString(R.string.strBackToMenu));
        btnBack.setBackgroundColor(Color.TRANSPARENT);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setLayoutParams(lpBtnBack);

        btnAgain.setText(getResources().getString(R.string.strAgain));
        btnAgain.setBackgroundColor(Color.TRANSPARENT);
        btnAgain.setVisibility(View.VISIBLE);
        btnAgain.setLayoutParams(lpBtnAgain);

        rl.addView(gameOverScreen, lpScreen);
        rl.addView(gameOverText, lpText);
    }

    private void sendAction(String action) {
        if(server) {
            PrintWriter outS;
            try {
                if(MpPreActivity.SERVERTHREAD.getSocket() != null) {
                    outS = new PrintWriter(new BufferedWriter(new OutputStreamWriter(MpPreActivity.SERVERTHREAD.getSocket().getOutputStream())), true);
                    outS.println(action);
                } else {
                    Toast.makeText(getBaseContext(), "Connection lost", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Socked closed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            PrintWriter outC;
            try {
                if(MpPreActivity.CLIENTTHREAD.getSocket() != null) {
                    outC = new PrintWriter(new BufferedWriter(new OutputStreamWriter(MpPreActivity.CLIENTTHREAD.getSocket().getOutputStream())), true);
                    outC.println(action);
                } else {
                    Toast.makeText(getBaseContext(), "Connection lost", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Socked closed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

/*
    private void randomShot() {
        int textViewId;
        while(true) {
            textViewId = (int) (Math.random()*101);

            for (int i = 0; i < 100; i++) {
                if(enemyShots[i] == textViewId) {
                    for (int[] anArrTextViewsUsed : arrTextViewsUsed) {
                        for (int k = 0; k < 5; k++) {
                            if (textViewId == anArrTextViewsUsed[k]) {
                                arrTextViews[i + 100].setBackgroundColor(Color.RED);
                                enemyShots[i] = -1;
                                if (checkIfWon(arrTextViewsUsed, enemyShots)) {
                                    gameIsOver(0);
                                }
                                return;
                            }
                        }
                    }

                    arrTextViews[i + 100].setBackgroundColor(Color.BLUE);
                    enemyShots[i] = -1;
                    return;
                }
            }
        }
    }

    private void botLevel1() {
        int textViewId;
        while(true) {
            if (nextShot[0] == -1) {
                textViewId = (int) (Math.random() * 101);
            } else {
                textViewId = nextShot[0];
            }
            for (int i = 0; i < 100; i++) {
                if(enemyShots[i] == textViewId) {
                    for (int[] anArrTextViewsUsed : arrTextViewsUsed) {
                        for (int k = 0; k < 5; k++) {
                            if (textViewId == anArrTextViewsUsed[k]) {
                                //arrTextViews[i + 100].setBackgroundColor(Color.RED);
                                setTextViewImage(true, i + 100, textViewSizeEnemy);
                                enemyShots[i] = -1;
                                anArrTextViewsUsed[k] = -1;
                                if (nextShot[1] == 0) {
                                    for (int y = 0; y < 100; y++) {
                                        if (enemyShots[y] == ((textViewId + 1)) & ((textViewId + 1) < 100)) {
                                            nextShot[0] = textViewId + 1;
                                            nextShot[1] = 1;
                                            break;
                                        }
                                        if (enemyShots[y] == ((textViewId - 1)) & ((textViewId - 1) >= 0)) {
                                            nextShot[0] = textViewId - 1;
                                            nextShot[1] = -1;
                                            break;
                                        }
                                        if (enemyShots[y] == ((textViewId + 10)) & ((textViewId + 10) < 100)) {
                                            nextShot[0] = textViewId + 10;
                                            nextShot[1] = 10;
                                            break;
                                        }
                                        if (enemyShots[y] == ((textViewId - 10)) & ((textViewId - 10 >= 0))) {
                                            nextShot[0] = textViewId - 10;
                                            nextShot[1] = -10;
                                            break;
                                        }
                                    }
                                } else {
                                    int ext = 0;
                                    switch (nextShot[1]) {
                                        case 1:
                                            ext += 1;
                                            break;
                                        case -1:
                                            ext += -1;
                                            break;
                                        case 10:
                                            ext += 10;
                                            break;
                                        case -10:
                                            ext += -10;
                                            break;
                                    }
                                    nextShot[0] = textViewId + ext;
                                }
                                if (checkIfWon(arrTextViewsUsed, enemyShots)) {
                                    gameIsOver(0);
                                }
                                if(firstHit == -1) {
                                    firstHit = i;
                                }
                                for (int u = 0; u < 5; u++) {
                                    if (anArrTextViewsUsed[u] != -1) {
                                        break;
                                    }
                                    if (u == 4) {
                                        firstHit = -1;
                                        nextShot[0] = -1;
                                        nextShot[1] = 0;
                                    }
                                }
                                return;
                            }
                        }
                    }
                    //arrTextViews[i + 100].setBackgroundColor(Color.BLUE);
                    setTextViewImage(false, i + 100, textViewSizeEnemy);
                    enemyShots[i] = -1;
                    if(firstHit != -1) {
                        for (int y = 0; y < 100; y++) {
                            if (enemyShots[y] == (firstHit + 1)) {
                                nextShot[0] = firstHit + 1;
                                nextShot[1] = 1;
                                break;
                            }
                            if (enemyShots[y] == (firstHit - 1)) {
                                nextShot[0] = firstHit - 1;
                                nextShot[1] = -1;
                                break;
                            }
                            if (enemyShots[y] == (firstHit + 10)) {
                                nextShot[0] = firstHit + 10;
                                nextShot[1] = 10;
                                break;
                            }
                            if (enemyShots[y] == (firstHit - 10)) {
                                nextShot[0] = firstHit - 10;
                                nextShot[1] = -10;
                                break;
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    private void botLevel2()  {
        int textViewId;
        int nextShot = -1;

        if (nextShot == -1) {
            textViewId = (int) (Math.random() * 101);
        }




        //TODO: Wenn Schiff zerstört, auch Felder um das Schiff nicht mehr beschießen
    }
*/


    private void getAllUsedTV() {
        int count = 0;
        for(int i = 0; i < arrTextViewsUsed.length; i++) {
            for (int j = 0; j < 5; j++) {
                if (arrTextViewsUsed[i][j] != -1) {
                    arrAllUsedTextViews[count] = arrTextViewsUsed[i][j];
                    count++;
                }
            }
        }
        sort(arrAllUsedTextViews);
    }

    private boolean checkIfHit(int shot) {
        for (int i = 0; i < arrAllUsedTextViews.length; i++) {
            if (shot == arrAllUsedTextViews[i]) {
                return true;
            }
        }
        return false;
    }

    private void bot() {
        getAllUsedTV();
        int targetTextView;
        boolean hit = false;

        if (!hit) {             //Vorheriger Schuss war KEIN Treffer
            int ret;
            do {                //Sucht neues Feld, auf das noch nicht geschossen wurde
                targetTextView = (int) (Math.random()*100);
                ret = binarySearch(arrBot, targetTextView);
            } while (ret < 0);

            arrBot[ret] = -1;   //Feld aus zu schießenden Feldern entfernen

            hit = checkIfHit(targetTextView);
            if (hit) {
                arrNextShot[0] = ret;
                arrNextShot[1] = ret;
            }

            setTextViewImage(hit, targetTextView + 100, textViewSizeEnemy);

        } else {                //Vorheriger Schuss war EIN Treffer
            int step = 0;
            int treffer = arrNextShot[1];
            int ret;
            do {
                switch (step) {
                    case 0:         //links
                        treffer++;
                        ret = binarySearch(arrBot, treffer);
                        if (ret < 0) {
                            step++;
                        } else {
                            hit = checkIfHit(treffer);
                            arrBot[ret] = -1;
                            setTextViewImage(hit, treffer + 100, textViewSizeEnemy);
                            arrNextShot[1] = ret;
                            step = 4;
                        }
                        break;
                    case 1:         //rechts
                        treffer = -2;
                        ret = binarySearch(arrBot, treffer);
                        if (ret < 0) {
                            step++;
                        } else {
                            hit = checkIfHit(treffer);
                            arrBot[ret] = -1;
                            setTextViewImage(hit, treffer + 100, textViewSizeEnemy);
                            arrNextShot[1] = ret;
                            step = 4;
                        }
                        break;
                    case 2:         //oben
                        treffer = -9;
                        ret = binarySearch(arrBot, treffer);
                        if (ret < 0) {
                            step++;
                        } else {
                            hit = checkIfHit(treffer);
                            arrBot[ret] = -1;
                            setTextViewImage(hit, treffer + 100, textViewSizeEnemy);
                            arrNextShot[1] = ret;
                            step = 4;
                        }
                        break;
                    case 3:         //unten
                        treffer += 20;
                        ret = binarySearch(arrBot, treffer);
                        if (ret < 0) {
                            step++;
                        } else {
                            hit = checkIfHit(treffer);
                            arrBot[ret] = -1;
                            setTextViewImage(hit, treffer + 100, textViewSizeEnemy);
                            arrNextShot[1] = ret;
                            step = 4;
                        }
                        break;
                    case 4:
                        int val = (arrNextShot[0] - arrNextShot[1]);
                        if (val < 10 & val > -10) {

                        }
                        break;
                }
            } while (step != 4);

        }


    }

    private void botLevel9000() {
        int textViewId = -1;
        while(true) {

            for(int[] arr : arrTextViewsUsed) {
                for (int j = 0; j < 5; j++) {
                    if (arr[j] != -1) {
                        textViewId = arr[j];
                        break;
                    }
                }
                if(textViewId != -1) {
                    break;
                }
            }

            for (int i = 0; i < 100; i++) {
                if(enemyShots[i] == textViewId) {
                    for (int[] anArrTextViewsUsed : arrTextViewsUsed) {
                        for (int k = 0; k < 5; k++) {
                            if (textViewId == anArrTextViewsUsed[k]) {
                                //arrTextViews[i + 100].setBackgroundColor(Color.RED);
                                setTextViewImage(true, i + 100, textViewSizeEnemy);
                                enemyShots[i] = -1;
                                anArrTextViewsUsed[k] = -1;
                                if (checkIfWon(arrTextViewsUsed, enemyShots)) {
                                    gameIsOver(0);
                                }
                                return;
                            }
                        }
                    }
                    //arrTextViews[i + 100].setBackgroundColor(Color.BLUE);
                    setTextViewImage(false, i + 100, textViewSizeEnemy);
                    enemyShots[i] = -1;
                    return;
                }
            }
        }
    } //unschaffbar (jeder Schuss = 1 Treffer)
}
