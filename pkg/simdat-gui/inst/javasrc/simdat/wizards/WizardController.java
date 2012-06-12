package simdat.wizards;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import java.io.IOException;

/**
 * This class is responsible for reacting to events generated by pushing any of the
 * three buttons, 'Next', 'Previous', and 'Cancel.' Based on what button is pressed,
 * the controller will update the model to show a new panel and reset the state of
 * the buttons as necessary.
 */
public class WizardController implements ActionListener {

    private Wizard wizard;

    /**
     * This constructor accepts a reference to the Wizard component that created it,
     * which it uses to update the button components and access the WizardModel.
     * @param w A callback to the Wizard component that created this controller.
     */
    public WizardController(Wizard w) {
        wizard = w;
    }

    /**
     * Calling method for the action listener interface. This class listens for actions
     * performed by the buttons in the Wizard class, and calls methods below to determine
     * the correct course of action.
     * @param evt The ActionEvent that occurred.
     */
    public void actionPerformed(java.awt.event.ActionEvent evt) {

        if (evt.getActionCommand().equals(Wizard.CANCEL_BUTTON_ACTION_COMMAND)) {
            cancelButtonPressed();
        } else if (evt.getActionCommand().equals(Wizard.BACK_BUTTON_ACTION_COMMAND)) {
            backButtonPressed();
        } else if (evt.getActionCommand().equals(Wizard.NEXT_BUTTON_ACTION_COMMAND)) {
            nextButtonPressed();
        } else if (evt.getActionCommand().equals(Wizard.HELP_BUTTON_ACTION_COMMAND)) {
            helpButtonPressed();
        }

    }

    private void cancelButtonPressed() {

        wizard.close(Wizard.CANCEL_RETURN_CODE);
    }

    private void nextButtonPressed() {

        WizardModel model = wizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

        //  If it is a finishable panel, close down the dialog. Otherwise,
        //  get the ID that the current panel identifies as the next panel,
        //  and display it.

        Object nextPanelDescriptor = descriptor.getNextPanelDescriptor();

        if (nextPanelDescriptor instanceof WizardPanelDescriptor.FinishIdentifier) {
            descriptor.aboutToHidePanel();
            wizard.onFinish();
            wizard.close(Wizard.FINISH_RETURN_CODE);
        } else {
            wizard.setCurrentPanel(nextPanelDescriptor);
        }

    }

    private void backButtonPressed() {

        WizardModel model = wizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

        //  Get the descriptor that the current panel identifies as the previous
        //  panel, and display it.

        Object backPanelDescriptor = descriptor.getBackPanelDescriptor();
        wizard.setCurrentPanel(backPanelDescriptor);

    }

    private void helpButtonPressed() {

        WizardModel model = wizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();
        String url = descriptor.getHelpUrl();
        if (url != null && url.length() > 0) {
            showInBrowser(url);
        }
    }

    void resetButtonsToPanelRules() {

        //  Reset the buttons to support the original panel rules,
        //  including whether the next or back buttons are enabled or
        //  disabled, or if the panel is finishable.

        WizardModel model = wizard.getModel();
        WizardPanelDescriptor descriptor = model.getCurrentPanelDescriptor();

        model.setCancelButtonText(Wizard.CANCEL_TEXT);
        model.setCancelButtonIcon(Wizard.CANCEL_ICON);

        model.setHelpButtonText(Wizard.HELP_TEXT);
        model.setHelpButtonIcon(Wizard.HELP_ICON);

        //  If the panel in question has another panel behind it, enable
        //  the back button. Otherwise, disable it.

        model.setBackButtonText(Wizard.BACK_TEXT);
        model.setBackButtonIcon(Wizard.BACK_ICON);

        if (descriptor.getBackPanelDescriptor() != null) {
            model.setBackButtonEnabled(Boolean.TRUE);
        } else {
            model.setBackButtonEnabled(Boolean.FALSE);
        }

        //  If the panel in question has one or more panels in front of it,
        //  enable the next button. Otherwise, disable it.

        if (descriptor.getNextPanelDescriptor() != null) {
            model.setNextFinishButtonEnabled(Boolean.TRUE);
        } else {
            model.setNextFinishButtonEnabled(Boolean.FALSE);
        }

        //  If the panel in question is the last panel in the series, change
        //  the Next button to Finish. Otherwise, set the text back to Next.

        if (descriptor.getNextPanelDescriptor() instanceof WizardPanelDescriptor.FinishIdentifier) {
            model.setNextFinishButtonText(Wizard.FINISH_TEXT);
            model.setNextFinishButtonIcon(Wizard.FINISH_ICON);
        } else {
            model.setNextFinishButtonText(Wizard.NEXT_TEXT);
            model.setNextFinishButtonIcon(Wizard.NEXT_ICON);
        }

    }

    public static boolean showInBrowser(String url) {


        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        try {
            if (os.indexOf("win") >= 0) {
                String[] cmd = new String[4];
                cmd[0] = "cmd.exe";
                cmd[1] = "/C";
                cmd[2] = "start";
                cmd[3] = url;
                rt.exec(cmd);
            } else if (os.indexOf("mac") >= 0) {
                rt.exec("open " + url);
            } else {
                //prioritized 'guess' of users' preference
                String[] browsers = {"epiphany", "firefox", "mozilla", "konqueror",
                    "netscape", "opera", "links", "lynx"};

                StringBuffer cmd = new StringBuffer();
                for (int i = 0; i < browsers.length; i++) {
                    cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");
                }

                rt.exec(new String[]{"sh", "-c", cmd.toString()});
                //rt.exec("firefox http://www.google.com");
                //System.out.println(cmd.toString());

            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "\n\n The system failed to invoke your default web browser while attempting to access: \n\n " + url + "\n\n",
                    "Browser Error",
                    JOptionPane.WARNING_MESSAGE);

            return false;
        }
        return true;
    }
}
