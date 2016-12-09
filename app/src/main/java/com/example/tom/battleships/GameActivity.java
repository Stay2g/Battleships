package com.example.tom.battleships;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

public class GameActivity extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //GameLayoutActivity gLA = new GameLayoutActivity();

        GridLayout GridLayoutPlayer = (GridLayout) findViewById(R.id.gridLayoutPlayer);
        GridLayout GridLayoutEnemy = (GridLayout) findViewById(R.id.gridLayoutEnemy);
        TextView textViewArrow = (TextView) findViewById(R.id.textViewArrow);

        //textViewArrow.setText("asdf <---");

        //gLA.createViews(GridLayoutPlayer, 0);
        //gLA.createViews(GridLayoutEnemy, 300);



    }

    @Override
    public void onClick(View view) {

    }
}
