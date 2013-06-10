/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universitaet Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools;

import javax.swing.UIManager;

import de.jreality.jogl.GLJPanelViewer;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.ConsolePlugin;
import de.jreality.ui.JRealitySplashScreen;
import de.jreality.util.SystemProperties;
import de.jtem.halfedgetools.plugin.HalfedgePluginFactory;
import de.jtem.jrworkspace.plugin.lnfswitch.LookAndFeelSwitch;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.CrossPlatformLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.NimbusLnF;
import de.jtem.jrworkspace.plugin.lnfswitch.plugin.SystemLookAndFeel;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;

public class JRHalfedgeViewer {
	
	public static void initHalfedgeFronted() {
		boolean overlaysSupported = false;
		try {
			overlaysSupported = supportsOverlay();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (overlaysSupported) {
			System.setProperty(
				SystemProperties.VIEWER,
				"de.jreality.jogl.GLJPanelViewer" + " " + 
				SystemProperties.VIEWER_DEFAULT_JOGL + " " + 
				SystemProperties.VIEWER_DEFAULT_SOFT
			);
		} else {
			System.setProperty(
				SystemProperties.VIEWER,
				SystemProperties.VIEWER_DEFAULT_JOGL + " " + 
				SystemProperties.VIEWER_DEFAULT_SOFT
			);
			System.err.println("Halfedge widget overlays not supported.");
		}
		UIManager.getDefaults().put("Slider.paintValue", false);
	}
	
	private static Boolean
		supportsOverlay = null;
	
	public static boolean supportsOverlay() {
		if (supportsOverlay == null) {
			new JRViewer(); 
			try {
				new GLJPanelViewer();
				supportsOverlay = true;
			} catch (Exception e) {
				supportsOverlay = false;
			}
		}
		return supportsOverlay;
	}
	
	
	private static void addLnFPlugins(JRViewer v) {
		v.registerPlugin(new LookAndFeelSwitch());
		v.registerPlugin(new CrossPlatformLnF());
		v.registerPlugin(new NimbusLnF());
		v.registerPlugin(new SystemLookAndFeel());
	}
	
	
	public static void main(String[] args) {
		SplashScreen splash = new JRealitySplashScreen();
		splash.setVisible(true);
		initHalfedgeFronted();
		JRViewer v = new JRViewer();
		v.setSplashScreen(splash);
		v.setPropertiesFile("JRHalfedgeViewer.xml");
		v.setPropertiesResource(JRHalfedgeViewer.class, "JRHalfedgeViewer.xml");
		v.addContentUI();
		v.addBasicUI();
		v.addContentSupport(ContentType.Raw);
		v.registerPlugins(HalfedgePluginFactory.createPlugins());
		v.registerPlugin(ConsolePlugin.class);
		v.getController().setManageLookAndFeel(false);
		addLnFPlugins(v);
		v.startup(); 
		splash.setVisible(false);
	}
}
