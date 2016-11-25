package com.example.tom.battleships;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;


public class GameLayoutActivity extends AppCompatActivity implements View.OnClickListener {

    TextView arrTextViews[] = new TextView[100];
    TextView arrShipCounters[] = new TextView[4];
    ImageView arrShips[] = new ImageView[10];

    int textViewSize, marginShips;
    int lastShipTouched;

    int arrShipPlaced[]= {1, 2, 3, 4};                                                              //4 Shiffstypen: Wert in Array = Schiffe noch gesetzt
    int arrShipOrigins[][] = new int[10][3];                                                        //10 Schiffe; X, Y, bewegt?
    boolean moving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_layout);

        textViewSize = (int)dpToPx(30);
        marginShips = (int)dpToPx(30);
        createViews();
        createShips();

        findViewById(R.id.buttonTest).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==findViewById(R.id.buttonTest).getId()) {
            btnRotate(1);
        }
        for(int i = 0; i < 100; i++) {
            if (arrTextViews[i].getId() == v.getId()) {
                arrTextViews[i].setBackgroundColor(Color.RED);
                Toast.makeText(this, Float.toString(arrTextViews[i].getWidth()), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    private void btnRotate(int shipId) {


    }

    private void createViews() {
        GridLayout gl = (GridLayout) findViewById(R.id.gridLayoutPlayer);
        gl.setColumnCount(10);
        gl.setRowCount(10);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int i = col + row * 10;
                LayoutParams lp = new LayoutParams(textViewSize, textViewSize);

                arrTextViews[i] = new TextView(this);
                arrTextViews[i].setId(i);
                arrTextViews[i].setOnClickListener(this);
                arrTextViews[i].setBackgroundColor(Color.GRAY);
                gl.addView(arrTextViews[i],lp);
            }
        }
    }

    private void createShips() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_main);

        for (int i = 0; i < 10; i++) {
            arrShips[i] = new ImageView(this);
            arrShips[i].setId(100+i);
            switch (i) {
                case 0:
                    setShipParams(5, i, rl);
                    createShipCounter(0, i, rl);
                    break;
                case 1:
                    setShipParams(4, i, rl);
                    break;
                case 2:
                    setShipParams(4, i, rl);
                    createShipCounter(1, i, rl);
                    break;
                case 3:
                    setShipParams(3, i, rl);
                    break;
                case 4:
                    setShipParams(3, i, rl);
                    break;
                case 5:
                    setShipParams(3, i, rl);
                    createShipCounter(2, i, rl);
                    break;
                case 6:
                case 7:
                case 8:
                    setShipParams(2, i, rl);
                    break;
                case 9:
                    setShipParams(2, i, rl);
                    createShipCounter(3, i, rl);
                    break;
            }
            arrShips[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent mv) {
                    int shipId = v.getId()-100;
                    if(arrShipOrigins[0][0]== 0) {getShipOrigin();}
                    switch (mv.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            moving = true;
                            setShipTransparency(true, shipId);
                            Log.d("X", Float.toString(arrShips[1].getWidth()));
                            Log.d("Y", Float.toString(arrShips[1].getHeight()));
                            break;
                        case MotionEvent.ACTION_UP:
                            moving = false;
                            setShipTransparency(false, shipId);
                            setShipLocation(shipId);
                            setShipCounter(v.getId());
                            lastShipTouched = shipId;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (moving) {
                                float x = mv.getRawX() - v.getWidth()/2;
                                float y = mv.getRawY() - v.getHeight();
                                v.setX(x);
                                v.setY(y);
                                setTextViewColorMove(shipId);
                            }
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void createShipCounter( int id, int ship,  RelativeLayout rl) {                          //setMargins: http://stackoverflow.com/questions/4472429/change-the-right-margin-of-a-view-programmatically
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);

        arrShipCounters[id] = new TextView(this);
        arrShipCounters[id].setId(120+id);
        arrShipCounters[id].setText(String.format(Locale.getDefault(),"%1$d×",id+1));
        arrShipCounters[id].setTextSize(41);

        lp.addRule(RelativeLayout.LEFT_OF, arrShips[ship].getId());
        lp.setMargins(0,marginShips + (marginShips*2*id),marginShips,0);
        rl.addView(arrShipCounters[id],lp);
    }

    private void setShipParams(int size, int ship, RelativeLayout rl) {                              //GridLayout http://stackoverflow.com/questions/25395773/gridlayout-align-children-within-column
        LayoutParams lp = new LayoutParams(textViewSize*size,textViewSize);
        switch (size) {
            case 5: //1x
                lp.addRule(RelativeLayout.ALIGN_PARENT_END);
                lp.setMargins(0,(int)(marginShips*1.5f),0,0);
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.giantship, null));
                break;
            case 4: //2x
                lp.addRule(RelativeLayout.ALIGN_START,arrShips[0].getId());
                lp.addRule(RelativeLayout.BELOW,arrShips[0].getId());
                lp.setMargins(0,marginShips,0,0);
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.bigship, null));
                break;
            case 3: //3x
                lp.addRule(RelativeLayout.BELOW,arrShips[2].getId());
                lp.addRule(RelativeLayout.ALIGN_START,arrShips[2].getId());
                lp.setMargins(0,marginShips,0,0);
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.mediumship, null));
                break;
            case 2: //4x
                lp.addRule(RelativeLayout.BELOW, arrShips[5].getId());
                lp.addRule(RelativeLayout.ALIGN_START,arrShips[5].getId());
                lp.setMargins(0,marginShips,0,0);
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.smallship, null));
                break;
        }
        rl.addView(arrShips[ship],lp);
    }

    private void getShipOrigin() {
        for (int ship = 0; ship < 10; ship++) {
            //Toast.makeText(this, Integer.toString((int)arrShips[ship].getX()),Toast.LENGTH_SHORT).show();
            arrShipOrigins[ship][0] = (int) arrShips[ship].getX();
            arrShipOrigins[ship][1] = (int) arrShips[ship].getY();
        }
    }

    private boolean shipWasMoved(int ship) {
        return !(arrShipOrigins[ship][0] == arrShips[ship].getX() & arrShipOrigins[ship][1] == arrShips[ship].getY());
    }

    private float dpToPx(int dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    private void incShipCounter(int i, int shipId) {
        if (!shipWasMoved(i)) {
            arrShipPlaced[shipId] += 1;
            arrShipOrigins[i][2] = 0;
        }
        arrShipCounters[shipId].setText(String.format(Locale.getDefault(), "%1$d×", arrShipPlaced[shipId]));
    }

    private void decShipCounter(int i, int shipId) {
        if(shipWasMoved(i)) {
            arrShipPlaced[shipId] -= 1;
            arrShipOrigins[i][2] = 1;
        }
        arrShipCounters[shipId].setText(String.format(Locale.getDefault(), "%1$d×", arrShipPlaced[shipId]));
    }

    private void setShipCounter(int shipId) {
        for (int i = 0; i < 10; i++) {
            if (shipId == arrShips[i].getId()) {
                switch (i) {
                    case 0:
                        if(arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 0);
                        } else {
                            incShipCounter(i, 0);
                        }
                        break;
                    case 1:
                    case 2:
                        if(arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 1);
                        } else {
                            incShipCounter(i, 1);
                        }
                        break;
                    case 3:
                    case 4:
                    case 5:
                        if(arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 2);
                        } else {
                            incShipCounter(i, 2);
                        }
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        if(arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 3);
                        } else {
                            incShipCounter(i, 3);
                        }
                        break;
                }
            }
        }
    }

    private int[] getShipLocation(int ship) {
        int arrLocation[] = new int[3];                                                             //0 = Startpunkt, 1 = Länge, 2 = Ausrichtung
        int shipAlign;                                                                              //1 = horizontal; 2 = vertikal
        int width = arrShips[ship].getWidth()/textViewSize;
        int height = arrShips[ship].getHeight()/textViewSize;
        int shipX = (int)arrShips[ship].getX();
        int shipY = (int)arrShips[ship].getY();

        if (width > height) {shipAlign = 1;} else {shipAlign = 2;}

        for (int i = 0; i < 100; i++) {
            int textViewX = (int)arrTextViews[i].getX();
            int textViewY = (int)arrTextViews[i].getY();
            if (shipX > textViewX & shipX < textViewX+textViewSize &
                    shipY > textViewY & shipY < textViewY+textViewSize) {
                arrLocation[0] = arrTextViews[i].getId();
                arrLocation[2] = shipAlign;
                switch(shipAlign) {
                    case 1:
                        arrLocation[1] = width;
                        break;
                    case 2:
                        arrLocation[1] = height;
                        break;
                }
                break;
            } else {
                arrLocation[0] = -1;
            }
        }
        return arrLocation;
    }

    private void setShipLocation(int ship) {
        int matchingTextViews[] =  getShipLocation(ship);

        if (matchingTextViews[0] > -1 & !shipOutsideLayout(ship) & shipCheckArea(ship)) {
            TextView firstTextView = (TextView) findViewById(matchingTextViews[0]);
            arrShips[ship].setX(firstTextView.getX()+textViewSize/2+3);                             //+3 vielleicht weg wegen Schiffbild-Ungenauigkeit
            arrShips[ship].setY(firstTextView.getY()+textViewSize/2+3);                             //+3 vielleicht weg wegen Schiffbild-Ungenauigkeit
        } else {
            arrShips[ship].setX(arrShipOrigins[ship][0]);
            arrShips[ship].setY(arrShipOrigins[ship][1]);
        }

    }

    private void setShipTransparency(boolean onOff, int ship) {
        if(onOff) {
            arrShips[ship].setAlpha(0.5f);                                                              //http://stackoverflow.com/questions/5078041/how-can-i-make-an-image-transparent-in-android
        } else {
            arrShips[ship].setAlpha(1.0f);
        }
    }

    private boolean shipOutsideLayout(int ship) {
        int textViewStart[] = getShipLocation(ship);

        switch(textViewStart[2]) {
            case 1:
                int shipEndX = (int)arrTextViews[textViewStart[0]].getX() + textViewSize*textViewStart[1];
                int shipStartX = (int)arrTextViews[textViewStart[0]].getX();
                int layoutEndX = (int)arrTextViews[9].getX() + textViewSize;
                int layoutStartX = (int)arrTextViews[0].getX();

                return (shipEndX > layoutEndX | shipStartX < layoutStartX);
            case 2:
                int shipEndY = (int)arrTextViews[textViewStart[0]].getY() + textViewSize*textViewStart[1];
                int shipStartY = (int)arrTextViews[textViewStart[0]].getX();
                int layoutEndY = (int)arrTextViews[90].getY() + textViewSize;
                int layoutStartY = (int)arrTextViews[0].getX();
                return (shipEndY > layoutEndY | shipStartY < layoutStartY);
            default:
                return true;
        }
    }

    private boolean shipCheckArea(int shipId) {
        int startX, startY, endX, endY, placedStartY = 0, placedEndX = 0, placedStartX = 0, placedEndY = 0;
        boolean ok = false;
        boolean okX, okY;
        int placedShip[] = getShipLocation(shipId);

        switch(placedShip[2]) {
            case 1:
                placedStartX = (int)arrTextViews[placedShip[0]].getX();
                placedEndX = (int)arrTextViews[placedShip[0]].getX() + textViewSize*placedShip[1];
                placedStartY = (int)arrTextViews[placedShip[0]].getY();
                placedEndY = (int)arrTextViews[placedShip[0]].getY() + textViewSize;
                break;
            case 2:
                placedStartX = (int)arrTextViews[placedShip[0]].getX();
                placedEndX = (int)arrTextViews[placedShip[0]].getX() + textViewSize;
                placedStartY = (int)arrTextViews[placedShip[0]].getY();
                placedEndY = (int)arrTextViews[placedShip[0]].getY()  + textViewSize*placedShip[1];
                break;
        }
        for(int i = 0; i < 10; i++) {
            int iShip[] = getShipLocation(i);
            if (i != shipId & iShip[0] > -1) {
                switch (iShip[2]) {
                    case 1:
                        startX = (int) arrShips[i].getX() - textViewSize -5;
                        endX = (int) arrShips[i].getX() + (iShip[1] + 1) * textViewSize -5;
                        startY = (int) arrShips[i].getY() - textViewSize -5;
                        endY = (int) arrShips[i].getY() + textViewSize -5;

                        okX = (placedStartX > startX & placedStartX < endX | placedEndX > startX & placedEndX < endX);
                        okY = (placedStartY > startY & placedStartY < endY | placedEndY > startY & placedEndY < endY);
                        ok = !(okX & okY);                                                          //NAND
                        break;
                    case 2:
                        startX = (int) arrShips[i].getX() - textViewSize;
                        endX = (int) arrShips[i].getX() + textViewSize;
                        startY = (int) arrShips[i].getY() - textViewSize;
                        endY = (int) arrShips[i].getY() + (iShip[1] + 1) * textViewSize;

                        okX = (placedStartX > startX & placedStartX < endX | placedEndX > startX & placedEndX < endX);
                        okY = (placedStartY > startY & placedStartY < endY | placedEndY > startY & placedEndY < endY);
                        ok = !(okX & okY);
                        break;
                }
            } else {
                ok = true;
            }
            if (!ok) {return false;}
        }
        return true;
    }                                                 //true = alles ok; false = kann nicht gesetzt werden

    private void setTextViewColorMove(int shipId) {
        if (!shipOutsideLayout(shipId)) {
            int arrShipCurrent[] = getShipLocation(shipId);

            for(int j = 0; j < 100; j++) {
                arrTextViews[j].setBackgroundColor(Color.GRAY);
            }

            if (shipCheckArea(shipId)) {
                for (int i = 0; i < arrShipCurrent[1]; i++) {
                    arrTextViews[arrShipCurrent[0] + i].setBackgroundColor(Color.GREEN);
                }
            } else {
                for (int i = 0; i < arrShipCurrent[1]; i++) {
                    arrTextViews[arrShipCurrent[0] + i].setBackgroundColor(Color.RED);
                }
            }
        }
    }
}


















