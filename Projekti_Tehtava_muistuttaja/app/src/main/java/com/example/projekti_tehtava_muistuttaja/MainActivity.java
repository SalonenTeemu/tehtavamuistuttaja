package com.example.projekti_tehtava_muistuttaja;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.projekti_tehtava_muistuttaja.Handler.AppHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// MainActivity-luokka, jossa totetutetaan napit, menu ja oikeuksien kysyminen
// Itse päätoiminnallisuus toteutetaan AppHandler-luokassa
// Perii luokan AppCompatActivity ja toteuttaa PopupMenun OnMenuItemClickListener-rajapinnan
public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static final String TAG = "Muistuttajasofta: ";

    // Muuttujat, jotka edustavat koodeja pyydettäviin käyttöoikeuksiin
    private final int MY_PERMISSIONS_REQUEST_SCHEDULE_EXACT_ALARM = 56;
    private final int MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS = 57;
    private final int MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE = 58;
    private final int MY_PERMISSIONS_REQUEST_VIBRATE = 59;

    private Context context;

    // Muuttujat saatujen käyttöoikeuksien kertomiselle
    private static boolean foregroundAquired = false;
    private static boolean vibrateAquired = false;
    private static boolean postNotificationsAquired = false;
    private static boolean scheduleExactAquired = false;

    // AppHandler-instanssi
    private AppHandler appHandler;

    // Näkymän elementit
    private FloatingActionButton buttonAdd;
    private FloatingActionButton buttonDelete;
    private ImageView buttonMenu;

    // Popup-menu
    private PopupMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Asetetaan sovellusmuoto (night mode) pois päältä
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Lukitaan screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Käytetään layoutia activity_main
        setContentView(R.layout.activity_main);

        context = this;

        ColorStateList csl = ColorStateList.valueOf(getResources().getColor(R.color.white));

        // Asetetaan floating action buttonien värit manuaalisesti valkoiseksi
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonAdd.setImageTintList(csl);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonDelete.setImageTintList(csl);
        buttonMenu = findViewById(R.id.buttonMenu);

        // Luodaan uusi PopupMenu-instanssi ja liitetään se valikkopainikkeeseen
        menu = new PopupMenu(this, buttonMenu);
        menu.getMenuInflater().inflate(R.menu.menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);

        // Luodaan uusi AppHandler-instanssi, jolle annetaan tämä mainActivity-konteksti
        appHandler = new AppHandler(this);

        // Asetetaan valikkopainikkeen klikatessa toiminnallisuus, jolloin menu näytetään
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });

        // Asetetaan uuden tehtävän lisäämiiseen käytetyn napin toiminnallisuus, jolloin kutsutaan appHandler.buttonAddPressed
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appHandler.buttonAddPressed();
            }
        });

        // Asetetaan kaikkien tehtävien poistamiseen käytetyn napin toiminnallisuus, jolloin kutsutaan appHandler.buttonDeletePressed
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appHandler.buttonDeletePressed();
            }
        });

        // Tarkistetaan oikeudet
        checkPermissions();
        Log.d(MainActivity.TAG, Build.VERSION.SDK_INT + ": " + vibrateAquired + " " + foregroundAquired + " " + scheduleExactAquired + " " + postNotificationsAquired);
    }

    // Metodi tarkistaa tarvittavat käyttöoikeudet API:n mukaan
    public void checkPermissions() {
        // Jos API >= 33, niin tarvitaan kaikki käyttöoikedet -> kysytään niitä askPermission-metodin avulla
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            askPermission(Manifest.permission.POST_NOTIFICATIONS,
                    "Ilmoitusoikeus on hälytysten ja muistutusten kannalta välttämätön.", MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS, postNotificationsAquired);
            askPermission(Manifest.permission.SCHEDULE_EXACT_ALARM,
                    "Hälytysoikeus on hälytysten ja muistutusten kannalta välttämätön.", MY_PERMISSIONS_REQUEST_SCHEDULE_EXACT_ALARM, scheduleExactAquired);
            askPermission(Manifest.permission.FOREGROUND_SERVICE,
                    "Etusijaoikeus on hälytysten ja muistutusten kannalta välttämätön.", MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE, foregroundAquired);
            askPermission(Manifest.permission.VIBRATE,
                    "Värinäoikeus vaaditaan muistutusten ja hälytysten värinään.", MY_PERMISSIONS_REQUEST_VIBRATE, vibrateAquired);

            // Jos 31 <= API < 33, niin ei tarvitse kysyä POST_NOTIFICATIONS -> kysytään muita askPermission-metodin avulla
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            postNotificationsAquired = true;
            askPermission(Manifest.permission.SCHEDULE_EXACT_ALARM,
                    "Hälytysoikeus on hälytysten ja muistutusten kannalta välttämätön.", MY_PERMISSIONS_REQUEST_SCHEDULE_EXACT_ALARM, scheduleExactAquired);
            askPermission(Manifest.permission.FOREGROUND_SERVICE,
                    "Etusijaoikeus on hälytysten ja muistutusten kannalta välttämätön.", MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE, foregroundAquired);
            askPermission(Manifest.permission.VIBRATE,
                    "Värinäoikeus vaaditaan muistutusten ja hälytysten värinään.", MY_PERMISSIONS_REQUEST_VIBRATE, vibrateAquired);

            // Jos 28 <= API < 31, niin ei tarvitse kysyä POST_NOTIFICATIONS eikä SCHEDULE_EXACT_ALARM -> kysytään muita askPermission-metodin avulla
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            postNotificationsAquired = true;
            scheduleExactAquired = true;
            askPermission(Manifest.permission.FOREGROUND_SERVICE,
                    "Etusijaoikeus on hälytysten ja muistutusten kannalta välttämätön.", MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE, foregroundAquired);
            askPermission(Manifest.permission.VIBRATE,
                    "Värinäoikeus vaaditaan muistutusten ja hälytysten värinään.", MY_PERMISSIONS_REQUEST_VIBRATE, vibrateAquired);

            // Muuten jos API < 28, niin ei tarvitse kysyä kuin VIBRATE -> kysytään sitä askPermission-metodin avulla
        } else {
            postNotificationsAquired = true;
            scheduleExactAquired = true;
            foregroundAquired = true;
            askPermission(Manifest.permission.VIBRATE,
                    "Värinäoikeus vaaditaan muistutusten ja hälytysten värinään.", MY_PERMISSIONS_REQUEST_VIBRATE, vibrateAquired);
        }
    }

    // Metodi kysyy annettua käyttöoikeutta käyttäjältä
    private void askPermission(String permission, String message, int code, boolean toBeGranted) {
        // Tarkastetaan, onko oikeus jo myönnetty / automaattisesti saatava
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Tarkastetaanko, pitäisikö näyttää selitys oikeuden tarpeellisuudesta
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {
                // Näytetään selitys käyttöoikeuden tarpeesta annetun viestin avulla
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(message)
                        .setTitle("Käyttöoikeus vaaditaan")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Kysytään oikeutta
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{permission}, code);
                            }
                        })
                        .setNegativeButton("PERUUTA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Näytetään viesti käyttäjälle, että käyttöoikeuden voi myöntää myöhemmin
                                Toast.makeText(context, "Voit antaa oikeuden myöhemmin kotiruudun menusta.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Kysytään oikeutta
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission}, code);
                if (!toBeGranted) {
                    // Jos käyttöoikeuden myöntäminen on pakollista, näytetään vaihtoehto siirtyä sovelluksen asetuksiin
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(message)
                            .setTitle("Käyttöoikeus vaaditaan")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("PERUUTA", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Näytetään viesti käyttäjälle, että käyttöoikeuden voi myöntää myöhemmin
                                    Toast.makeText(context, "Voit antaa oikeuden myöhemmin kotiruudun menusta.",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        } else {
            // Jos oikeus on saatu, asetetaan totuuarvot tämän mukaan
            if (code == MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS) {
                postNotificationsAquired = true;
            } else if (code == MY_PERMISSIONS_REQUEST_SCHEDULE_EXACT_ALARM) {
                scheduleExactAquired = true;
            } else if (code == MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE) {
                foregroundAquired = true;
            } else if (code == MY_PERMISSIONS_REQUEST_VIBRATE) {
                vibrateAquired = true;
            }
        }
    }

    // Metodi, joka käsittelee käyttöoikeuspyyntöjen tulokset
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SCHEDULE_EXACT_ALARM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scheduleExactAquired = true;
                } else {
                    scheduleExactAquired = false;
                }
                break;
            case MY_PERMISSIONS_REQUEST_POST_NOTIFICATIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    postNotificationsAquired = true;
                } else {
                    postNotificationsAquired = false;
                }
                break;
            case MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    foregroundAquired = true;
                } else {
                    foregroundAquired = false;
                }
                break;
            case MY_PERMISSIONS_REQUEST_VIBRATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    vibrateAquired = true;
                } else {
                    vibrateAquired = false;
                }
                break;
        }
    }

    // Metodi palauttaa tiedon siitä, onko kaikki oikeudet myönnetty
    public static boolean allPermissionsGranted() {
        return scheduleExactAquired && foregroundAquired && vibrateAquired && postNotificationsAquired;
    }

    // Metodi, joka näyttää varmistusikkunan kaikkien tehtävien poistamisesta
    public void confirmDeleteAllTasks(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Vahvista tehtävien poisto");
        builder.setMessage("Oletko varma, että haluat poistaa kaikki tehtävät?");
        builder.setPositiveButton("Vahvista",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        appHandler.deleteAllTasks();
                    }
                });
        builder.setNegativeButton("Peruuta", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Metodi, joka käsittelee aktiviteetin paluun, eli käsittelee tulokset toisista aktiviteeteista
    // Käytännössä joko tehtävän muokkaaminen tai uuden tehtävän luominen
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        appHandler.onActivityResult(requestCode, resultCode, data);
    }

    // Metodi käsittelee menun painikkeiden painallukset
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            // Jos painettu "Kirjaudu ulos"-painiketta, kirjaudutaan käyttäjä ulos sovelluksesta
            case R.id.signOut:
                appHandler.signUserOut(false);
                return true;

            // Jos painettu "Kirjautumistila"-painiketta, tarkistetaan ja tulostetaan käyttäjän kirjautumistila
            case R.id.signInStatus:
                appHandler.checkUserValidation(true);
                return true;

            // Jos painettu "Anna välttämättömät käyttöoikeudet"-painiketta,
            // tarkistetaan ovatko kaikki tarvittavat oikeudet jo myönnetty ja jos ei,
            // kysytään niitä uudelleen
            case R.id.givePermissions:
                if (MainActivity.allPermissionsGranted()) {
                    Toast.makeText(context, "Olet antanut jo kaikki tarvittavat oikeudet.",
                            Toast.LENGTH_LONG).show();
                } else {
                    checkPermissions();
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Tarkistetaan käyttäjän kirjautumistila
        appHandler.checkUserValidation(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}