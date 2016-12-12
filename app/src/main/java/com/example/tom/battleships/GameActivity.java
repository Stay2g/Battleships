package com.example.tom.battleships;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class GameActivity extends Activity implements View.OnClickListener{
    int arrTextViewsUsed[][] = new int[10][5];
    int arrTextViewsEnemy[][] = new int[10][5];
    TextView arrTextViews[] = new TextView[200];
    ImageView arrShipsPlayer[] = new ImageView[10];
    int textViewSizePlayer, textViewSizeEnemy;
    GridLayout gridLayoutPlayer, gridLayoutEnemy;
    TextView textViewArrow;
    boolean playerTurn;
    int textViewForGame[] = new int[100];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        gridLayoutPlayer = (GridLayout) findViewById(R.id.gridLayoutPlayer);
        gridLayoutEnemy = (GridLayout) findViewById(R.id.gridLayoutEnemy);
        textViewArrow = (TextView) findViewById(R.id.textViewArrow);

        textViewSizeEnemy = (int) dpToPx(30);
        textViewSizePlayer = (int) dpToPx(22);
        textViewArrow.setText("<-Tom=gay-");

        findViewById(R.id.btnTest).setOnClickListener(this);

        createViews(gridLayoutPlayer, 100, textViewSizePlayer);
        createViews(gridLayoutEnemy, 0, textViewSizeEnemy);
        initTextView4Game();

        textViewArrow.postDelayed(new Runnable() {
            @Override
            public void run() {
                initShipsPlayer();
            }
        }, 50);

        textViewArrow.postDelayed(new Runnable() {
            @Override
            public void run() {

                animShowShips();
            }
        }, 100);
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 100; i++) {
            if (arrTextViews[i].getId() == v.getId()) {
                arrTextViews[i].setBackgroundColor(Color.BLUE);
                randomShot();
                break;
            }
        }
        if(v.getId() == findViewById(R.id.btnTest).getId()) {

        }
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
                arrTextViews[i+idOffset].setBackgroundResource(R.drawable.textview_border);
                arrTextViews[i+idOffset].setAlpha(0.7f);
                if(idOffset == 0) {
                    arrTextViews[i + idOffset].setOnClickListener(this);
                }
                gl.addView(arrTextViews[i + idOffset], lp);
            }
        }
    }

    private void animShowShips() {
        for (int i = 0; i < 10; i++) {
            arrShipsPlayer[i].animate().alpha(1.0f).setDuration(100);
            arrShipsPlayer[i].postDelayed(new Runnable() {@Override public void run() {}}, 100);
        }
    }

    public float dpToPx(int dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private Bitmap scaleShipImage(int size, int shipImage) {
        Bitmap ship = BitmapFactory.decodeResource(getResources(), shipImage);
        return Bitmap.createScaledBitmap(ship, textViewSizePlayer * size, textViewSizePlayer, false);
    }

    private void rotate(int i, int id, int l) {
        Matrix matrix = new Matrix();
        Bitmap shipImage = scaleShipImage(l, id);
        if ((arrTextViewsUsed[i][1] - arrTextViewsUsed[i][0]) == 10) {
            matrix.setRotate(90f);
        } else {
            matrix.setRotate(180f);
        }
        Bitmap rotated = Bitmap.createBitmap(shipImage, 0, 0, shipImage.getWidth(), shipImage.getHeight(), matrix, false);
        arrShipsPlayer[i].setImageBitmap(rotated);
    }

    private void initShipsPlayer() {
        Intent intent = getIntent();
        arrTextViewsUsed = (int[][]) intent.getSerializableExtra("textViewUsed");

        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlActivityGame);
        int extX = (int) gridLayoutPlayer.getX();
        int extY = (int) gridLayoutPlayer.getY();

        for (int i = 0; i < 10; i++) {
            arrShipsPlayer[i] = new ImageView(this);
            arrShipsPlayer[i].setId(400+i);
            arrShipsPlayer[i].setAlpha(0.0f);
            rl.addView(arrShipsPlayer[i], RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            switch (i) {
                case 0:
                    rotate(i, R.drawable.giantship, 5);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 1:
                case 2:
                    rotate(i, R.drawable.bigship, 4);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 3:
                case 4:
                case 5:
                    rotate(i, R.drawable.mediumship, 3);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    rotate(i, R.drawable.smallship, 2);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
            }
        }
        gridLayoutPlayer.bringToFront();
    }

    private void initTextView4Game() {
        for(int i = 0; i < 100; i++) {
            textViewForGame[i] = i;
        }
    }

    private void randomShot() {
        int textViewId;
        while(true) {
            textViewId = (int) (Math.random()*101);

            for (int i = 0; i < 100; i++) {
                if(textViewForGame[i] == textViewId) {
                    for (int j = 0; j < 10; j++) {
                        for (int k = 0; k < 5; k++) {
                            if (textViewId == arrTextViewsUsed[j][k]) {
                                arrTextViews[i + 100].setBackgroundColor(Color.RED);
                                textViewForGame[i] = -1;
                                return;
                            }
                        }
                    }
                    arrTextViews[i + 100].setBackgroundColor(Color.BLUE);
                    textViewForGame[i] = -1;
                    return;
                }
            }
        }

    }

    private void initShipsEnemy() {
        
    }
}
