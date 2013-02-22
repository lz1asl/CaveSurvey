package com.astoev.cave.survey.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.home.HomeActivity;
import com.astoev.cave.survey.activity.main.OptionsActivity;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainMenuActivity extends BaseActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(Constants.LOG_TAG_UI, "Creating main menu");
        new MenuInflater(this).inflate(R.menu.mainmenu, menu);

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Main menu selected - " + item.toString());
        switch (item.getItemId()) {
            case R.id.menuOtherProject:
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;
            case R.id.menuOptions:
                startActivity(new Intent(this, OptionsActivity.class));
                return true;
            case R.id.menuAbout:
                try {
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.about);
                    ImageView image = (ImageView) dialog.findViewById(R.id.aboutImage);
                    image.setImageResource(R.drawable.paldin);
                    TextView url = (TextView) dialog.findViewById(R.id.aboutUrl);
                    Linkify.addLinks(url, Linkify.WEB_URLS);
                    dialog.show();
                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG_UI, "Failed toshow about", e);
                    UIUtilities.showNotification(this, R.string.error);
                }
                return true;
            case R.id.menuExit:

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setMessage(R.string.menu_exit_confirmation_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Log.i(Constants.LOG_TAG_UI, "Exit app");
                                mWorkspace.clean();
                                MainMenuActivity.this.moveTaskToBack(true);
                            }
                        })
                        .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = dialogBuilder.create();
                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
