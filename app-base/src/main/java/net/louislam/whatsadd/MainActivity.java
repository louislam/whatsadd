package net.louislam.whatsadd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.louislam.android.InputListener;
import net.louislam.android.L;
import net.louislam.android.LStorage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public abstract class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener{

    protected AlertDialog settingDialog;
    protected AlertDialog askDialog;

    public final static String WHATSAPP = "com.whatsapp";
    public final static String WHATSAPP_BUSINESS = "com.whatsapp.w4b";

    protected EditText areaCode;
    protected EditText number;
    protected ImageButton button;
    protected TextView currentDefaultWhatsapp;
    protected Switch showKeyboardSwitch;

    protected boolean autoShowKeyboard = true;

    protected boolean again = false;

    // Strings to Show In Dialog with Radio Buttons
    CharSequence[] options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        options = new CharSequence[]{
                "WhatsApp",
                "WhatsApp Business",
                getString(R.string.always_ask)
        };

        //L.alert(this, Locale.getDefault().getDisplayLanguage());

        areaCode = findViewById(R.id.areaCode);
        number = findViewById(R.id.number);
        button = findViewById(R.id.button);
        currentDefaultWhatsapp = findViewById(R.id.currentDefaultWhatsapp);
        showKeyboardSwitch = findViewById(R.id.showKeyboardSwitch);

        number.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
                if(keyCode == KeyEvent.KEYCODE_DEL && number.getText().length() == 0) {
                    if (again) {
                        areaCode.requestFocus();
                        areaCode.setSelection(areaCode.getText().length());
                        again = false;
                    } else {
                        again = true;
                    }
                } else {
                    again = false;
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
            defaultApp = "2";
            L.storeString(MainActivity.this, "defaultApp", defaultApp);
        }

        autoShowKeyboard = LStorage.getBoolean(MainActivity.this, "autoShowKeyboard");
        currentDefaultWhatsapp.setText(options[Integer.parseInt(defaultApp)]);

        showKeyboardSwitch.setChecked(autoShowKeyboard);
        showKeyboardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoShowKeyboard = isChecked;
                LStorage.store(MainActivity.this, "autoShowKeyboard", autoShowKeyboard);
            }
        });

        String areaCodeDefault = L.getString(MainActivity.this, "areaCode");

        if (areaCodeDefault == null) {
            areaCodeDefault = "852";
        }

        areaCode.setText(areaCodeDefault);

        L.log("Auto Show Keyboard:" + autoShowKeyboard);
        if (autoShowKeyboard) {
            number.requestFocus();
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    e.printStackTrace();
                    L.alert(MainActivity.this, getString(R.string.no_whatsapp));

                }

            }
        });

        BottomNavigationView nav = findViewById(R.id.navigation);

        nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle item selection

                if (item.getItemId() == R.id.navigation_about) {
                    String design;
                    String lang = Locale.getDefault().getLanguage();

                    String version = "(" + L.getAppVersion(MainActivity.this) + ") ";

                    switch (lang) {
                        case "ja":
                            design = "作成者: ";
                            break;
                        case "zh":
                            design = "作者: ";
                            break;
                        default:
                            design = "Designed by";
                            break;
                    }

                    AlertDialog b = new AlertDialog.Builder(MainActivity.this).setMessage(Html.fromHtml("<h2>WhatsAdd</h2>" +
                            version + design + " LouisLam &copy; 2017-2018<br><br>" +
                            "Twitter: <a href=\"https://twitter.com/LouisLam\">@LouisLam</a><br/><br/>" +
                            "<a href=\"https://louislam.net/blog/2018/01/whatsadd/\">https://louislam.net</a><br /><br/>" +
                            "(Find me to translate the app into your language)")).create();
                    b.show();

                    ((TextView) b.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
                    return false;
                } else if (item.getItemId() == R.id.navigation_settings) {
                    //showDefaultWhatsappDialog();
                    openSettingPage();
                    return true;
                } else {
                    openAddPage();
                    return true;
                }

            }
        });

    }

    public abstract void openSettingPage();
    public abstract void openAddPage();

    public abstract void openWhatsApp(String packageName);

    public void openAskDialog() {

        // Strings to Show In Dialog with Radio Buttons
        final CharSequence[] items = {"WhatsApp","WhatsApp Business"};

        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_app);
        builder.setSingleChoiceItems(items, -1, this);

        askDialog = builder.create();
        askDialog.show();
    }

    public void showDefaultWhatsappDialog() {


        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.default_app);
        builder.setSingleChoiceItems(options, Integer.parseInt(L.getString(this, "defaultApp")), this);

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

            boolean installedApp = false;

            if (item == 0) {
                installedApp = appInstalledOrNot(WHATSAPP);
            } else if (item == 1) {
                installedApp = appInstalledOrNot(WHATSAPP_BUSINESS);
            } else if (item == 2) {
                installedApp  = true;
            }

            if (installedApp) {
                currentDefaultWhatsapp.setText(options[item]);
                L.storeString(MainActivity.this, "defaultApp", "" + item);
            } else {
                L.alert(this, getString(R.string.app_not_installed));
            }

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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
