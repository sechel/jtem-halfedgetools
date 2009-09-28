package de.jtem.halfedgetools.functional.circlepattern;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.log;


/**
 * Evaluate Clausens integral
 * <p>
 * Copyright 2005 <a href="http://www.math.tu-berlin.de/~springb/">Boris Springborn</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Boris Springborn
 */
public class Clausen {

	private static final double TWO_PI = 2 * Math.PI;
	private static final double MINUS_LOG_2 = Math.log(0.5);
	private static final double BORDERLINE = 2.0944;
	
	private Clausen() {}
	
	public static double lob(double x) {
		return (0 <= x && x <= PI) ? Clausen.clausen2(2*x)/2 : -1e10;
	}
	
	public static double clausen(double x) {
		x = Math.IEEEremainder(x, TWO_PI);
		if (x == 0.0) {
			return 0.0;
		}
		if (Math.abs(x) <= BORDERLINE) {
			final double xx = x * x;
			return ((((((((((((
					  2.3257441143020875e-22 * xx
					+ 1.0887357368300848e-20) * xx 
					+ 5.178258806090624e-19) * xx
					+ 2.5105444608999545e-17) * xx
					+ 1.2462059912950672e-15) * xx
					+ 6.372636443183181e-14) * xx
					+ 3.387301370953521e-12) * xx
					+ 1.8978869988971e-10) * xx
					+ 1.1482216343327455e-8) * xx
					+ 7.873519778281683e-7) * xx
					+ 0.00006944444444444444) * xx
					+ 0.013888888888888888) * xx 
					- Math.log(Math.abs(x)) + 1.0) * x;
		}
		x += ((x > 0.0) ? - Math.PI : Math.PI);
		final double xx = x * x;
		return ((((((((((((  
				  3.901950904063069e-15 * xx
				+ 4.566487567193635e-14) * xx
				+ 5.429792727596476e-13) * xx
				+ 6.5812165661369675e-12) * xx
				+ 8.167010963952222e-11) * xx
				+ 1.0440290284867003e-9) * xx
				+ 1.3870999114054669e-8) * xx
				+ 1.941538399871733e-7) * xx
				+ 2.927965167548501e-6) * xx
				+ 0.0000496031746031746) * xx
				+ 0.0010416666666666667) * xx
				+ 0.041666666666666664) * xx 
				+ MINUS_LOG_2) * x;
	}
	
	
	

	
	private static final double M_PI = 3.14159265358979323846264338328;
	private static final double M_LN2 = 0.693147180559945309417232121458;
	private static int nclpi6 = 0, nclpi2 = 0, ncl5pi6 = 0;
	
	
	public static double clausen2( double x ) {
	  /*
	   * right half (Pi <= x < 2 Pi)
	   */
	  int rh = 0;
	  double f;

	  /*
	   * get to canonical interval
	   */
	  if( ( x = x % (2 * M_PI) ) < 0 ){
	    x += ( 2 * M_PI );
	  }
	  if( x > M_PI ){
	    rh = 1;
	    x = ( 2 * M_PI ) - x;
	  }

	  if( x == 0 ){
	    f = x;
	  }else if( x <= ( M_PI / 3 ) ){
	    f = csevl( x * ( 6 / M_PI ) - 1, clpi6, nclpi6 ) * x
	      - x * log( x );
	  }else if( x <= ( 2 * M_PI / 3 ) ){
	    f = csevl( x * ( 3 / M_PI ) - 1, clpi2, nclpi2 ) * x
	      - x * log( x );
	  }else{ /* x <= Pi */
	    f = ( M_LN2 -
		 csevl( 5 - x * ( 6 / M_PI ), cl5pi6, ncl5pi6 ) ) *
		   ( M_PI - x );
	  }

	  return (rh != 0) ? -f : f;
	}
	

