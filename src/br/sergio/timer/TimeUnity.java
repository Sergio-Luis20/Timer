package br.sergio.timer;

/**
 * Enum que contém as principais unidades de medida de
 * tempo que podem ser usadas pela classe Timer.
 * @author Sergio Luis
 *
 */
public enum TimeUnity {
	
	NANOSECOND(0.000000001),
	MICROSECOND(0.000001),
	MILLISECOND(0.001),
	SECOND(1),
	MINUTE(60),
	HOUR(3600),
	DAY(86400);
	
	private double relationWithSecond;
	
	private TimeUnity(double relationWithSecond) {
		this.relationWithSecond = relationWithSecond;
	}
	
	/**
	 * @return a equivalência entre esta unidade de tempo e o segundo.
	 */
	public double getRelationWithSecond() {
		return relationWithSecond;
	}
}
