package net.louislam.whatsadd;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener{

    private AlertDialog settingDialog;
    private AlertDialog askDialog;

    public final static String WHATSAPP = "com.whatsapp";
    public final static String WHATSAPP_BUSINESS = "com.whatsapp.w4b";
    private EditText areaCode;
    private EditText number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //L.alert(this, Locale.getDefault().getDisplayLanguage());

        areaCode = (EditText) findViewById(R.id.areaCode);
        number = (EditText) findViewById(R.id.number);
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

        String defaultApp = L.getString(MainActivity.this, "defaultApp");

        if (defaultApp == null) {
            L.storeString(MainActivity.this, "defaultApp", "0");
        }

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



                try {
                    //String url = "https://api.whatsapp.com/send?phone=" + URLEncoder.encode(areaCodeString + numberString, "utf-8");
                    //Log.v("URL", url);
                    //browse = new Intent( Intent.ACTION_VIEW , Uri.parse(url));

                    boolean installedWhatsApp = appInstalledOrNot(WHATSAPP);
                    boolean installedWhatAppBusiness = appInstalledOrNot(WHATSAPP_BUSINESS);

                    if (installedWhatAppBusiness && installedWhatsApp) {

                        String defaultApp = L.getString(MainActivity.this, "defaultApp");

                        if (defaultApp.equals("0")) {
                            openWhatsApp(WHATSAPP);

                        } else if (defaultApp.equals("1")) {
                            openWhatsApp(WHATSAPP_BUSINESS);

                        } else if (defaultApp.equals("2")) {
                            openAskDialog();
                        }

                    } else if (installedWhatAppBusiness) {
                        openWhatsApp(WHATSAPP_BUSINESS);
                    } else if (installedWhatsApp) {
                        openWhatsApp(WHATSAPP);
                    } else {
                        throw new Exception();
                    }


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

                AlertDialog b = new AlertDialog.Builder(MainActivity.this).setMessage(Html.fromHtml("<h2>WhatsAdd</h2>" +
                        design + " LouisLam &copy; 2017<br><br>" +
                        "Twitter: <a href=\"https://twitter.com/LouisLam\">@LouisLam</a><br/><br/>" +
                        "<a href=\"https://louislam.net\">https://louislam.net</a>")).create();
                b.show();

                ((TextView) b.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                return false;
            } else if (item.getItemId() == R.id.navigation_settings) {
                showSettingDialog();
                return false;
            } else {
                return true;
            }

            }
        });

    }

    public void openWhatsApp(String packageName) {
        String areaCodeString = areaCode.getText().toString().trim();
        String numberString = number.getText().toString().trim();

        if ( areaCodeString.equals("") || numberString.equals("")) {
            Toast.makeText(MainActivity.this, "Please input the area code and phone number.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // New Method
        Uri uri = Uri.parse("smsto:" + areaCodeString + numberString);
        Intent i = new Intent(Intent.ACTION_SENDTO, uri);
        i.setPackage(packageName);


        L.storeString(MainActivity.this, "areaCode", areaCodeString);
        number.setText("");
        startActivity(Intent.createChooser(i, ""));
    }

    public void openAskDialog() {

        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {"WhatsApp","WhatsApp Business"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose App");
        builder.setSingleChoiceItems(items, -1, this);

        askDialog = builder.create();
        askDialog.show();
    }

    public void showSettingDialog() {

        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {"WhatsApp","WhatsApp Business", "Always ask"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Default App");
        builder.setSingleChoiceItems(items, Integer.parseInt(L.getString(this, "defaultApp")), this);

        settingDialog = builder.create();
        settingDialog.show();
    }

    /**
     * Settings onClick event
     */
    public void onClick(DialogInterface dialog, int item) {

        if (dialog == askDialog) {

            if (item == 1) {
                openWhatsApp(WHATSAPP_BUSINESS);
            } else {
                openWhatsApp(WHATSAPP);
            }

        } else if (dialog == settingDialog) {
            L.storeString(MainActivity.this, "defaultApp", "" + item);
        }

        dialog.dismiss();
    }

    private boolean appInstalledOrNot(String uri) {

        PackageManager pm = getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        // For instant app
       if (packages.size() == 0) {
           return true;
       }

        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            L.log("No this app: " + uri);
            return false;
        }
    }

}