	private static final double[] mach = 
	{
		2.2250738585072014e-308,
		1.7976931348623157e+308,
		1.1102230246251565e-16,
		2.2204460492503131e-16,
		3.0102999566398120e-01
	};
	/*
	 * chebyshev expansion of `cl(t)/t + log(t)' around Pi/6
	 * accurate to 20 decimal places
	 */
	static	double[]	clpi6 =
	{
	  2*1.0057346496467363858,
	  .0076523796971586786263,
	  .0019223823523180480014,
	  .53333368801173950429e-5,
	  .68684944849366102659e-6,
	  .63769755654413855855e-8,
	  .57069363812137970721e-9,
	  .87936343137236194448e-11,
	  .62365831120408524691e-12,
	  .12996625954032513221e-13,
	  .78762044080566097484e-15,
	  .20080243561666612900e-16,
	  .10916495826127475499e-17,
	  .32027217200949691956e-19
	  };

	/*
	 * chebyshev expansion of cl(t/2)/t + log(t)' around Pi/2
	 * accurate to 20 decimal places
	 */
	static	double[]	clpi2 =
	{
	  2*.017492908851746863924+2*1.0057346496467363858,
	  .023421240075284860656+.0076523796971586786263,
	  .0060025281630108248332+.0019223823523180480014,
	  .000085934211448718844330+.53333368801173950429e-5,
	  .000012155033501044820317+.68684944849366102659e-6,
	  .46587486310623464413e-6+.63769755654413855855e-8,
	  .50732554559130493329e-7+.57069363812137970721e-9,
	  .28794458754760053792e-8+.87936343137236194448e-11,
	  .27792370776596244150e-9+.62365831120408524691e-12,
	  .19340423475636663004e-10+.12996625954032513221e-13,
	  .17726134256574610202e-11+.78762044080566097484e-15,
	  .13811355237660945692e-12+.20080243561666612900e-16,
	  .12433074161771699487e-13+.10916495826127475499e-17,
	  .10342683357723940535e-14+.32027217200949691956e-19,
	  .92910354101990447850e-16,
	  .80428334724548559541e-17,
	  .72598441354406482972e-18,
	  .64475701884829384587e-19,
	  .58630185185185185187e-20
	  };

	/*
	 * chebyshev expansion of `-cl(Pi-t)/(Pi-t) + log(2)' around 5Pi/6
	 * accurate to 20 decimal places
	 */
	static	double[]	cl5pi6 =
	{
	  2*.017492908851746863924,
	  .023421240075284860656,
	  .0060025281630108248332,
	  .000085934211448718844330,
	  .000012155033501044820317,
	  .46587486310623464413e-6,
	  .50732554559130493329e-7,
	  .28794458754760053792e-8,
	  .27792370776596244150e-9,
	  .19340423475636663004e-10,
	  .17726134256574610202e-11,
	  .13811355237660945692e-12,
	  .12433074161771699487e-13,
	  .10342683357723940535e-14,
	  .92910354101990447850e-16,
	  .80428334724548559541e-17,
	  .72598441354406482972e-18,
	  .64475701884829384587e-19,
	  .58630185185185185187e-20
	  };

	/*
	 * evaluate a chebyshev series
	 * adapted from fortran csevl
	 */
	private static double csevl( double x, double[] cs, int n ) {
	  double b2 = 0, b1 = 0, b0 = 0, twox = 2 * x;

	  while( n-- > 0 ){
	    b2 = b1;
	    b1 = b0;
	    b0 = twox * b1 - b2 + cs[n];
	  }

	  return .5 * ( b0 - b2 );
	}
	
	
	static int inits( double[] series, int n, double eta ) {
	  double err = 0;

	  while( err <= eta && (n-- != 0) ){
	    err += abs( series[n] );
	  }

	  return n++;
	}
	
	
	static {
	    nclpi6 = inits( clpi6, clpi6.length, mach[2] / 10 );
	    nclpi2 = inits( clpi2, clpi2.length, mach[2] / 10 );
	    ncl5pi6 = inits( cl5pi6, cl5pi6.length, mach[2] / 10 );
	}
	
	
}

