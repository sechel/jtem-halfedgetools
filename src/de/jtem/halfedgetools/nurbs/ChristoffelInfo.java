package de.jtem.halfedgetools.nurbs;

public class ChristoffelInfo {
	
	
	protected double [] Su;
	protected double [] Sv;
	protected double [] Suu;
	protected double [] Suv;
	protected double [] Svv;

	protected double G111;
	protected double G112;
	protected double G121;
	protected double G122;
	protected double G211;
	protected double G212;
	protected double G221;
	protected double G222;
	
	
	public ChristoffelInfo(){
		
	}
	
	public double[] getSu() {
		return Su;
	}


	public void setSu(double[] su) {
		Su = su;
	}


	public double[] getSv() {
		return Sv;
	}


	public void setSv(double[] sv) {
		Sv = sv;
	}


	public double[] getSuu() {
		return Suu;
	}


	public void setSuu(double[] suu) {
		Suu = suu;
	}


	public double[] getSuv() {
		return Suv;
	}


	public void setSuv(double[] suv) {
		Suv = suv;
	}


	public double[] getSvv() {
		return Svv;
	}


	public void setSvv(double[] svv) {
		Svv = svv;
	}


	public double getG111() {
		return G111;
	}


	public void setG111(double g111) {
		G111 = g111;
	}


	public double getG112() {
		return G112;
	}


	public void setG112(double g112) {
		G112 = g112;
	}


	public double getG121() {
		return G121;
	}


	public void setG121(double g121) {
		G121 = g121;
	}


	public double getG122() {
		return G122;
	}


	public void setG122(double g122) {
		G122 = g122;
	}


	public double getG221() {
		return G221;
	}
	
	public double getG211() {
		return G211;
	}

	public void setG211(double g211) {
		G211 = g211;
	}

	public double getG212() {
		return G212;
	}

	public void setG212(double g212) {
		G212 = g212;
	}




	public void setG221(double g221) {
		G221 = g221;
	}


	public double getG222() {
		return G222;
	}


	public void setG222(double g222) {
		G222 = g222;
	}
	
	
	

}
