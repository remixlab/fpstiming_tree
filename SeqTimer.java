/*********************************************************************************
 * FPSTiming
 * Copyright (c) 2014 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 *     
 * All rights reserved. Library that eases the creation of interactive
 * scenes, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 *********************************************************************************/
package remixlab.fpstiming;

/**
 * Sequential timers are single-threaded timers handled by a TimingHandler.
 */
public class SeqTimer implements Timable {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + (int) (counter ^ (counter >>> 32));
		result = prime * result + (int) (prd ^ (prd >>> 32));
		result = prime * result + (runOnlyOnce ? 1231 : 1237);
		result = prime * result + (int) (startTime ^ (startTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeqTimer other = (SeqTimer) obj;
		if (active != other.active)
			return false;
		if (counter != other.counter)
			return false;
		if (prd != other.prd)
			return false;
		if (runOnlyOnce != other.runOnlyOnce)
			return false;
		if (startTime != other.startTime)
			return false;
		return true;
	}

	protected TimingHandler handler;
	protected boolean active;
	protected boolean runOnlyOnce;
	private long counter;
	private long prd;
	private long startTime;

	/**
	 * Defines a single shot sequential (single-threaded) timer.
	 * 
	 * @param h timing handler owner
	 */
	public SeqTimer(TimingHandler h) {
		this(h, false);
	}

	/**
	 * Defines a sequential (single-threaded) timer.
	 * 
	 * @param h timing handler owner
	 * @param singleShot
	 */
	public SeqTimer(TimingHandler h, boolean singleShot) {
		handler = h;
		runOnlyOnce = singleShot;
		create();
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#cancel()
	 */
	@Override
	public void cancel() {
		stop();
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#create()
	 */
	@Override
	public void create() {
		inactivate();
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#run(long)
	 */
	@Override
	public void run(long period) {
		prd = period;
		run();
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#run()
	 */
	@Override
	public void run() {
		if (prd <= 0)
			return;
		inactivate();
		counter = 1;
		active = true;
		startTime = System.currentTimeMillis();
	}

	@Override
	public void stop() {
		inactivate();
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#isActive()
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	// others

	/**
	 * Deactivates the SeqTimer.
	 */
	public void inactivate() {
		active = false;
	}

	/**
	 * Returns {@code true} if the timer was triggered at the given frame.
	 * <p>
	 * <b>Note:</b> You should not call this method since it's done by the
	 * timing handler (see {@link remixlab.fpstiming.TimingHandler#handle()}).
	 */
	public boolean trigggered() {
		if (!active)
			return false;

		long elapsedTime = System.currentTimeMillis() - startTime;

		float timePerFrame = (1 / handler.frameRate()) * 1000;
		long threshold = counter * prd;

		boolean result = false;
		if (threshold >= elapsedTime) {
			long diff = elapsedTime + (long) timePerFrame - threshold;
			if (diff >= 0) {
				if ((threshold - elapsedTime) < diff) {
					result = true;
				}
			}
		} else {
			result = true;
		}

		if (result) {
			counter++;
			/**
			 * if( prd < timePerFrame )
			 * System.out.println("Your current frame rate (~" +
			 * scene.frameRate() + " fps) is not high enough " +
			 * "to run the timer and reach the specified " + prd +
			 * " ms period, " + timePerFrame +
			 * " ms period will be used instead. If you want to sustain a lower timer "
			 * + "period, define a higher frame rate (minimum of " + 1000f/prd +
			 * " fps) " +
			 * "before running the timer (you may need to simplify your drawing to achieve it.)"
			 * ); //
			 */
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#period()
	 */
	@Override
	public long period() {
		return prd;
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#setPeriod(long)
	 */
	@Override
	public void setPeriod(long period) {
		prd = period;
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#isSingleShot()
	 */
	@Override
	public boolean isSingleShot() {
		return runOnlyOnce;
	}

	/*
	 * (non-Javadoc)
	 * @see remixlab.fpstiming.Timable#setSingleShot(boolean)
	 */
	@Override
	public void setSingleShot(boolean singleShot) {
		runOnlyOnce = singleShot;
	}
}
