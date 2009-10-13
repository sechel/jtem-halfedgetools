package de.jtem.halfedgetools.plugin;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.thoughtworks.xstream.XStream;

import de.jtem.halfedge.HalfEdgeDataStructure;


public class HalfedgeIO {
	
	static XStream xstream = new XStream();

	private static String readTextFile(String fullPathFilename) throws IOException {
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new FileReader(fullPathFilename));
				
		char[] chars = new char[1024];
		while( reader.read(chars) > -1){
			sb.append(String.valueOf(chars));	
		}

		reader.close();

		return sb.toString();
	}
	
	private static void writeTextFile(String contents, String fullPathFilename) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathFilename));
		writer.write(contents);
		writer.flush();
		writer.close();	
	}
	
	@SuppressWarnings("unchecked")
	public static
	< 
	HDS extends HalfEdgeDataStructure<?,?,?>
	> 
	HDS readHDS(String filename){
		String xml = null;
		try {
			xml = readTextFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		HDS heds = (HDS)xstream.fromXML(xml);
		return heds;
	}
	
	
	public static void writeHDS(HalfEdgeDataStructure<?,?,?> heds, String filename) {

		xstream.setMode(XStream.ID_REFERENCES);
		
		String xml = xstream.toXML(heds);
		try {
			writeTextFile(xml, filename);
		} catch (IOException e) {
			System.err.println("Could not write to file " + filename);
			e.printStackTrace();
		}
		
	}

}
