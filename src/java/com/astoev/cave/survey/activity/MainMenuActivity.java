package com.astoev.cave.survey.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.R;
import com.astoev.cave.survey.activity.dialog.ConfirmationDialog;
import com.astoev.cave.survey.activity.dialog.ConfirmationHandler;
import com.astoev.cave.survey.activity.dialog.ConfirmationOperation;
import com.astoev.cave.survey.service.bluetooth.BluetoothService;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/23/12
 * Time: 4:09 PM
 *
 * @author Alexander Stoev
 * @author Zhivko Mitrev
 */
public class MainMenuActivity extends BaseActivity implements ConfirmationHandler {

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
	 * Helper method that determines if the base menu should be shown
	 * 
	 * @return true if should be shown otherwise false
	 */
	protected boolean showBaseOptionsMenu(){
		return true;
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
        if (showBaseOptionsMenu()){
        	menuInflater.inflate(R.menu.basemenu, menu);
        }

        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(Constants.LOG_TAG_UI, "Main menu selected - " + item.toString());
        switch (item.getItemId()) {
            case R.id.menuExit:
                showExitConfirmationDialog();
                return true;
            default:
            	return super.onOptionsItemSelected(item);
        }
    }

    protected void showExitConfirmationDialog(){
        String message = getString(R.string.menu_exit_confirmation_question);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConfirmationDialog.OPERATION, ConfirmationOperation.EXIT);
        bundle.putString(ConfirmationDialog.MESSAGE, message);
        ConfirmationDialog confirmationDialog = new ConfirmationDialog();
        confirmationDialog.setArguments(bundle);
        confirmationDialog.show(getSupportFragmentManager(), ConfirmationDialog.CONFIRM_DIALOG);
    }

    @Override
    public boolean confirmOperation(ConfirmationOperation operationArg) {
        if (operationArg == null){
            return false;
        }
        if (ConfirmationOperation.EXIT.equals(operationArg)){
            exit();
            return true;
        }
        return false;
    }

    /**
     * Cleans resources. Stops services and exit
     */
    protected void exit(){
        Log.i(Constants.LOG_TAG_UI, "Exit app");
        getWorkspace().clean();
        BluetoothService.stop();
        UIUtilities.cleanStatusBarMessages(MainMenuActivity.this);
        MainMenuActivity.this.moveTaskToBack(true);
//        System.exit(0);
    }
}
