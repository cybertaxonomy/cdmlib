package eu.etaxonomy.cdm.test.function;

import java.math.BigDecimal;

public class BigDecimalTesting {

	public static void main (String[] args){
		BigDecimal a = new BigDecimal(2.05);
//		a = a.round(MathContext.DECIMAL128);
		
		System.out.println(a.toPlainString());
		
//		a = a.multiply(BigDecimal);
		System.out.println(a.toPlainString());
		
		System.out.println(a.toString());
		
		System.out.println(a.toEngineeringString());
		BigDecimal b = a.setScale(7, BigDecimal.ROUND_HALF_DOWN);
		
		BigDecimal c = new BigDecimal(33.12345).setScale(7, BigDecimal.ROUND_HALF_UP);
		a = a.add(c);
		
		
		
		System.out.println(a.toPlainString());
		System.out.println(b.add(c).toPlainString());
		
		BigDecimal d = c.add(new BigDecimal(100));
		System.out.println(d.toPlainString());

		
	}
}
