package org.eclipse.swt.examples.ole.win32;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved
 */

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.ole.win32.*;

/**
 * Wrapper for an OleAutomation object used to send commands
 * to a Win32 "Shell.Explorer" OLE control.
 * 
 * Instances of this class manage the setup, typical use and teardown of
 * a simple web browser.
 */
class OleWebBrowser {
	/* See the Windows Platform SDK documentation for more information about the
	 * OLE control used here and its usage.
	 */
	// Generated from typelib filename: shdocvw.dll

	// Constants for WebBrowser CommandStateChange
	public static final int CSC_UPDATECOMMANDS = -1;
	public static final int CSC_NAVIGATEFORWARD = 1;
	public static final int CSC_NAVIGATEBACK = 2;

	// COnstants for Web Browser ReadyState
	public static final int READYSTATE_UNINITIALIZED = 0;
	public static final int READYSTATE_LOADING = 1;
	public static final int READYSTATE_LOADED = 2;
	public static final int READYSTATE_INTERACTIVE = 3;
	public static final int READYSTATE_COMPLETE = 4;
	
	// Web Browser Control Events 
	public static final int BeforeNavigate        = 100; // Fired when a new hyperlink is being navigated to.
	public static final int NavigateComplete      = 101; // Fired when the document being navigated to becomes visible and enters the navigation stack.
	public static final int StatusTextChange      = 102; // Statusbar text changed.
	public static final int ProgressChange        = 108; // Fired when download progress is updated.
	public static final int DownloadComplete      = 104; // Download of page complete.
	public static final int CommandStateChange    = 105; // The enabled state of a command changed
	public static final int DownloadBegin         = 106; // Download of a page started.
	public static final int NewWindow             = 107; // Fired when a new window should be created.
	public static final int TitleChange           = 113; // Document title changed.
	public static final int FrameBeforeNavigate   = 200; // Fired when a new hyperlink is being navigated to in a frame.
	public static final int FrameNavigateComplete = 201; // Fired when a new hyperlink is being navigated to in a frame.
	public static final int FrameNewWindow        = 204; // Fired when a new window should be created.
	public static final int Quit                  = 103; // Fired when application is quiting.
	public static final int WindowMove            = 109; // Fired when window has been moved.
	public static final int WindowResize          = 110; // Fired when window has been sized.
	public static final int WindowActivate        = 111; // Fired when window has been activated.
	public static final int PropertyChange        = 112; // Fired when the PutProperty method has been called.

	// Web Browser properties
	public static final int DISPID_READYSTATE = -525;
	 
	// Keep track of the whether it is possible to navigate in the forward and backward directions
	private boolean backwardEnabled;
	private boolean forwardEnabled;

	private OleAutomation  oleAutomation;
	private OleControlSite oleControlSite;

	/**
	 * Creates a Web browser control.
	 * <p>
	 * Typical use:<br>
	 * <code>
	 * OleControlSite oleControlSite = new OleControlSite(oleFrame, style, "Shell.Explorer");<br>
	 * OleAutomation oleAutomation = new OleAutomation(oleControlSite);<br>
	 * OleWebBrowser webBrowser = new OleWebBrowser(oleControlSite, oleAutomation);<br>
	 * </code>
	 * 
     * @param oleAutomation the OleAutomation object for this control.
     * @param oleControlSite the OleControlSite object for this control.
	 */
	public OleWebBrowser(OleAutomation oleAutomation, OleControlSite oleControlSite) {
		this.oleAutomation = oleAutomation;
		this.oleControlSite = oleControlSite;
	
		backwardEnabled = false;
		forwardEnabled = false;
		
		// Listen for changes to the Command States
		oleControlSite.addEventListener(OleWebBrowser.CommandStateChange, new OleListener() {
			public void handleEvent(OleEvent event) {
				switch (event.type){
				case (OleWebBrowser.CommandStateChange) :
					int command = 0;
					boolean enabled = false;
					
					Variant varResult = event.arguments[0];
					if (varResult != null){
						command = varResult.getInt();
					}
				
					varResult = event.arguments[1];
					if (varResult != null){
						enabled = varResult.getBoolean();
					}
				
					if (command == CSC_NAVIGATEBACK) 	backwardEnabled = enabled;
					if (command == CSC_NAVIGATEFORWARD) forwardEnabled = enabled;		
					return;
				}
			}
		});
	}
	
	/**
	 * Activates the web browser control.
	 */
	public void activate() {
		if (oleControlSite == null || oleControlSite.isDisposed()) return;
		oleControlSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
	}
	
	/**
	 * Deactivates the web browser control.
	 */
	public void deactivate() {
		if (oleControlSite == null || oleControlSite.isDisposed()) return;
		oleControlSite.deactivateInPlaceClient();
	}
	
