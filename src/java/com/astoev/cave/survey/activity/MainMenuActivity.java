package com.astoev.cave.survey.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.home.HomeActivity;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainMenuActivity extends BaseActivity {

	/** Constant that shows that there is no child menu item */
	protected static int NO_CHILD_MENU_ITEMS = 0;
	
	/**
	 * Defines the child activity's additional menu items
	 * 
	 * @return menu resource id
	 */
	protected int getChildsOptionsMenu(){
		return NO_CHILD_MENU_ITEMS;
	}
	
	/**
	 * Helper method that defines if the child class will define its additional menu items
	 * 
	 * @return true if the child activity defines its additional menu items
	 */
	protected boolean hasChildOptionsMenu(){
		return true;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(Constants.LOG_TAG_UI, "Creating main menu");
        
        MenuInflater menuInflater = getMenuInflater();
        
        if (hasChildOptionsMenu() && getChildsOptionsMenu() != NO_CHILD_MENU_ITEMS){
        	menuInflater.inflate(getChildsOptionsMenu(), menu);
        }
        
        menuInflater.inflate(R.menu.mainmenu, menu);

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Main menu selected - " + item.toString());
        switch (item.getItemId()) {
            case R.id.menuOpen:
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                return true;
//            case R.id.menuAbout:
//                try {
//                    Dialog dialog = new Dialog(this);
//                    dialog.setContentView(R.layout.about);
//                    ImageView image = (ImageView) dialog.findViewById(R.id.aboutImage);
//                    image.setImageResource(R.drawable.paldin);
//                    TextView url = (TextView) dialog.findViewById(R.id.aboutUrl);
//                    Linkify.addLinks(url, Linkify.WEB_URLS);
//                    dialog.show();
//                } catch (Exception e) {
//                    Log.e(Constants.LOG_TAG_UI, "Failed toshow about", e);
//                    UIUtilities.showNotification(this, R.string.error);
//                }
//                return true;
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
            default:
                UIUtilities.showNotification(this, "TODO " + item.getItemId());
        }
        return super.onOptionsItemSelected(item);
    }

}
