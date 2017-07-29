package net.louislam.whatsadd;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.louislam.android.L;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        final EditText areaCode = (EditText) findViewById(R.id.areaCode);
        final EditText number = (EditText) findViewById(R.id.number);
        ImageButton button = (ImageButton) findViewById(R.id.button);

        areaCode.setText(L.getString(MainActivity.this, "areaCode"));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browse = null;

                String areaCodeString = areaCode.getText().toString().trim();
                String numberString = number.getText().toString().trim();

                if ( areaCodeString.equals("") || numberString.equals("")) {
                    Toast.makeText(MainActivity.this, "Please input the area code and phone number.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String url = "https://api.whatsapp.com/send?phone=" + URLEncoder.encode(areaCodeString + numberString, "utf-8");
                    Log.v("URL", url);

                    browse = new Intent( Intent.ACTION_VIEW , Uri.parse(url));
                    L.storeString(MainActivity.this, "areaCode", areaCodeString);
                    number.setText("");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startActivity( browse );
            }
        });

        BottomNavigationView nav = (BottomNavigationView) findViewById(R.id.navigation);

        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle item selection
                switch (item.getItemId()) {
                    case R.id.navigation_about:

                        AlertDialog b = new AlertDialog.Builder(MainActivity.this).setMessage( Html.fromHtml("<h2>WhatsAdd</h2>" +
                                "Designed by LouisLam &copy; 2017<br><br>" +
                                "Twitter: <a href=\"https://twitter.com/LouisLam\">@LouisLam</a><br/><br/>" +
                                "<a href=\"https://louislam.net\">https://louislam.net</a>")).create();
                        b.show();

                        ((TextView) b.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                        return false;
                    default:
                        return true;
                }
            }
        });

    }
    
}
