package com.controlj.addon.gwttree.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

/**<!=========================================================================>
   Helper dialog to display dialog boxes.
<!==========================================================================>*/
public class Dialog
{
   /**<!====== showHtmlErrorDialog ===========================================>
      Displays an error dialog box.  The given <code>html</code> is displayed
      as HTML inside the dialog.  The only button is a "Close" button.
      <!      Name       Description>
      @param  title      The dialog title.
      @param  html       The dialog content, as HTML.
   <!=======================================================================>*/
   public static void showHtmlErrorDialog(String title, String html)
   {
      showErrorDialog(title, html, null);
   }

   /**<!====== showTextErrorDialog ===========================================>
      Displays an error dialog box.  The given <code>text</code> is displayed
      inside the dialog.  The only button is a "Close" button.
      <!      Name       Description>
      @param  title      The dialog title.
      @param  text       The dialog content, as a text string.
   <!=======================================================================>*/
   public static void showTextErrorDialog(String title, String text)
   {
      showErrorDialog(title, null, text);
   }

   private static void showErrorDialog(String title, String html, String text)
   {
      // Create a dialog box and set the caption text
      final DialogBox dialogBox = new DialogBox(false, true);
      dialogBox.setText(title);

      // Create a table to layout the content
      VerticalPanel dialogContents = new VerticalPanel();
      dialogContents.setSpacing(4);
      dialogBox.setWidget(dialogContents);

      // Add either HTML or Text
      Widget details = html != null ? new HTML(html) : new Label(text);
      dialogContents.add(details);
      dialogContents.setCellHorizontalAlignment(details,
            HasHorizontalAlignment.ALIGN_CENTER);

      // Add a close button at the bottom of the dialog
      Button closeButton = new Button("Close",
            new ClickHandler()
            {
               public void onClick(ClickEvent event)
               {
                  dialogBox.hide();
               }
            });
      dialogContents.add(closeButton);
      dialogContents.setCellHorizontalAlignment(closeButton,
            HasHorizontalAlignment.ALIGN_CENTER);

      // final configuration and show the dialog box
      dialogBox.setGlassEnabled(true);
      dialogBox.setAnimationEnabled(true);
      dialogBox.center();
      dialogBox.show();
   }

   public interface Handler
   {
      void setupButtons(Button ok, Button cancel);
      void dialogClosed(boolean wasCancelled);
   }

   /**<!====== showInputDialog ===============================================>
      Displays a dialog box allowing input from the user.  The dialog contains
      both "OK" and "Cancel" buttons.  Before it is displayed,
      the {@link Handler#setupButtons} method is called (to allow "hotkeys" to
      be associated with the buttons).  When the dialog is closed by the user
      the {@link Handler#dialogClosed} method is called so that appropriate
      action can be taken.  This method immediately returns to the caller.
      <!      Name       Description>
      @param  titleBar   The dialog title.
      @param  content    The content (can be a panel with lots of controls).
      @param  handler    The handler for when the dialog is closed.
   <!=======================================================================>*/
   public static void showInputDialog(String titleBar, Widget content, Handler handler)
   {
      // Create a dialog box and set the caption text
      final DialogBox dialogBox = new DialogBox(false, true);
      dialogBox.setText(titleBar);

      // Create a table to layout the content
      VerticalPanel dialogContents = new VerticalPanel();
      dialogContents.setSpacing(4);
      dialogContents.add(content);

      // Add Ok and Cancel buttons at the bottom of the dialog
      Widget buttonPanel = createButtonPanel(dialogBox, handler);
      dialogContents.add(buttonPanel);
      dialogContents.setCellHorizontalAlignment(buttonPanel, HasHorizontalAlignment.ALIGN_CENTER);

      // final configuration and show the dialog box
      dialogBox.setWidget(dialogContents);
      dialogBox.setGlassEnabled(true);
      dialogBox.setAnimationEnabled(true);
      dialogBox.center();
      dialogBox.show();
   }

   private static Widget createButtonPanel(DialogBox dialogBox, Handler handler)
   {
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setSpacing(15);
      Button okButton = createButton("OK", dialogBox, false, handler);
      buttonPanel.add(okButton);
      Button cancelButton = createButton("Cancel", dialogBox, true, handler);
      buttonPanel.add(cancelButton);
      handler.setupButtons(okButton, cancelButton);
      return buttonPanel;
   }

   private static Button createButton(String label, final DialogBox dialogBox, final boolean isCancel, final Handler handler)
   {
      return new Button(label, new ClickHandler()
      {
         public void onClick(ClickEvent event)
         {
            dialogBox.hide();
            handler.dialogClosed(isCancel);
         }
      });
   }
}

