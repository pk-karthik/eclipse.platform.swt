package org.eclipse.swt.examples.javaviewer;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
*/

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.io.*;
import java.text.*;

/**
*/
public class JavaViewer implements DisposeListener {  
	Shell shell;
	StyledText text;
	JavaLineStyler lineStyler = new JavaLineStyler();
	static ResourceBundle resources = ResourceBundle.getBundle("examples_javaviewer");
	
public void close () {
	if (shell != null && !shell.isDisposed ()) 
		shell.dispose ();
	lineStyler.disposeColors();
}
public void widgetDisposed (DisposeEvent event) {
	text.removeLineStyleListener(lineStyler);
	text.removeDisposeListener(this);
}

Menu createFileMenu() {
	Menu bar = shell.getMenuBar ();
	Menu menu = new Menu (bar);
	MenuItem item;

	// Open 
	item = new MenuItem (menu, SWT.CASCADE);
	item.setText (resources.getString("Open_menuitem"));
	item.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			openFile();
		}
	});

	// Close
	item = new MenuItem (menu, SWT.PUSH);
	item.setText (resources.getString("Close_menuitem"));
	item.addSelectionListener (new SelectionAdapter () {
		public void widgetSelected (SelectionEvent e) {
			menuFileExit ();
		}
	});
	return menu;
}

void createMenuBar () {
	Menu bar = new Menu (shell, SWT.BAR);
	shell.setMenuBar (bar);

	MenuItem fileItem = new MenuItem (bar, SWT.CASCADE);
	fileItem.setText (resources.getString("File_menuitem"));
	fileItem.setMenu (createFileMenu ());

}

void createShell () {
	shell = new Shell ();
	shell.setText (resources.getString("Window_title"));	
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	shell.setSize(500, 400);
	shell.setLayout(layout);
}
void createStyledText() {
	text = new StyledText (shell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	GridData spec = new GridData();
	spec.horizontalAlignment = spec.FILL;
	spec.grabExcessHorizontalSpace = true;
	spec.verticalAlignment = spec.FILL;
	spec.grabExcessVerticalSpace = true;
	text.setLayoutData(spec);
	text.addLineStyleListener(lineStyler);
	text.addDisposeListener(this);
	text.setEditable(false);
	Color bg = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
	text.setBackground(bg);
}

void displayError(String msg) {
	MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
	box.setMessage(msg);
	box.open();
}

public static void main (String [] args) {
	JavaViewer example = new JavaViewer ();
	example.open ();
	example.run ();
	example.close ();
}

void openFile() {	
	final String textString;
	FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);

	fileDialog.setFilterExtensions(new String[] {"*.java", "*.*"});
	fileDialog.open();
	String name = fileDialog.getFileName();
	
	if ((name == null) || (name.length() == 0)) return;

	File file = new File(fileDialog.getFilterPath(), name);
	if (!file.exists()) {
		String message = MessageFormat.format(resources.getString("Err_file_no_exist"), new String[] {file.getName()});
		displayError(message);
		return;
	}

	try {
		FileInputStream stream= new FileInputStream(file.getPath());
		try {
			Reader in = new BufferedReader(new InputStreamReader(stream));
			char[] readBuffer= new char[2048];
			StringBuffer buffer= new StringBuffer((int) file.length());
			int n;
			while ((n = in.read(readBuffer)) > 0) {
				buffer.append(readBuffer, 0, n);
			}
			textString = buffer.toString();
			stream.close();
		}
		catch (IOException e) {
			// Err_file_io
			String message = MessageFormat.format(resources.getString("Err_file_io"), new String[] {file.getName()});
			displayError(message);
			return;
		}
	}
	catch (FileNotFoundException e) {
		String message = MessageFormat.format(resources.getString("Err_not_found"), new String[] {file.getName()});
		displayError(message);
		return;
	}	
	// Workaround for superfluous mouse move event that is being sent
	// by the WIN platform when a file is selected via a double click in
	// in the file dialog (PR 1G80JJV).  If the extra move event was not 
	// being generated, the asyncExec would not be necessary.
	Display display = text.getDisplay();
	display.asyncExec(new Runnable() {
		public void run() {
			text.setText(textString);
		}
	});	
	
	// parse the block comments up front since block comments can go across
	// lines - inefficient way of doing this
	lineStyler.parseBlockComments(textString);
}

void menuFileExit () {
	shell.close ();
}

public void open () {
	createShell ();
	createMenuBar ();
	createStyledText ();
	shell.open ();
}

public void run () {
	Display display = shell.getDisplay ();
	while (!shell.isDisposed ())
		if (!display.readAndDispatch ()) display.sleep ();
}


}
