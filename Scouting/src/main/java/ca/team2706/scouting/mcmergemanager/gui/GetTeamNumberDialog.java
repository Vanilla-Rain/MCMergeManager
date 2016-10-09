package ca.team2706.scouting.mcmergemanager.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import ca.team2706.scouting.mcmergemanager.R;

public class GetTeamNumberDialog {
    private Activity launchActivity;
    private EditText editText;

    private String title;
    private String inputHint;
    private int inputType;

    public boolean accepted = false;
    public boolean canceled = false;
    public String inputResult = "-1";

    public GetTeamNumberDialog(String title, String inputHint, int inputType, Activity launchActivity) {
        this.title = title;
        this.inputHint = inputHint;
        this.inputType = inputType;
        this.launchActivity = launchActivity;
    }

    public int getTeamNumber() {
        return Integer.parseInt(inputResult);
    }

    public void displayAlertDialog() {
        LayoutInflater inflater = launchActivity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.layout_custom_dialog, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(launchActivity);

        alert.setTitle(title);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        //this stuff gets the edittext from the view and sets the hint and the inputtype
        editText =  (EditText) alertLayout.findViewById(R.id.inputHint);
        editText.setHint(inputHint);


        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                canceled = true;
            }
        });

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                accepted = true;
                inputResult = editText.getText().toString();
            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

}