	/**
	 * Disposes of the Web browser control.
	 */
	public void dispose() {
		if (oleAutomation != null) oleAutomation.dispose();
		oleAutomation = null;
	}
	
	/*
	 * Interact with the Control via OLE Automation
	 * 
	 * Note: You can hard code the DISPIDs if you know them beforehand
	 *       this is of course the fastest way, but you increase coupling
	 *       to the control.
	 */
	 
	/**
	 * Returns the current web page title.
	 * 
	 * @return the current web page title String
	 */
	public String getLocationName() {
		// dispid=210, type=PROPGET, name="LocationName"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"LocationName"}); 
		int dispIdMember = rgdispid[0];
		Variant pVarResult = oleAutomation.getProperty(dispIdMember);
		if (pVarResult == null) return null;
		return pVarResult.getString();
	}
	
	/**
	 * Returns the current URL.
	 * 
	 * @return the current URL String
	 */
	public String getLocationURL() {
		// dispid=211, type=PROPGET, name="LocationURL"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"LocationURL"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.getProperty(dispIdMember);
		if (pVarResult == null) return null;
		return pVarResult.getString();
	}
	
	/**
	 * Returns the current state of the control.
	 * 
	 * @return the current state of the control, one of:
	 *         READYSTATE_UNINITIALIZED;
	 *         READYSTATE_LOADING;
	 *         READYSTATE_LOADED;
	 *         READYSTATE_INTERACTIVE;
	 *         READYSTATE_COMPLETE.
	 */
	public int getReadyState() {
		// dispid=4294966771, type=PROPGET, name="ReadyState"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"ReadyState"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.getProperty(dispIdMember);
		if (pVarResult == null) return -1;
		return pVarResult.getInt();
	}
	
	/**
	 * Navigates backwards through previously visited web sites.
	 * 
	 * @return the platform-defined result code for the "GoBack" method invocation
	 */
	public int doGoBack() {
		if (!backwardEnabled) return OLE.S_FALSE;
	
		// dispid=100, type=METHOD, name="GoBack"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"GoBack"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.invoke(dispIdMember);
		if (pVarResult == null) return 0;
		return pVarResult.getInt();
	}
	
	/**
	 * Navigates backwards through previously visited web sites.
	 * 
	 * @return the platform-defined result code for the "GoForward" method invocation
	 */
	public int doGoForward() {
		if (!forwardEnabled) return OLE.S_FALSE;
	
		// dispid=101, type=METHOD, name="GoForward"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"GoForward"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.invoke(dispIdMember);
		if (pVarResult == null) return 0;
		return pVarResult.getInt();
	}
	
	/**
	 * Navigates to home page.
	 *
	 * @return the platform-defined result code for the "GoHome" method invocation
	 */
	public int doGoHome() {
		// dispid=102, type=METHOD, name="GoHome"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"GoHome"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.invoke(dispIdMember);
		if (pVarResult == null) return 0;
		return pVarResult.getInt();
	}
	
	/**
	 * Navigates to user-specified Web search gateway.
	 *
	 * @return the platform-defined result code for the "GoSearch" method invocation
	 */
	public int doGoSearch() {
		// dispid=103, type=METHOD, name="GoSearch"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"GoSearch"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.invoke(dispIdMember);
		if (pVarResult == null) return 0;
		return pVarResult.getInt();
	}
	
	/**
	 * Navigates to a particular URL.
	 *
	 * @return the platform-defined result code for the "Navigate" method invocation
	 */
	public int doNavigate(String url) {
		// dispid=104, type=METHOD, name="Navigate"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"Navigate", "URL"}); 
		int dispIdMember = rgdispid[0];
		
		Variant[] rgvarg = new Variant[1];
		rgvarg[0] = new Variant(url);
		int[] rgdispidNamedArgs = new int[1];
		rgdispidNamedArgs[0] = rgdispid[1]; // identifier of argument
		Variant pVarResult = oleAutomation.invoke(dispIdMember, rgvarg, rgdispidNamedArgs);
	
		if (pVarResult == null) return 0;
		return pVarResult.getInt();
	}
	
	/**
	 * Refreshes the currently viewed page.
	 *
	 * @return the platform-defined result code for the "Refresh" method invocation
	 */
	public void doRefresh(){
		// dispid= 4294966746, type=METHOD, name="Refresh"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"Refresh"}); 
		int dispIdMember = rgdispid[0];
		
		oleAutomation.invokeNoReply(dispIdMember);
	}
	
	/**
	 * Aborts loading of the currnet page.
	 *
	 * @return the platform-defined result code for the "Stop" method invocation
	 */
	public void doStop() {
		// dispid=106, type=METHOD, name="Stop"
		int[] rgdispid = oleAutomation.getIDsOfNames(new String[]{"Stop"}); 
		int dispIdMember = rgdispid[0];
		
		Variant pVarResult = oleAutomation.invoke(dispIdMember);
	}	
}
