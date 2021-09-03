package br.sergio.timer;

import java.io.Serializable;

/**
 * Classe que representa um cronômetro e executa uma determinada
 * ação a cada ciclo que se passa. A thread principal desta classe
 * é daemon.
 * @author Sergio Luis
 * 
 */
public class Timer implements Runnable, Serializable {
	
	/**
	 * Constante que pode ser usada no construtor no parâmetro
	 * <code>totalCycles</code>. Caso ela de fato seja utilizada,
	 * o cronômetro executará <code>Long.MAX_VALUE</code> ciclos. 
	 */
	public static transient final int INFINITY = -1;
	private static final long serialVersionUID = 5894944399936982630L;
	protected TimeUnity unity;
	protected String name;
	protected Thread timer;
	private TimerAction action;
	private boolean running;
	private boolean isInfinity;
	private boolean daemon;
	private long[] delay;
	private long[] period;
	private long totalCycles;
	private long leftCycles;
	private long passedCycles;
	private long unitDelay;
	private long unitPeriod;
	
	/**
	 * Construtor.
	 * Aqui, como não há o parâmetro nome, ele por padrão será nulo. Se quiser definir um
	 * nome para este cronômetro, utilize o outro construtor ou use o método
	 * <code>setName(String name)</code> posteriormente.
	 * @param unity A unidade de tempo que será usada neste cronômetro.
	 * @param totalCycles O total de unidades (baseado no parâmetro <code>unity</code>) que o
	 * cronômetro contará até parar no momento em que ele iniciar. Para rodar o cronômetro
	 * infinitamente, use <code>Timer.INFINITY</code>.
	 * @param action A ação que será executada por este cronômetro a cada ciclo.
	 */
	public Timer(TimeUnity unity, long totalCycles, TimerAction action) {
		this(unity, null, totalCycles, action);
	}
	
	/**
	 * Construtor
	 * @param unity A unidade de tempo que será usada neste cronômetro.
	 * @param name O nome do cronômetro. Pode ser nulo.
	 * @param totalCycles O total de unidades (baseado no parâmetro <code>unity</code>) que o
	 * cronômetro contará até parar no momento em que ele iniciar. Para rodar o cronômetro
	 * infinitamente, use <code>Timer.INFINITY</code>.
	 * @param action A ação que será executada por este cronômetro a cada ciclo.
	 */
	public Timer(TimeUnity unity, String name, long totalCycles, TimerAction action) {
		setUnity(unity);
		setName(name);
		setTotalCycles(totalCycles);
		setAction(action);
	}
	
	/**
	 * Inicia o cronômetro com o período padrão de execução: 0.
	 * @param delay A quantidade de unidades que devem ser esperadas antes de
	 * iniciar a execução.
	 */
	public void startDelay(long delay) {
		start(delay, 0);
	}
	
	/**
	 * Inicia o cronômetro imediatamente.
	 * @param period A quantidade de unidades que devem ser aguardadas entre
	 * um ciclo e outro de execução.
	 */
	public void startPeriod(long period) {
		start(0, period);
	}
	
	/**
	 * Define o estado de daemon desta thread. Caso este estado não seja chamado
	 * e a thread seja executada, o padrão é false. Se a thread está em execução
	 * no momento em que este método é chamado, uma exception é lançada.
	 * 
	 * @throws IllegalThreadStateException se o cronômetro estiver em execução.
	 */
	public void setDaemon(boolean daemon) {
		if(timer == null) {
			return;
		}
		if(timer.isAlive()) {
			throw new IllegalThreadStateException("O cronômetro não pode mudar seu estado daemon se estiver em execução.");
		}
		this.daemon = daemon;
	}
	
	/**
	 * Diz se a thread deste cronômetro é daemon.
	 * 
	 * @see #java.lang.Thread.isDaemon()
	 * @return <code>true</code> se for daemon, <code>false</code> caso contrário.
	 */
	public boolean isDaemon() {
		return daemon;
	}
	
	/**
	 * Inicia a execução do cronômetro.
	 * @param delay A quantidade de unidades que devem ser esperadas antes de
	 * iniciar a execução.
	 * @param period A quantidade de unidades que devem ser aguardadas entre
	 * um ciclo e outro de execução.
	 */
	public synchronized void start(long delay, long period) {
		if(running) {
			return;
		}
		if(timer == null || !timer.isAlive()) {
			timer = new Thread(this);
			timer.setDaemon(daemon);
		}
		this.delay = getMillisAndNanos(delay, unity);
		this.period = getMillisAndNanos(period, unity);
		long millisToSeconds, nanosToSeconds;
		millisToSeconds = (long) (this.delay[0] * TimeUnity.MILLISECOND.getRelationWithSecond());
		nanosToSeconds = (long) (this.delay[1] * TimeUnity.NANOSECOND.getRelationWithSecond());
		unitDelay = (long) ((millisToSeconds + nanosToSeconds) / unity.getRelationWithSecond());
		millisToSeconds = (long) (this.period[0] * TimeUnity.MILLISECOND.getRelationWithSecond());
		nanosToSeconds = (long) (this.period[1] * TimeUnity.NANOSECOND.getRelationWithSecond());
		unitPeriod = (long) ((millisToSeconds + nanosToSeconds) / unity.getRelationWithSecond());
		running = true;
		timer.start();
	}
	
