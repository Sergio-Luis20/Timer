package br.sergio.timer;

import java.io.Serializable;

/**
 * Interface que representa a ação a ser executada
 * a cada ciclo de um cronômetro (Timer).
 * @author Sergio Luis
 *
 */
public interface TimerAction extends Serializable {
	
	/**
	 * Método que é invocado na execução de cada ciclo de um cronômetro.
	 * Representa a ação a ser feita.
	 * @param timer O cronômetro que está usando o método.
	 */
	void action(Timer timer);
}
