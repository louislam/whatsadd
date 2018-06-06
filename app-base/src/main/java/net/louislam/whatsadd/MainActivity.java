package net.louislam.whatsadd;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.louislam.android.L;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //L.alert(this, Locale.getDefault().getDisplayLanguage());

        final EditText areaCode = (EditText) findViewById(R.id.areaCode);
        final EditText number = (EditText) findViewById(R.id.number);
        ImageButton button = (ImageButton) findViewById(R.id.button);

        number.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL && number.getText().length() == 0) {
                    areaCode.requestFocus();
                    areaCode.setSelection(areaCode.getText().length());
                }
                return false;
            }
        });

        areaCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.v("key code", keyCode + "");
                if (areaCode.getText().length() == 3 && keyCode != KeyEvent.KEYCODE_DEL) {
                    number.requestFocus();
                }
                return false;
            }
        });

        String areaCodeDefault = L.getString(MainActivity.this, "areaCode");

        if (areaCodeDefault == null) {
            areaCodeDefault = "852";
        }

        areaCode.setText(areaCodeDefault);
        number.requestFocus();

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
                    startActivity( browse );


                } catch (UnsupportedEncodingException e) {
                    L.alert(MainActivity.this, "");

                } catch (ActivityNotFoundException e) {
                    L.alert(MainActivity.this, getString(R.string.need_browser));

                } catch (Exception e) {
                    L.alert(MainActivity.this, "Unable to find WhatsApp, please install first.");

                }

            }
        });

        BottomNavigationView nav = (BottomNavigationView) findViewById(R.id.navigation);

        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // Handle item selection

            if (item.getItemId() == R.id.navigation_about) {
                String design;
                String lang = Locale.getDefault().getLanguage();

                if (lang.equals("ja")) {
                    design = "作成者: ";
                } else if (lang.equals("zh")) {
                    design = "作者: ";
                } else {
                    design = "Designed by";
                }

                AlertDialog b = new AlertDialog.Builder(MainActivity.this).setMessage( Html.fromHtml("<h2>WhatsAdd</h2>" +
                        design + " LouisLam &copy; 2017<br><br>" +
                        "Twitter: <a href=\"https://twitter.com/LouisLam\">@LouisLam</a><br/><br/>" +
                        "<a href=\"https://louislam.net\">https://louislam.net</a>")).create();
                b.show();

                ((TextView) b.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                return false;
            } else {
                return true;
            }

            }
        });

    }
    
}
