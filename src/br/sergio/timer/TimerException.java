package br.sergio.timer;

/**
 * Classe que estende RuntimeException e é responsável
 * por parte dos throwables lançados na classe Timer.
 * @author Sergio Luis
 * 
 */
public class TimerException extends RuntimeException {
	
	private static final long serialVersionUID = -2937456870482955438L;
	
	public TimerException() {
		super();
	}
	
	public TimerException(String message) {
		super(message);
	}
	
	public TimerException(Throwable cause) {
		super(cause);
	}
	
	public TimerException(String message, Throwable cause) {
		super(message, cause);
	}
}
