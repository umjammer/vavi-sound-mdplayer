/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

/**
 * A flocking visualizer: one flock of boids per channel, crossing over each other and swelling on
 * every note.
 * <p>
 * It is fed by the same {@link vavi.sound.visualizer.fmdsp.FmDspDataSource} the FMDSP visualizer
 * takes, so anything that drives that one drives this one too, and everything it draws with is a
 * parameter in {@link vavi.sound.visualizer.boids.BoidsParams}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
package vavi.sound.visualizer.boids;