	/**
	 * Método que para a execução do cronômetro. Se ele não estiver em execução, não tem efeito algum.
	 * Atenção: não utilize este método dentro de <code>TimerAction.action(Timer timer)</code>, uma vez
	 * que pode causar um deadlock. Caso seja necessário parar este cronômetro pelo seu TimerAction,
	 * utilize o método <code>void Timer.stopInTimerAction()</code>.
	 */
	public synchronized void stop() {
		if(!running) {
			return;
		}
		leftCycles = 0;
		try {
			timer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Este método para a execução do cronômetro. Só deve ser executado dentro de um TimerAction,
	 * evitando, assim, deadlocks.
	 */
	public synchronized void stopInTimerAction() {
		if(!running) {
			return;
		}
		leftCycles = 0;
	}
	
	/**
	 * Reinicia o cronômetro com o mesmo delay e o mesmo período fornecidos no
	 * início da contagem. Se ele não estiver em execução, este método não tem
	 * efeito algum; nesse caso, é recomendável invocar um dos 3 métodos de 
	 * inicialização. Atenção: não utilize este método dentro do método
	 * <code>TimerAction.action(Timer timer)</code>, uma vez que pode causar
	 * um deadlock.
	 */
	public synchronized void restart() {
		if(running) {
			stop();
			long delay = unitDelay;
			long period = unitPeriod;
			start(delay, period);
		}
	}
	
	/**
	 * Método que executa os ciclos e a cada um invoca o método
	 * <code>TimerAction.action(Timer timer)</code> do objeto TimerAction provido
	 * pelo construtor.
	 */
	@Override
	public void run() {
		try {
			long milis = delay[0];
			int nanos = (int) delay[1];
			Thread.sleep(milis, nanos);
			while(leftCycles > 0) {
				try {
					action.action(this);
				} catch(Exception e) {
					e.printStackTrace();
					break;
				}
				if(leftCycles == 0) {
					break;
				}
				if(!isInfinity) {
					leftCycles--;
				}
				passedCycles++;
				Thread.sleep(milis, nanos);
			}
			reset();
		} catch (InterruptedException e1) {
			reset();
			e1.printStackTrace();
		}
	}
	
	/**
	 * Reseta os valores deste cronômetro para os padrões.
	 */
	private void reset() {
		delay = null;
		period = null;
		unitDelay = 0;
		unitPeriod = 0;
		running = false;
		leftCycles = totalCycles;
		passedCycles = 0;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(o instanceof Timer) {
			Timer timer = (Timer) o;
			boolean nameEquals;
			if(name == null && timer.name == null) {
				nameEquals = true;
			} else if(name != null && timer.name != null) {
				nameEquals = name.equals(timer.name);
			} else {
				nameEquals = false;
			}
			boolean totalCyclesEquals = totalCycles == timer.totalCycles;
			boolean unityEquals = unity == timer.unity;
			boolean actionEquals = action.equals(timer.action);
			return nameEquals && totalCyclesEquals && unityEquals && actionEquals;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int nameHash = name == null ? 1 : name.hashCode();
		int actionHash = action.hashCode();
		int unityHash = unity.hashCode();
		long zeta = (totalCycles == 0 ? 1 : totalCycles);
		long sum = (nameHash + actionHash + unityHash) * zeta;
		double sqrt = Math.sqrt(Math.abs(sum));
		return (int) (sqrt * zeta);
	}
	
	/**
	 * Transforma as unidades dadas num vetor long de tamanho 2, em que o primeiro elemento é
	 * a quantidade de milissegundos e o segundo é a quantidade de nanossegundos que sobraram
	 * para completar o próximo milissegundo (em outras palavras: o resto).
	 * @param units A quantidade de unidades.
	 * @param unity A unidade de tempo usada.
	 * @return O referido vetor.
	 */
	public static synchronized final long[] getMillisAndNanos(long units, TimeUnity unity) {
		if(units < 0) {
			throw new IllegalArgumentException("As unidades não podem ser menores que 0");
		}
		double seconds = units * unity.getRelationWithSecond();
		double nanos = seconds / TimeUnity.NANOSECOND.getRelationWithSecond();
		if(nanos < 0) {
			throw new TimerException("O valor das unidades ultrapassou o limite do long.");
		}
		long millis = 0;
		while(nanos >= 1000000) {
			millis++;
			nanos -= 1000000;
		}
		return new long[] {millis, (long) nanos};
	}
	
	/**
	 * @return <code>true</code> se este cronômetro estiver em execução; <code>false</code>
	 * caso contrário.
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * @return O nome deste cronômetro. Observação: ele pode ser nulo.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Define o nome deste cronômetro. Obs.: não tem efeito algum se ele estiver em
	 * execução. Pode ser nulo.
	 * @param name O novo nome.
	 */
	public void setName(String name) {
		if(running) {
			return;
		}
		this.name = name;
	}
	
	/**
	 * Retorna o objeto TimerAction que está sendo usado neste cronômetro.
	 * Lembrando que um TimerAction representa a ação que será executada a cada ciclo.
	 * @return o referido objeto.
	 */
	public TimerAction getAction() {
		return action;
	}
	
	/**
	 * Define um novo objeto TimerAction que será usado por este cronômetro a cada ciclo.
	 * Obs.: não tem efeito algum se ele estiver em execução. Não pode ser nulo.
	 * @param action A nova ação a ser executada a cada ciclo.
	 */
	public void setAction(TimerAction action) {
		if(running) {
			return;
		}
		if(action == null) {
			throw new NullPointerException("A ação não pode ser nula.");
		}
		this.action = action;
	}
	
	/**
	 * @return A unidade de tempo usada por este cronômetro.
	 */
	public TimeUnity getUnity() {
		return unity;
	}
	
	/**
	 * Define a unidade de tempo deste cronômetro. Obs.: não tem efeito algum se ele 
	 * estiver em execução.
	 * @param unity A nova unidade de tempo.
	 */
	public void setUnity(TimeUnity unity) {
		if(running) {
			return;
		}
		if(unity == null) {
			throw new NullPointerException("A unidade não pode ser nula.");
		}
		this.unity = unity;
	}
	
	/**
	 * @return O total de ciclos que este cronômetro usou para iniciar a contagem.
	 * Foi provido pelo construtor.
	 */
	public long getTotalCycles() {
		return totalCycles;
	}
	
	/**
	 * Define o total de ciclos que o cronômetro deve se basear para iniciar
	 * a contagem até ele zerar. Obs.: não tem efeito algum se ele estiver em execução.
	 * Não pode ser negativo.
	 * @param totalCycles O total de ciclos.
	 */
	public void setTotalCycles(long totalCycles) {
		if(running) {
			return;
		}
		if(totalCycles < 0 && totalCycles != INFINITY) {
			throw new IllegalArgumentException("O total de unidades não pode ser negativo.");
		} else if(totalCycles == INFINITY) {
			isInfinity = true;
			this.totalCycles = totalCycles = 1;
		} else {
			isInfinity = false;
			this.totalCycles = totalCycles;
		}
		this.leftCycles = totalCycles;
	}
	
	/**
	 * @return O total de ciclos que faltam para o cronômetro terminar a contagem.
	 * Se ele não estiver em execução, este método retorna o equivalente a
	 * <code>getTotalCycles()</code>.
	 */
	public long getLeftCycles() {
		return leftCycles;
	}
	
	/**
	 * @return O total de unidades que o usuário definiu para serem esperadas antes de
	 * iniciar a contagem do cronômetro. Se ele não estiver em execução, é retornado 0.
	 */
	public long getDelay() {
		return unitDelay;
	}
	
	/**
	 * @return O total de unidades que o usuário definiu para serem usadas como intervalo
	 * de tempo entre um ciclo e outro de execução deste cronômetro. Se ele não estiver em
	 * execução, é retornado 0.
	 */
	public long getPeriod() {
		return unitPeriod;
	}
	
	/**
	 * @return A quantidade de ciclos que o cronômetro já executou. Se ele não estiver em
	 * execução, é retornado 0.
	 */
	public long getPassedCycles() {
		return passedCycles;
	}
	
	/**
	 * @return O ciclo que está em execução atualmente. Equivale a
	 * <code>getPassedCycles() + 1</code>.
	 */
	public long getCurrentCycle() {
		return passedCycles + 1;
	}
	
	/**
	 * @return <code>true</code> se este cronômetro estiver programado para rodar
	 * infinitamente; <code>false</code> caso contrário.
	 */
	public boolean isInfinity() {
		return isInfinity;
	}
}
