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
import android.media.Image;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class GameActivity extends Activity implements View.OnClickListener{
    int arrTextViewsUsed[][] = new int[8][5];
    int arrTextViewsEnemy[][] = new int[arrTextViewsUsed.length][5];
    TextView arrTextViews[] = new TextView[200];
    ImageView arrShipsPlayer[] = new ImageView[arrTextViewsUsed.length];
    ImageView arrShipsEnemy[] = new ImageView[arrTextViewsUsed.length];
    int textViewSizePlayer, textViewSizeEnemy;
    GridLayout gridLayoutPlayer, gridLayoutEnemy;
    TextView textViewArrow;
    int firstHit = -1;
    int nextShot[] = {-1, 0};                                                                      // arr[0] = nextShot, arr[1] = textViewId + x (x wird gemerkt)
    int playerShots[] = new int[100];
    int enemyShots[] = new int[100];

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
        textViewArrow.setText("...");

        findViewById(R.id.btnTest).setOnClickListener(this);

        createViews(gridLayoutPlayer, 100, textViewSizePlayer);
        createViews(gridLayoutEnemy, 0, textViewSizeEnemy);
        initTextView4Game();

        textViewArrow.postDelayed(new Runnable() {
            @Override
            public void run() {
                initShipsPlayer();
                initShipsEnemy();
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
                playerShot(v.getId());
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
        for (int i = 0; i < arrTextViewsEnemy.length; i++) {
            arrShipsPlayer[i].animate().alpha(1.0f).setDuration(100);
            arrShipsPlayer[i].postDelayed(new Runnable() {@Override public void run() {}}, 100);
        }
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
                    rotate(i, R.drawable.giantship, 5, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 1:
                //case 2:
                    rotate(i, R.drawable.bigship, 4, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 2: //3
                case 3: //4
                case 4: //5
                    rotate(i, R.drawable.mediumship, 3, arrShipsPlayer);
                    arrShipsPlayer[i].setX(extX + arrTextViews[arrTextViewsUsed[i][0]+100].getX());
                    arrShipsPlayer[i].setY(extY + arrTextViews[arrTextViewsUsed[i][0]+100].getY());
                    break;
                case 5: //6
                case 6: //7
                //case 8:
                case 7: //9
                    rotate(i, R.drawable.smallship, 2, arrShipsPlayer);
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
                    rotate(i, R.drawable.giantship, 5, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
                case 1:
                    //case 2:
                    rotate(i, R.drawable.bigship, 4, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
                case 2: //3
                case 3: //4
                case 4: //5
                    rotate(i, R.drawable.mediumship, 3, arrShipsEnemy);
                    arrShipsEnemy[i].setX(extX + arrTextViews[arrTextViewsEnemy[i][0]].getX());
                    arrShipsEnemy[i].setY(extY + arrTextViews[arrTextViewsEnemy[i][0]].getY());
                    break;
                case 5: //6
                case 6: //7
                    //case 8:
                case 7: //9
                    rotate(i, R.drawable.smallship, 2, arrShipsEnemy);
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
        }
    }

    private void playerShot(int vId) {
        for (int x = 0; x < 100; x++) {
            if (playerShots[x] == vId) {
                for (int i = 0; i < arrShipsPlayer.length; i++) {
                    int anArrTextViewsEnemy[] = arrTextViewsEnemy[i];
                    for (int k = 0; k < 5; k++) {
                        if (vId == anArrTextViewsEnemy[k]) {
                            arrTextViews[x].setBackgroundColor(Color.RED);
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
                                Toast.makeText(this, "Du hast gewonnen!", Toast.LENGTH_LONG).show();
                                textViewArrow.setText("won");
                            }
                            botLevel1();
                            return;
                        }
                    }

                }
                arrTextViews[x].setBackgroundColor(Color.BLUE);
                botLevel1();
                playerShots[x] = -1;
                return;
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
                                    Toast.makeText(this, "Dein Gegner hat gewonnen.", Toast.LENGTH_SHORT).show();
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
                                arrTextViews[i + 100].setBackgroundColor(Color.RED);
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
                                    Toast.makeText(this, "Dein Gegner hat gewonnen.", Toast.LENGTH_SHORT).show();
                                    textViewArrow.setText("lost");
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
                    arrTextViews[i + 100].setBackgroundColor(Color.BLUE);
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
        //TODO: Wenn Schiff zerstört, auch Felder um das Schiff nicht mehr beschießen
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
                                arrTextViews[i + 100].setBackgroundColor(Color.RED);
                                enemyShots[i] = -1;
                                anArrTextViewsUsed[k] = -1;
                                if (checkIfWon(arrTextViewsUsed, enemyShots)) {
                                    textViewArrow.setText("verlohren");
                                    Toast.makeText(this, "Dein Gegner hat gewonnen.", Toast.LENGTH_SHORT).show();
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
}