/*                Schiffanordnung:
    private void setShipParams(int size, int ship, RelativeLayout rl) {
        LayoutParams lp = new LayoutParams(textViewSize*size,textViewSize);
        switch (size) {
            case 5: //1x
                lp.addRule(RelativeLayout.ALIGN_PARENT_END);
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.giantship, null));
                break;
            case 4: //2x
                lp.addRule(RelativeLayout.ALIGN_PARENT_END);
                lp.addRule(RelativeLayout.BELOW,arrShips[ship-1].getId());
                lp.setMargins(0,marginShips,0,0);
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.bigship, null));
                break;
            case 3: //3x
                lp.addRule(RelativeLayout.BELOW,arrShips[2].getId());
                if (ship == 3) {
                    lp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    lp.setMargins(0,marginShips,0,0);
                } else {
                    lp.addRule(RelativeLayout.LEFT_OF, arrShips[ship-1].getId());
                    lp.setMargins(0,marginShips,marginShips,0);
                }
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.mediumship, null));
                break;
            case 2: //4x
                if (ship == 6 | ship == 8) {
                    lp.addRule(RelativeLayout.BELOW, arrShips[ship-2].getId());
                    lp.addRule(RelativeLayout.ALIGN_PARENT_END);
                    lp.setMargins(0,marginShips,0,0);
                } else {
                    lp.addRule(RelativeLayout.BELOW, arrShips[ship-2].getId());
                    lp.addRule(RelativeLayout.LEFT_OF, arrShips[ship-1].getId());
                    lp.setMargins(0,marginShips,marginShips,0);

                }
                arrShips[ship].setImageDrawable(getResources().getDrawable(R.drawable.smallship, null));
                break;
        }
        rl.addView(arrShips[ship],lp);
    }
*/

