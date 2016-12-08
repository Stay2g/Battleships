package com.example.tom.battleships;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.util.Timer;


public class GameLayoutActivity extends Activity implements View.OnClickListener {

    TextView arrTextViews[] = new TextView[100];
    TextView arrShipCounters[] = new TextView[4];
    TextView arrPlaceholder[] = new TextView[4];
    ImageView arrShips[] = new ImageView[10];

    Button btnStart;

    RotateAnimation rotateAnim;

    int textViewSize, marginShips;
    int lastShipTouched[] = new int[3];                                                             //Schiff-ID, Bild-ID, Länge

    int arrShipPlaced[] = {1, 2, 3, 4};                                                             //4 Shiffstypen: Wert in Array = Schiffe noch gesetzt
    int arrShipOrigins[][] = new int[10][3];                                                        //10 Schiffe; X, Y, bewegt?
    int arrTextViewsUsed[][] = new int[10][5];
    int arrTextViewsLocked[] = new int[270];
    boolean moving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_layout);

        textViewSize = (int) dpToPx(30);
        marginShips = (int) dpToPx(30);
        createViews();
        createShips();
        initTextViewUsed();
        initTextViewLocked();
        findViewById(R.id.buttonStart).setOnClickListener(this);
        findViewById(R.id.buttonTest).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == findViewById(R.id.buttonTest).getId()) {
            setValidShipLocation();
        }
        if (v.getId() == findViewById(R.id.buttonStart).getId()) {
            Toast.makeText(this, "Start!", Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < 100; i++) {
            if (arrTextViews[i].getId() == v.getId()) {
                arrTextViews[i].setBackgroundColor(Color.RED);
                break;
            }
        }
    }
    //------------------------------------------------------------------------------//
    //----------------------------------- Creator ----------------------------------//
    //------------------------------------------------------------------------------//

    public void createViews() {
        GridLayout gl = (GridLayout) findViewById(R.id.gridLayoutPlayer);
        gl.setColumnCount(10);
        gl.setRowCount(10);

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int i = col + row * 10;
                LayoutParams lp = new LayoutParams(textViewSize, textViewSize);

                arrTextViews[i] = new TextView(this);
                arrTextViews[i].setId(i);
                arrTextViews[i].setBackgroundResource(R.drawable.textview_border);
                arrTextViews[i].setOnClickListener(this);
                gl.addView(arrTextViews[i], lp);
            }
        }
    }

    private void createShips() {
        initPlaceholder();
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_main);

        for (int i = 0; i < arrShips.length; i++) {
            arrShips[i] = new ImageView(this);
            arrShips[i].setId(100 + i);
            switch (i) {
                case 0:
                    setShipParams(5, i, rl);
                    createShipCounter(0, rl);
                    break;
                case 1:
                    setShipParams(4, i, rl);
                    break;
                case 2:
                    setShipParams(4, i, rl);
                    createShipCounter(1, rl);
                    break;
                case 3:
                    setShipParams(3, i, rl);
                    break;
                case 4:
                    setShipParams(3, i, rl);
                    break;
                case 5:
                    setShipParams(3, i, rl);
                    createShipCounter(2, rl);
                    break;
                case 6:
                case 7:
                case 8:
                    setShipParams(2, i, rl);
                    break;
                case 9:
                    setShipParams(2, i, rl);
                    createShipCounter(3, rl);
                    break;
            }
            arrShips[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent mv) {
                    int shipId = v.getId() - 100;
                    if (arrShipOrigins[0][0] == 0) {
                        getShipOrigin();
                    }
                    switch (mv.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            moving = true;
                            setShipTransparency(true, shipId);
                            break;
                        case MotionEvent.ACTION_UP:
                            moving = false;
                            setShipTransparency(false, shipId);
                            setShipLocation(shipId);
                            setShipCounter(v.getId());
                            getShipLastTouched(shipId);
                            setTextViewColorMove(shipId, true);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (moving) {
                                float x = mv.getRawX() - v.getWidth() / 2;
                                float y = mv.getRawY() - v.getHeight();
                                v.setX(x);
                                v.setY(y);
                                setTextViewColorMove(shipId, false);
                            }
                            break;
                    }
                    return true;
                }
            });
        }
    }

    private void createShipCounter(int id, RelativeLayout rl) {                                   //setMargins: http://stackoverflow.com/questions/4472429/change-the-right-margin-of-a-view-programmatically
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        arrShipCounters[id] = new TextView(this);
        arrShipCounters[id].setId(120 + id);
        arrShipCounters[id].setText(String.format(Locale.getDefault(), "%1$d×", id + 1));
        arrShipCounters[id].setTextSize(41);

        lp.addRule(RelativeLayout.LEFT_OF, arrPlaceholder[id].getId());
        //lp.setMargins(0,marginShips + (marginShips*2*id),marginShips,0);
        lp.setMargins(0, (int) (marginShips * 0.5) + (marginShips * 2 * id), marginShips, 0);
        rl.addView(arrShipCounters[id], lp);
    }

    //------------------------------------------------------------------------------//
    //------------------------------------ Getter ----------------------------------//
    //------------------------------------------------------------------------------//

    private void getShipOrigin() {
        for (int ship = 0; ship < 10; ship++) {
            //Toast.makeText(this, Integer.toString((int)arrShips[ship].getX()),Toast.LENGTH_SHORT).show();
            arrShipOrigins[ship][0] = (int) arrShips[ship].getX();
            arrShipOrigins[ship][1] = (int) arrShips[ship].getY();
        }
    }

    private int[] getShipLocation(int ship) {
        int arrLocation[] = new int[3];                                                             //0 = Startpunkt, 1 = Länge, 2 = Ausrichtung
        int shipAlign;                                                                              //1 = horizontal; 2 = vertikal
        int width = arrShips[ship].getWidth() / textViewSize;
        int height = arrShips[ship].getHeight() / textViewSize;
        int shipX = (int) arrShips[ship].getX();
        int shipY = (int) arrShips[ship].getY();

        if (width > height) {
            shipAlign = 1;
        } else {
            shipAlign = 2;
        }

        for (int i = 0; i < 100; i++) {
            int textViewX = (int) arrTextViews[i].getX();
            int textViewY = (int) arrTextViews[i].getY();
            if (shipX > textViewX & shipX < textViewX + textViewSize &
                    shipY > textViewY & shipY < textViewY + textViewSize) {
                arrLocation[0] = arrTextViews[i].getId();
                arrLocation[2] = shipAlign;
                switch (shipAlign) {
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

    private void getShipLastTouched(int shipId) {                                                   //Schiff-ID, Bild-ID, Länge
        int width = arrShips[shipId].getWidth() / textViewSize;
        int height = arrShips[shipId].getHeight() / textViewSize;

        if (width > height) {
            lastShipTouched[2] = width;
        } else {
            lastShipTouched[2] = height;
        }
        lastShipTouched[0] = shipId;
        switch (shipId) {
            case 0:
                lastShipTouched[1] = R.drawable.giantship;
                break;
            case 1:
            case 2:
                lastShipTouched[1] = R.drawable.bigship;
                break;
            case 3:
            case 4:
            case 5:
                lastShipTouched[1] = R.drawable.mediumship;
                break;
            case 6:
            case 7:
            case 8:
            case 9:
                lastShipTouched[1] = R.drawable.smallship;
                break;
        }
    }

    //------------------------------------------------------------------------------//
    //------------------------------------ Setter ----------------------------------//
    //------------------------------------------------------------------------------//

    private void setShipParams(int size, int ship, RelativeLayout rl) {                              //GridLayout http://stackoverflow.com/questions/25395773/gridlayout-align-children-within-column
        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switch (size) {
            case 5: //1x
                lp.addRule(RelativeLayout.ALIGN_START, arrPlaceholder[0].getId());
                lp.addRule(RelativeLayout.BELOW, arrPlaceholder[0].getId());
                arrShips[ship].setImageDrawable(new BitmapDrawable(getResources(), scaleShipImage(size, R.drawable.giantship)));
                break;
            case 4: //2x
                lp.addRule(RelativeLayout.ALIGN_START, arrPlaceholder[1].getId());
                lp.addRule(RelativeLayout.BELOW, arrPlaceholder[1].getId());
                arrShips[ship].setImageDrawable(new BitmapDrawable(getResources(), scaleShipImage(size, R.drawable.bigship)));
                break;
            case 3: //3x
                lp.addRule(RelativeLayout.BELOW, arrPlaceholder[2].getId());
                lp.addRule(RelativeLayout.ALIGN_START, arrPlaceholder[2].getId());
                arrShips[ship].setImageDrawable(new BitmapDrawable(getResources(), scaleShipImage(size, R.drawable.mediumship)));
                break;
            case 2: //4x
                lp.addRule(RelativeLayout.BELOW, arrPlaceholder[3].getId());
                lp.addRule(RelativeLayout.ALIGN_START, arrPlaceholder[3].getId());
                arrShips[ship].setImageDrawable(new BitmapDrawable(getResources(), scaleShipImage(size, R.drawable.smallship)));
                break;
        }
        rl.addView(arrShips[ship], lp);
    }

    private void setShipCounter(int shipId) {
        for (int i = 0; i < arrShips.length; i++) {
            if (shipId == arrShips[i].getId()) {
                switch (i) {
                    case 0:
                        if (arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 0);
                        } else {
                            incShipCounter(i, 0);
                        }
                        break;
                    case 1:
                    case 2:
                        if (arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 1);
                        } else {
                            incShipCounter(i, 1);
                        }
                        break;
                    case 3:
                    case 4:
                    case 5:
                        if (arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 2);
                        } else {
                            incShipCounter(i, 2);
                        }
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        if (arrShipOrigins[i][2] == 0) {
                            decShipCounter(i, 3);
                        } else {
                            incShipCounter(i, 3);
                        }
                        break;
                }
            }
        }
    }

    private void setShipLocation(int shipId) {
        int matchingTextViews[] = getShipLocation(shipId);
        if(!isFirstShip()) {
            if (matchingTextViews[0] > -1 & !shipOutsideLayout(shipId) & shipCheckArea(shipId)) {
                TextView firstTextView = (TextView) findViewById(matchingTextViews[0]);
                arrShips[shipId].setX(firstTextView.getX() + getResources().getDimension(R.dimen.activity_horizontal_margin));
                arrShips[shipId].setY(firstTextView.getY() + getResources().getDimension(R.dimen.activity_vertical_margin));
                setArrTextViewsUsed(shipId);
            } else {
                arrShips[shipId].animate().x(arrShipOrigins[shipId][0]).y(arrShipOrigins[shipId][1]);
                arrShips[shipId].setX(arrShipOrigins[shipId][0]);
                arrShips[shipId].setY(arrShipOrigins[shipId][1]);
                setArrTextViewsUsed(shipId);

            }
        } else {
            if (matchingTextViews[0] > -1 & !shipOutsideLayout(shipId)) {
                TextView firstTextView = (TextView) findViewById(matchingTextViews[0]);
                arrShips[shipId].setX(firstTextView.getX() + getResources().getDimension(R.dimen.activity_horizontal_margin));
                arrShips[shipId].setY(firstTextView.getY() + getResources().getDimension(R.dimen.activity_vertical_margin));
                setArrTextViewsUsed(shipId);
            } else {
                arrShips[shipId].animate().x(arrShipOrigins[shipId][0]).y(arrShipOrigins[shipId][1]);
                arrShips[shipId].setX(arrShipOrigins[shipId][0]);
                arrShips[shipId].setY(arrShipOrigins[shipId][1]);
                setArrTextViewsUsed(shipId);
            }
        }
    }

    private void setShipTransparency(boolean onOff, int ship) {
        if (onOff) {
            arrShips[ship].setAlpha(0.5f);                                                              //http://stackoverflow.com/questions/5078041/how-can-i-make-an-image-transparent-in-android
        } else {
            arrShips[ship].setAlpha(1.0f);
        }
    }

    private void setTextViewColorMove(int shipId, boolean reset) {
        if (!reset) {
            int ext = 1;
            if (!shipOutsideLayout(shipId)) {
                int arrShipCurrent[] = getShipLocation(shipId);

                for (int j = 0; j < 100; j++) {
                    arrTextViews[j].setBackgroundResource(R.drawable.textview_border);
                }
                if (arrShips[shipId].getWidth() < arrShips[shipId].getHeight()) {
                    ext = 10;
                }
                if (shipCheckArea(shipId)) {
                    for (int i = 0; i < arrShipCurrent[1]; i++) {
                        arrTextViews[arrShipCurrent[0] + i * ext].setBackgroundColor(Color.GREEN);
                        arrTextViews[arrShipCurrent[0] + i * ext].setAlpha(0.5f);
                    }
                } else {
                    for (int i = 0; i < arrShipCurrent[1]; i++) {
                        arrTextViews[arrShipCurrent[0] + i * ext].setBackgroundColor(Color.RED);
                        arrTextViews[arrShipCurrent[0] + i * ext].setAlpha(0.5f);
                    }
                }
            }
        } else {
            for (int j = 0; j < 100; j++) {
                arrTextViews[j].setBackgroundResource(R.drawable.textview_border);
                arrTextViews[j].setAlpha(1.0f);
            }
        }
    }

    private void setArrTextViewsUsed(int shipId) {
        int currentShip[] = getShipLocation(shipId);
        int textViewId = currentShip[0];
        if(shipOutsideLayout(shipId) & shipCheckArea(shipId)) {
            for (int i = 0; i < 5; i++) {
                arrTextViewsUsed[shipId][i] = -1;
            }
        } else {
            for (int i = 0; i < currentShip[1]; i++) {
                switch (currentShip[2]) {
                    case 1:
                        arrTextViewsUsed[shipId][i] = textViewId + i;
                        break;
                    case 2:
                        arrTextViewsUsed[shipId][i] = textViewId + i * 10;
                        break;
                }
            }
        }
    }

    private void setValidShipLocation() {
        ImageView ship = arrShips[lastShipTouched[0]];
        int shipId = lastShipTouched[0];
        int align;
        int l = lastShipTouched[2];
        int textViewId = getShipLocation(shipId)[0];
        int textViewStart;
        int xEnd = 9;
        int yEnd = 9;
        int step = 0;
        boolean exit = false;
        boolean top = false;
        boolean bottom = false;


        //Schiff-Image drehen (nur innerhalb des Spielfeldes
        if (((lastShipTouched[1] != 0) & !shipOutsideLayout(shipId))) {
            Bitmap shipImage = scaleShipImage(lastShipTouched[2], lastShipTouched[1]);
            Matrix matrix = new Matrix(); //erstelle Matrix, das anschließend dem
            if (ship.getWidth() > ship.getHeight()) {
                matrix.setRotate(90f);
                align = 2;                //horizontal
            } else {
                matrix.setRotate(180f);
                align = 1;                //vertikal
            }
            Bitmap rotated = Bitmap.createBitmap(shipImage, 0, 0, shipImage.getWidth(), shipImage.getHeight(), matrix, false);

            //Schiff um enstrechende felder nach innen bewegen, wenn gedrehtes Schiff außerhalb des Spielfeldes liegt
            if(arrTextViews[arrTextViewsUsed[shipId][0]].getX() + rotated.getWidth() > arrTextViews[9].getX() + textViewSize) {
                int temp = (int) (((ship.getX() + rotated.getWidth()) - (arrTextViews[9].getX() + textViewSize)) / textViewSize);
                textViewId -= temp;
                xEnd -= temp;
            }
            if(arrTextViews[arrTextViewsUsed[shipId][0]].getY() + rotated.getHeight() > arrTextViews[90].getY() + textViewSize) {
                int temp = (int) (((ship.getY() + rotated.getHeight()) - (arrTextViews[90].getY() + textViewSize)) / textViewSize);
                textViewId -= temp*10;
                yEnd -= temp;
            }

            textViewStart = textViewId; //Startwert auf den die Id nach jedem step zurückgesetzt wird

            do {
                switch (step) {
                    case 0:
                        textViewId = textViewStart;
                        for (int i = textViewId % 10; i <= xEnd; i++) {       //X-Achse vom Punkt bis zum Endspalte
                            setNewLocation(align, textViewId);
                            if (checkNewLocation(align, textViewId)) {
                                step = 4;
                                break;
                            } else {
                                textViewId++;
                            }
                        }
                        if(step == 4) {break;}
                        step = 1;
                        break;
                    case 1:
                        textViewId = textViewStart;
                        for (int i = textViewId % 10; i >= 0; i--) {          //X-Achse vom Punkt bis zum Anfangsspalte
                            setNewLocation(align, textViewId);
                            if (checkNewLocation(align, textViewId)) {
                                step = 4;
                                break;
                            } else {
                                textViewId--;
                            }
                        }
                        if(step == 4) {break;}
                        step = 2;
                        break;
                    case 2:
                        textViewId = textViewStart;
                        for (int i = textViewId; i < yEnd; i += 10) {       //Y-Achse vom Punkt bis zum Endzeile
                            setNewLocation(align, textViewId);
                            if (checkNewLocation(align, textViewId)) {
                                step = 4;
                                break;
                            } else {
                                textViewId += 10;
                            }
                        }
                        if(step == 4) {break;}
                        step = 3;
                        break;
                    case 3:
                        textViewId = textViewStart;
                        for (int i = textViewId; i >= 0; i -= 10) {         //Y-Achse vom Punkt bis zur Anfangszeile
                            setNewLocation(align, textViewId);
                            if (checkNewLocation(align, textViewId)) {
                                step = 4;
                                break;
                            } else {
                                textViewId -= 10;
                            }
                        }
                        if(step == 4) {break;}
                        step = 0;
                        if (!bottom) {
                            bottom = (textViewStart > 89);}       //wenn die unterste Zeile erreicht ist
                        if (!top) {
                            top = (textViewStart < 11);}             //wenn die oberste Zeile erreicht ist
                        if (!bottom & top) {
                            textViewStart += 10;}
                        if (!top & bottom) {
                            textViewStart -= 10;}
                        if(!bottom & !top) {
                            textViewStart += 10;}
                        if(bottom & top) {
                            step = 5;}                        //wenn nichts frei ist, dann exit
                        break;
                    case 4:
                        ship.setImageBitmap(rotated);
                        shipBlink(shipId, 1);
                        exit = true;
                        break;
                    case 5:
                        for (int i = 0; i < l; i++) {
                            switch (align) {
                                case 1:
                                    arrTextViewsUsed[shipId][i] = getShipLocation(shipId)[0] + i * 10;
                                    break;
                                case 2:
                                    arrTextViewsUsed[shipId][i] = getShipLocation(shipId)[0] + i;
                                    break;
                            }
                        }
                        shipBlink(shipId, 2);
                        exit = true;
                        break;
                }
            } while (!exit);

            int x = (int) (arrTextViews[arrTextViewsUsed[shipId][0]].getX() + getResources().getDimension(R.dimen.activity_horizontal_margin));
            int y = (int) (arrTextViews[arrTextViewsUsed[shipId][0]].getY() + getResources().getDimension(R.dimen.activity_vertical_margin));

            ship.animate().x(x).y(y).setDuration(200);

            arrShips[shipId].setX(x);
            arrShips[shipId].setY(y);
        }
    }

    //------------------------------------------------------------------------------//
    //-------------------------------- Outsourcing ---------------------------------//
    //------------------------------------------------------------------------------//

    private void incShipCounter(int i, int shipId) {
        if (!shipWasMoved(i)) {
            arrShipPlaced[shipId] += 1;
            arrShipOrigins[i][2] = 0;
        }
        arrShipCounters[shipId].setText(String.format(Locale.getDefault(), "%1$d×", arrShipPlaced[shipId]));
    }

    private void decShipCounter(int i, int shipId) {
        if (shipWasMoved(i)) {
            arrShipPlaced[shipId] -= 1;
            arrShipOrigins[i][2] = 1;
        }
        arrShipCounters[shipId].setText(String.format(Locale.getDefault(), "%1$d×", arrShipPlaced[shipId]));
    }

    private Bitmap scaleShipImage(int size, int shipImage) {
        Bitmap ship = BitmapFactory.decodeResource(getResources(), shipImage);
        return Bitmap.createScaledBitmap(ship, textViewSize * size, textViewSize, false);
    }

    private boolean shipWasMoved(int ship) {
        return !(arrShipOrigins[ship][0] == arrShips[ship].getX() & arrShipOrigins[ship][1] == arrShips[ship].getY());
    }

    private boolean shipOutsideLayout(int ship) {
        int textViewStart[] = getShipLocation(ship);

        switch (textViewStart[2]) {
            case 1:
                int shipEndX = (int) arrTextViews[textViewStart[0]].getX() + textViewSize * textViewStart[1];
                int shipStartX = (int) arrTextViews[textViewStart[0]].getX();
                int layoutEndX = (int) arrTextViews[9].getX() + textViewSize;
                int layoutStartX = (int) arrTextViews[0].getX();

                return (shipEndX > layoutEndX | shipStartX < layoutStartX);
            case 2:
                int shipEndY = (int) arrTextViews[textViewStart[0]].getY() + textViewSize * textViewStart[1];
                int shipStartY = (int) arrTextViews[textViewStart[0]].getX();
                int layoutEndY = (int) arrTextViews[90].getY() + textViewSize;
                int layoutStartY = (int) arrTextViews[0].getX();
                return (shipEndY > layoutEndY | shipStartY < layoutStartY);
            default:
                return true;
        }
    }

    private boolean isFirstShip() {
        for (int i = 0; i < arrShips.length; i++) {
            if (arrTextViewsUsed[i][0] != -1) {
                return false;
            }
        }
        return true;
    }

    private boolean shipCheckArea(int shipId) {
        int currentShip[] = getShipLocation(shipId);
        for(int i = 0; i < arrShips.length; i++) {
            if(i != shipId) {
               for (int j = 0; j < 5; j++) {
                   if(arrTextViewsUsed[i][j] == -1) {                                          //wenn Schiff schon gesetzt wurde, dann...
                       break;
                   } else {
                       for (int k = 0; k < arrTextViewsLocked.length; k++) {
                           if(arrTextViewsLocked[k] == -1) {                                        //suche nach unbenutztem Platz...
                               arrTextViewsLocked[k] = arrTextViewsUsed[i][j];
                               switch(isTextViewAtBorder(arrTextViewsUsed[i][j])) {
                                   case 0:
                                       arrTextViewsLocked[k + 1] = checkTextViewExist(arrTextViewsUsed[i][j] - 1);
                                       arrTextViewsLocked[k + 2] = checkTextViewExist(arrTextViewsUsed[i][j] + 1);
                                       arrTextViewsLocked[k + 3] = checkTextViewExist(arrTextViewsUsed[i][j] - 9);
                                       arrTextViewsLocked[k + 4] = checkTextViewExist(arrTextViewsUsed[i][j] + 9);
                                       arrTextViewsLocked[k + 5] = checkTextViewExist(arrTextViewsUsed[i][j] + 10);
                                       arrTextViewsLocked[k + 6] = checkTextViewExist(arrTextViewsUsed[i][j] - 10);
                                       arrTextViewsLocked[k + 7] = checkTextViewExist(arrTextViewsUsed[i][j] + 11);
                                       arrTextViewsLocked[k + 8] = checkTextViewExist(arrTextViewsUsed[i][j] - 11);
                                       break;
                                   case 1:
                                       arrTextViewsLocked[k + 1] = checkTextViewExist(arrTextViewsUsed[i][j] + 1);
                                       arrTextViewsLocked[k + 2] = checkTextViewExist(arrTextViewsUsed[i][j] - 9);
                                       arrTextViewsLocked[k + 3] = checkTextViewExist(arrTextViewsUsed[i][j] + 10);
                                       arrTextViewsLocked[k + 4] = checkTextViewExist(arrTextViewsUsed[i][j] - 10);
                                       arrTextViewsLocked[k + 5] = checkTextViewExist(arrTextViewsUsed[i][j] + 11);
                                       break;
                                   case 2:
                                       arrTextViewsLocked[k + 1] = checkTextViewExist(arrTextViewsUsed[i][j] - 1);
                                       arrTextViewsLocked[k + 2] = checkTextViewExist(arrTextViewsUsed[i][j] + 9);
                                       arrTextViewsLocked[k + 3] = checkTextViewExist(arrTextViewsUsed[i][j] + 10);
                                       arrTextViewsLocked[k + 4] = checkTextViewExist(arrTextViewsUsed[i][j] - 10);
                                       arrTextViewsLocked[k + 5] = checkTextViewExist(arrTextViewsUsed[i][j] - 11);
                                       break;
                               }
                               break;
                           }
                       }
                   }
               }
            }
        }
        for(int l = 0; l < currentShip[1]; l++) {
            switch (currentShip[2]) {
                case 1:
                    for(int m = 0; m < arrTextViewsLocked.length; m++) {
                        if((currentShip[0] + l) == arrTextViewsLocked[m]) {
                            initTextViewLocked();
                            return false;
                        }
                    }
                    break;
                case 2:
                    for(int m = 0; m < arrTextViewsLocked.length; m++) {
                        if((currentShip[0] + l * 10) == arrTextViewsLocked[m]) {
                            initTextViewLocked();
                            return false;
                        }
                    }
                    break;
            }
        }
        initTextViewLocked();
        return true;
    }

    private boolean shipCheckArea(int shipId, int align, int textViewStartId) {
        int currentShip[] = getShipLocation(shipId);
        for(int i = 0; i < arrShips.length; i++) {
            if(i != shipId) {
                for (int j = 0; j < 5; j++) {
                    if(arrTextViewsUsed[i][j] == -1) {                                          //wenn Schiff schon gesetzt wurde, dann...
                        break;
                    } else {
                        for (int k = 0; k < arrTextViewsLocked.length; k++) {
                            if(arrTextViewsLocked[k] == -1) {                                        //suche nach unbenutztem Platz...
                                arrTextViewsLocked[k] = arrTextViewsUsed[i][j];
                                switch(isTextViewAtBorder(arrTextViewsUsed[i][j])) {
                                    case 0:
                                        arrTextViewsLocked[k + 1] = checkTextViewExist(arrTextViewsUsed[i][j] - 1);
                                        arrTextViewsLocked[k + 2] = checkTextViewExist(arrTextViewsUsed[i][j] + 1);
                                        arrTextViewsLocked[k + 3] = checkTextViewExist(arrTextViewsUsed[i][j] - 9);
                                        arrTextViewsLocked[k + 4] = checkTextViewExist(arrTextViewsUsed[i][j] + 9);
                                        arrTextViewsLocked[k + 5] = checkTextViewExist(arrTextViewsUsed[i][j] + 10);
                                        arrTextViewsLocked[k + 6] = checkTextViewExist(arrTextViewsUsed[i][j] - 10);
                                        arrTextViewsLocked[k + 7] = checkTextViewExist(arrTextViewsUsed[i][j] + 11);
                                        arrTextViewsLocked[k + 8] = checkTextViewExist(arrTextViewsUsed[i][j] - 11);
                                        break;
                                    case 1:
                                        arrTextViewsLocked[k + 1] = checkTextViewExist(arrTextViewsUsed[i][j] + 1);
                                        arrTextViewsLocked[k + 2] = checkTextViewExist(arrTextViewsUsed[i][j] - 9);
                                        arrTextViewsLocked[k + 3] = checkTextViewExist(arrTextViewsUsed[i][j] + 10);
                                        arrTextViewsLocked[k + 4] = checkTextViewExist(arrTextViewsUsed[i][j] - 10);
                                        arrTextViewsLocked[k + 5] = checkTextViewExist(arrTextViewsUsed[i][j] + 11);
                                        break;
                                    case 2:
                                        arrTextViewsLocked[k + 1] = checkTextViewExist(arrTextViewsUsed[i][j] - 1);
                                        arrTextViewsLocked[k + 2] = checkTextViewExist(arrTextViewsUsed[i][j] + 9);
                                        arrTextViewsLocked[k + 3] = checkTextViewExist(arrTextViewsUsed[i][j] + 10);
                                        arrTextViewsLocked[k + 4] = checkTextViewExist(arrTextViewsUsed[i][j] - 10);
                                        arrTextViewsLocked[k + 5] = checkTextViewExist(arrTextViewsUsed[i][j] - 11);
                                        break;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        for(int l = 0; l < currentShip[1]; l++) {
            switch (align) {
                case 1:
                    for(int m = 0; m < arrTextViewsLocked.length; m++) {
                        if((textViewStartId + l) == arrTextViewsLocked[m]) {
                            initTextViewLocked();
                            return false;
                        }
                    }
                    break;
                case 2:
                    for(int m = 0; m < arrTextViewsLocked.length; m++) {
                        if((textViewStartId + l * 10) == arrTextViewsLocked[m]) {
                            initTextViewLocked();
                            return false;
                        }
                    }
                    break;
            }
        }
        initTextViewLocked();
        return true;
    }

    private boolean checkNewLocation(int align, int tv) {
        boolean exists = true;
        for(int i = 0; i < lastShipTouched[2] /*=Länge des Schiffs*/; i++) {
            if (checkTextViewExist(arrTextViewsUsed[lastShipTouched[0]][i]) == -1) {
                exists = false;
                break;
            }
        }
        switch (align) {
            case 1:
                if (exists & (tv%10 > (tv + lastShipTouched[2]-1)%10)) {
                    exists = false;
                }
                break;
            case 2:
                if (exists & (tv%10 < (tv + (lastShipTouched[2]-1)*10)%10)) {
                    exists = false;
                }
                break;
        }
        return (exists & shipCheckArea(lastShipTouched[0], align, tv));
    }

    private void setNewLocation(int align, int tv) {
        for (int i = 0; i < lastShipTouched[2] /*=Länge des Schiffs*/; i++) {
            switch (align) {
                case 1:
                    arrTextViewsUsed[lastShipTouched[0]][i] = tv + i;
                    break;
                case 2:
                    arrTextViewsUsed[lastShipTouched[0]][i] = tv + i * 10;
                    break;
            }
        }
    }

    private void shipBlink(final int shipId, int color) {
        switch (color) {
            case 1:
                arrShips[shipId].setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                break;
            case 2:
                arrShips[shipId].setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                break;
        }
        arrShips[shipId].animate().scaleX(1.15f).scaleY(1.15f).setDuration(150);
        arrShips[shipId].postDelayed(new Runnable() {
            @Override
            public void run() {
                arrShips[shipId].animate().scaleX(1f).scaleY(1f).setDuration(150);
            }
        }, 150);
        arrShips[shipId].postDelayed(new Runnable() {
            @Override
            public void run() {
                arrShips[shipId].clearColorFilter();
            }
        }, 150);
    }

    private int isTextViewAtBorder(int textViewId) {                                                //0 = nicht am Rand, 1 = linker Rand, 2 = rechter Rand
        String val = Integer.toString(textViewId);
        int start = val.length();
        val = val.substring(start-1);
        switch(val) {
            case "9":
                return 2;
            case "0":
                return 1;
            default:
                return 0;
        }
    }

    private int checkTextViewExist(int textViewId) {
        if (!((textViewId >= 0) & (textViewId <= 99))) {
            return -1;
        } else {
            return textViewId;
        }
    }

    //------------------------------------------------------------------------------//
    //------------------------------------- Other ----------------------------------//
    //------------------------------------------------------------------------------//

    private void initPlaceholder() {
        arrPlaceholder[0] = (TextView) findViewById(R.id.textViewPlaceholder0);
        arrPlaceholder[1] = (TextView) findViewById(R.id.textViewPlaceholder1);
        arrPlaceholder[2] = (TextView) findViewById(R.id.textViewPlaceholder2);
        arrPlaceholder[3] = (TextView) findViewById(R.id.textViewPlaceholder3);
    }

    private void initTextViewUsed() {
        for(int i = 0; i < arrShips.length; i++) {
            for (int j = 0; j < 5; j++) {
                arrTextViewsUsed[i][j] = -1;
            }
        }
    }

    private void initTextViewLocked() {
            for (int i = 0; i < arrTextViewsLocked.length; i++) {
                arrTextViewsLocked[i] = -1;
            }
    }

    private float dpToPx(int dp) {
        Resources r = getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    //------------------------------------------------------------------------------//
    //----------------------------- UNDER DEVELOPMENT ------------------------------//
    //------------------------------------------------------------------------------//

    private void fixTextViewsUsed() {
        int l = lastShipTouched[2];
        int shipId = lastShipTouched[0];
        int startTextView = getShipLocation(shipId)[0];
        int align = getShipLocation(shipId)[2];
        for (int i = 0; i < l; i++) {
            switch (align) {
                case 1:
                    arrTextViewsUsed[shipId][i] = startTextView + i * 10;
                    break;
                case 2:
                    arrTextViewsUsed[shipId][i] = startTextView + i;
                    break;
            }
        }
    }

    private void dev() {
        if (shipCheckArea(lastShipTouched[0]) & !shipOutsideLayout(lastShipTouched[0])) {
            arrShips[lastShipTouched[0]].clearColorFilter();
        } else {
            arrShips[lastShipTouched[0]].setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        }
    }

    private void btnStart() {

    }

    private void btnRotate() {
        ImageView ship = arrShips[lastShipTouched[0]];
        if (lastShipTouched[1] != 0) {
            Bitmap shipImage = scaleShipImage(lastShipTouched[2], lastShipTouched[1]);
            Matrix matrix = new Matrix();
            if (ship.getWidth() > ship.getHeight()) {
                matrix.setRotate(90f);
                ship.setY((int) (ship.getY()-(Math.ceil(lastShipTouched[2]/2))*textViewSize));
                ship.setX((int) (ship.getX()+(Math.ceil(lastShipTouched[2]/2))*textViewSize));
            } else {
                matrix.setRotate(180f);
                ship.setX((int) (ship.getX()-(Math.ceil(lastShipTouched[2]/2))*textViewSize));
                ship.setY((int) (ship.getY()+(Math.ceil(lastShipTouched[2]/2))*textViewSize));
            }
            Bitmap rotated = Bitmap.createBitmap(shipImage, 0, 0, shipImage.getWidth(), shipImage.getHeight(), matrix, false);
            ship.setImageBitmap(rotated);
            fixTextViewsUsed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: GOING BACK
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        com.example.tom.battleships.SystemUiHelper uiHelper =                                       //Aufrufen einer Helper-Klasse, die einen "richtigen" Vollbildmodus hervorruft
                new com.example.tom.battleships.SystemUiHelper(
                        this, com.example.tom.battleships.SystemUiHelper.LEVEL_IMMERSIVE ,flags);
        uiHelper.hide();
    }
}




/*
                TranslateAnimation a = new TranslateAnimation(
                        Animation.ABSOLUTE, 0,
                        Animation.ABSOLUTE, arrShipOrigins[shipId][0]-arrShips[shipId].getX(),
                        Animation.ABSOLUTE, 0,
                        Animation.ABSOLUTE, arrShipOrigins[shipId][1]-arrShips[shipId].getY());
                a.setDuration(200);
                a.setFillAfter(true);
                arrShips[shipId].startAnimation(a);
                arrShips[shipId].postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        arrShips[shipId].setX(arrShipOrigins[shipId][0]);
                        arrShips[shipId].setY(arrShipOrigins[shipId][1]);
                    }
                }, 200);
                setArrTextViewsUsed(shipId);
 */


/*  Erste Versuch, zu überprüfen, ob das Schiff dort platziert werden darf
    private boolean shipCheckArea(int shipId) {
        int startX, startY, endX, endY, placedStartY = 0, placedEndX = 0, placedStartX = 0, placedEndY = 0;
        boolean ok = false;
        boolean okX, okY;
        int placedShip[] = getShipLocation(shipId);

        switch (placedShip[2]) {
            case 1:
                placedStartX = (int) arrTextViews[placedShip[0]].getX();
                placedEndX = (int) arrTextViews[placedShip[0]].getX() + textViewSize * placedShip[1];
                placedStartY = (int) arrTextViews[placedShip[0]].getY();
                placedEndY = (int) arrTextViews[placedShip[0]].getY() + textViewSize;
                break;
            case 2:
                placedStartX = (int) arrTextViews[placedShip[0]].getX();
                placedEndX = (int) arrTextViews[placedShip[0]].getX() + textViewSize;
                placedStartY = (int) arrTextViews[placedShip[0]].getY();
                placedEndY = (int) arrTextViews[placedShip[0]].getY() + textViewSize * placedShip[1];
                break;
        }
        for (int i = 0; i < arrShips.length; i++) {
            int iShip[] = getShipLocation(i);
            if (i != shipId & iShip[0] > -1) {
                switch (iShip[2]) {
                    case 1:
                        startX = (int) arrShips[i].getX() - textViewSize;
                        endX = (int) arrShips[i].getX() + (iShip[1] + 1) * textViewSize;
                        startY = (int) arrShips[i].getY() - textViewSize;
                        endY = (int) arrShips[i].getY() + textViewSize;

                        for (int j = 0; j < placedShip[1]; j++) {
                            if (j == 0 | j == placedShip[1]) {
                                placedStartX += textViewSize;
                            } else {
                                placedStartX += textViewSize - 5;
                            }
                            if (placedShip[2] == iShip[2]) {
                                okX = (placedStartX > startX & placedStartX < endX);
                                okY = (placedStartY > startY & placedStartY < endY | placedEndY > startY & placedEndY < endY);
                                ok = !(okX & okY);                                                      //NAND
                            } else {
                                okX = (placedStartY > startX & placedStartY < endX);
                                okY = (placedStartX > startY & placedStartX < endY | placedEndX > startY & placedEndX < endY);
                                ok = !(okX & okY);
                            }
                            if (!ok) {
                                j = placedShip[1] + 1;
                            }
                        }
                        break;
                    case 2:
                        startX = (int) arrShips[i].getX() - textViewSize;
                        endX = (int) arrShips[i].getX() + textViewSize;
                        startY = (int) arrShips[i].getY() - textViewSize;
                        endY = (int) arrShips[i].getY() + (iShip[1] + 1) * textViewSize;


                        for (int j = 0; j < placedShip[1] + 2; j++) {
                            if (j == 0 | j == placedShip[1]) {
                                placedStartY += textViewSize;
                            } else {
                                placedStartY += textViewSize - 5;
                            }
                            if (placedShip[2] == iShip[2]) {
                                okX = (placedStartX > startX & placedStartX < endX | placedEndX > startX & placedEndX < endX);
                                okY = (placedStartY > startY & placedStartY < endY);
                                ok = !(okX & okY);
                            } else {
                                okX = (placedStartY > startX & placedStartY < endX | placedEndY > startX & placedEndY < endX);
                                okY = (placedStartX > startY & placedStartX < endY);
                                ok = !(okX & okY);
                            }
                            if (!ok) {
                                j = placedShip[1] + 1;
                            }
                        }
                        break;
                }
            } else {
                ok = true;
            }
            if (!ok) {
                return false;
            }
        }
        return true;
    }                                                                                               //true = alles ok; false = kann nicht gesetzt werden -> Schiff in der Nähe

*/
/* Versuch 1. -> Automatisch freien Platz nach drehen finden
    private void shipFindValidLocation() {
        ImageView ship = arrShips[lastShipTouched[0]];
        int shipId = lastShipTouched[0];
        int align, startTextView;
        int l = lastShipTouched[2];
        int done = 0;
        int ext = 0;
        boolean exit = false;
        boolean textViewNotExists = false;
        boolean whileEnd;

        if (((lastShipTouched[1] != 0) & !shipOutsideLayout(shipId))) {
            Bitmap shipImage = scaleShipImage(lastShipTouched[2], lastShipTouched[1]);
            Matrix matrix = new Matrix();
            if (ship.getWidth() > ship.getHeight()) {
                matrix.setRotate(90f);
                align = 2;
            } else {
                matrix.setRotate(180f);
                align = 1;
            }
            Bitmap rotated = Bitmap.createBitmap(shipImage, 0, 0, shipImage.getWidth(), shipImage.getHeight(), matrix, false);



            do {
                startTextView = getShipLocation(shipId)[0] + ext;

                if((startTextView%10) == 9) {
                    ext = 0;
                    done ++;
                }
                if ((startTextView%10) == 0) {
                    ext = 0;
                    done ++;
                }

                if(arrTextViews[arrTextViewsUsed[shipId][0]].getY() + rotated.getHeight() > arrTextViews[90].getY() + textViewSize) {
                    int fix = (int) -(((ship.getY() + rotated.getHeight()) - (arrTextViews[90].getY() + textViewSize)) / textViewSize);
                    startTextView += fix*10;
                    Toast.makeText(this, Integer.toString(ext), Toast.LENGTH_SHORT).show();
                }

                if(arrTextViews[arrTextViewsUsed[shipId][0]].getX() + rotated.getWidth() > arrTextViews[9].getX() + textViewSize) {
                    done = 1;
                    ext = (int) -(((ship.getX() + rotated.getWidth()) - (arrTextViews[9].getX() + textViewSize)) / textViewSize);
                    startTextView += ext;
                }



                for (int i = 0; i < l; i++) {
                    switch (align) {
                        case 1:
                            arrTextViewsUsed[shipId][i] = startTextView + i;
                            break;
                        case 2:
                            arrTextViewsUsed[shipId][i] = startTextView + i * 10;
                            break;
                    }
                }
                switch(done) {
                    case 0:
                        ext ++;
                        break;
                    case 1:
                        ext --;
                        break;
                    case 2:
                        exit = true;
                        break;
                }

                for(int i = 0; i < l; i++) {
                    if (checkTextViewExist(arrTextViewsUsed[shipId][i]) == -1) {
                        textViewNotExists = true;
                        break;
                    }
                }

                if (exit) {
                    for (int i = 0; i < l; i++) {
                        switch (align) {
                            case 1:
                                arrTextViewsUsed[shipId][i] = getShipLocation(shipId)[0] + i * 10;
                                break;
                            case 2:
                                arrTextViewsUsed[shipId][i] = getShipLocation(shipId)[0] + i;
                                break;
                        }
                    }
                    break;
                }
                whileEnd = ((shipCheckArea(lastShipTouched[0], align, startTextView) & !textViewNotExists));
            } while (!whileEnd);


            if (exit) {shipBlink(shipId, 2);} else {ship.setImageBitmap(rotated); shipBlink(shipId, 1);}

            int x = (int) (arrTextViews[arrTextViewsUsed[shipId][0]].getX() + getResources().getDimension(R.dimen.activity_horizontal_margin));
            int y = (int) (arrTextViews[arrTextViewsUsed[shipId][0]].getY() + getResources().getDimension(R.dimen.activity_vertical_margin));

            ship.animate().x(x).y(y).setDuration(200);
            arrShips[shipId].setX(x);
            arrShips[shipId].setY(y);
        }
    }
*/