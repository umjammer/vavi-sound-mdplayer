/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.boids;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Every number {@link BoidsVisualizer} draws with, in one place.
 * <p>
 * The flock's look is a matter of taste and no single set of numbers is going to be the right one
 * first time, so nothing here is a constant: each parameter carries its own range and default,
 * which is what lets {@link BoidsControlPanel} build a slider for it without being told about it,
 * what lets a whole set be saved to and loaded from a properties file, and what makes adding a
 * parameter a matter of one enum line rather than an edit in three places.
 * <p>
 * Values are read every frame, so a change takes effect at once - the panel can be dragged while
 * the music plays.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-08-03 nsano initial version <br>
 */
public class BoidsParams {

    /** What a parameter is about; the control panel groups its sliders by this. */
    public enum Group {
        /** How a channel's own boids move and hold together. */
        FLOCK,
        /** How the channels' flocks travel the window and meet each other. */
        CROSS,
        /** How a note turns into the swell of a flock. */
        BEAT,
        /** How the window frame is met. */
        WALL,
        /** What it all looks like. */
        LOOK,
    }

    /**
     * One knob. {@code min}/{@code max} are what the slider spans, {@code def} the value a fresh
     * set starts at, and {@code decimals} how it is written down (0 means the value is a whole
     * number - a count, a toggle or a mode).
     */
    public enum Param {

        // ---- flock ----
        /** boids per channel */
        COUNT("boids / ch", Group.FLOCK, 1, 32, 7, 0),
        /** px/s a boid may fly at */
        MAX_SPEED("max speed", Group.FLOCK, 10, 900, 150, 0),
        /** px/s below which a boid is pushed back up, so nothing ever parks */
        MIN_SPEED("min speed", Group.FLOCK, 0, 400, 30, 0),
        /** px/s² cap on the whole steering sum */
        MAX_FORCE("max force", Group.FLOCK, 20, 4000, 700, 0),
        /** weight of the push away from crowded flockmates */
        SEPARATION("separation", Group.FLOCK, 0, 4, 1.3, 2),
        /** weight of the pull towards the flock's heading */
        ALIGNMENT("alignment", Group.FLOCK, 0, 4, 1.1, 2),
        /** weight of the pull towards the flock's middle */
        COHESION("cohesion", Group.FLOCK, 0, 4, 1.4, 2),
        /** px within which a flockmate is seen at all */
        NEIGHBOR_RADIUS("neighbour r", Group.FLOCK, 8, 400, 120, 0),
        /** px a boid wants to keep to itself; a beat blows this up */
        SEPARATION_RADIUS("personal r", Group.FLOCK, 2, 200, 16, 0),
        /** weight of the random wander that keeps the flock from settling */
        WANDER("wander", Group.FLOCK, 0, 4, 0.35, 2),

        // ---- crossing ----
        /** weight of the pull towards the nearest other channel's flock */
        CROSSOVER("cross pull", Group.CROSS, 0, 4, 0.85, 2),
        /**
         * weight of the sideways component of that pull. Attraction alone would let the flocks
         * merge and stay merged; a tangential share turns the meeting into a sweep through each
         * other and out the far side. Negative swirls the other way round.
         */
        CROSS_SWIRL("cross swirl", Group.CROSS, -3, 3, 1.15, 2),
        /** weight of the pull towards the flock's own travelling anchor */
        ORBIT_PULL("home pull", Group.CROSS, 0, 4, 0.9, 2),
        /** how fast that anchor travels; each channel gets its own two frequencies */
        ORBIT_SPEED("orbit speed", Group.CROSS, 0, 2, 0.16, 2),
        /** the share of the window the anchors range over */
        ORBIT_SPREAD("orbit spread", Group.CROSS, 0, 1, 0.85, 2),
        /** how far pitch lifts a flock's anchor up the window */
        KEY_LIFT("key lift", Group.CROSS, 0, 1, 0.3, 2),

        // ---- beat ----
        /** the rise in loudness, 0..1, that counts as a beat on its own */
        BEAT_SENSE("beat sense", Group.BEAT, 0.01, 1, 0.11, 2),
        /** 1: a new key counts as a beat too, which is what a quiet legato part has */
        KEY_BEAT("key = beat", Group.BEAT, 0, 1, 1, 0),
        /** seconds the swell takes to arrive */
        PULSE_ATTACK("attack", Group.BEAT, 0.005, 0.5, 0.04, 3),
        /** seconds the swell takes to fall away */
        PULSE_DECAY("decay", Group.BEAT, 0.05, 4, 0.5, 2),
        /** how much bigger a boid is drawn at full swell */
        PULSE_SIZE("pulse size", Group.BEAT, 0, 8, 1.8, 2),
        /** how much wider the flock spreads at full swell */
        PULSE_SPREAD("pulse spread", Group.BEAT, 0, 6, 1.2, 2),
        /** how much faster the flock flies at full swell */
        PULSE_SPEED("pulse speed", Group.BEAT, 0, 4, 1.1, 2),
        /** px/s thrown outwards from the flock's middle at the moment of the beat */
        PULSE_KICK("pulse kick", Group.BEAT, 0, 800, 150, 0),

        // ---- walls ----
        /** what a boid keeps of its speed when it bounces off the frame */
        BOUNCE("bounce", Group.WALL, 0.2, 1.4, 1.0, 2),
        /** px from the frame at which a boid starts turning back; 0 means bounce only */
        WALL_MARGIN("wall margin", Group.WALL, 0, 250, 0, 0),
        /** weight of that turn */
        WALL_STEER("wall steer", Group.WALL, 0, 8, 2.0, 2),

        // ---- look ----
        /** px radius of a resting boid */
        BOID_SIZE("size", Group.LOOK, 1, 40, 4, 1),
        /** 0: dot, 1: dart pointing where it flies, 2: comet */
        SHAPE("shape", Group.LOOK, 0, 2, 1, 0),
        /** how much of the previous frame survives, per 1/60 s */
        TRAIL("trail", Group.LOOK, 0, 0.99, 0.8, 2),
        /** halo around each boid */
        GLOW("glow", Group.LOOK, 0, 1, 0.6, 2),
        /** how visible the threads between flockmates are */
        LINK_ALPHA("link alpha", Group.LOOK, 0, 1, 0.28, 2),
        /** px within which flockmates are threaded together */
        LINK_RADIUS("link r", Group.LOOK, 0, 300, 90, 0),
        /** where C sits on the colour wheel */
        HUE_OFFSET("hue offset", Group.LOOK, 0, 1, 0, 2),
        /** how much of the wheel the twelve semitones cover */
        HUE_SPAN("hue span", Group.LOOK, 0, 2, 1, 2),
        /** colour saturation */
        SATURATION("saturation", Group.LOOK, 0, 1, 0.85, 2),
        /** how much a high octave brightens the colour */
        OCTAVE_BRIGHT("octave light", Group.LOOK, 0, 1, 0.35, 2),
        /** how much the channel's level decides its opacity */
        LEVEL_ALPHA("level alpha", Group.LOOK, 0, 1, 0.7, 2),
        /** how far a resting or muted channel dims towards nothing */
        IDLE_FADE("idle fade", Group.LOOK, 0, 1, 0.78, 2),
        /** 1: write each flock's channel name beside it */
        LABELS("labels", Group.LOOK, 0, 1, 1, 0);

        /** shown next to the slider */
        public final String label;
        public final Group group;
        public final double min, max, def;
        /** digits shown; 0 marks a value that is really an integer */
        public final int decimals;

        Param(String label, Group group, double min, double max, double def, int decimals) {
            this.label = label;
            this.group = group;
            this.min = min;
            this.max = max;
            this.def = def;
            this.decimals = decimals;
        }

        /** the key this parameter is saved under */
        String key() {
            return "boids." + name().toLowerCase().replace('_', '.');
        }

        /** whether the value is a count, a mode or a toggle rather than a measurement */
        public boolean isIntegral() {
            return decimals == 0;
        }
    }

    private final double[] values = new double[Param.values().length];

    private final List<Runnable> listeners = new ArrayList<>();

    /** A set at its defaults. */
    public BoidsParams() {
        reset();
    }

    /** Puts every parameter back to its default. */
    public void reset() {
        for (Param p : Param.values()) values[p.ordinal()] = p.def;
        fire();
    }

    public double get(Param p) {
        return values[p.ordinal()];
    }

    public int getInt(Param p) {
        return (int) Math.round(values[p.ordinal()]);
    }

    public boolean getFlag(Param p) {
        return getInt(p) != 0;
    }

    /** Sets one parameter, clamped to its range. */
    public void set(Param p, double value) {
        double v = Math.clamp(value, p.min, p.max);
        if (p.isIntegral()) v = Math.round(v);
        if (v == values[p.ordinal()]) return;
        values[p.ordinal()] = v;
        fire();
    }

    /** Takes every value from {@code other}. */
    public void copyFrom(BoidsParams other) {
        System.arraycopy(other.values, 0, values, 0, values.length);
        fire();
    }

    /**
     * Called whenever any value changes, so that a control panel showing these can follow a preset
     * being applied or a file being loaded.
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    private void fire() {
        listeners.forEach(Runnable::run);
    }

    // ----- presets -----

    private static final Map<String, Map<Param, Double>> presets = new LinkedHashMap<>();

    static {
        presets.put("default", Map.of());
        presets.put("calm", Map.of(
                Param.COUNT, 5d, Param.MAX_SPEED, 90d, Param.WANDER, 0.25,
                Param.PULSE_KICK, 90d, Param.PULSE_SIZE, 1.6, Param.TRAIL, 0.9,
                Param.CROSS_SWIRL, 0.6, Param.ORBIT_SPEED, 0.08, Param.BOID_SIZE, 7d));
        presets.put("swarm", Map.of(
                Param.COUNT, 20d, Param.BOID_SIZE, 3d, Param.MAX_SPEED, 240d,
                Param.SEPARATION_RADIUS, 14d, Param.NEIGHBOR_RADIUS, 70d,
                Param.LINK_ALPHA, 0.12, Param.LINK_RADIUS, 34d, Param.TRAIL, 0.7));
        // a big kick, but a short one and a strong gathering after it: a flock that is thrown
        // outwards faster than it can come back never comes back at all, and ends up smeared
        // along the frame for the rest of the song
        presets.put("fireworks", Map.of(
                Param.COUNT, 14d, Param.PULSE_KICK, 380d, Param.PULSE_SIZE, 4d,
                Param.PULSE_SPREAD, 1.6, Param.PULSE_DECAY, 0.3, Param.COHESION, 2.4,
                Param.SEPARATION, 0.8, Param.TRAIL, 0.88, Param.SHAPE, 2d, Param.LINK_ALPHA, 0d));
        presets.put("ribbons", Map.of(
                Param.COUNT, 3d, Param.SHAPE, 2d, Param.TRAIL, 0.95, Param.BOID_SIZE, 4d,
                Param.MAX_SPEED, 300d, Param.MIN_SPEED, 150d, Param.WANDER, 0.2,
                Param.CROSSOVER, 1.4, Param.CROSS_SWIRL, 2.2, Param.LINK_ALPHA, 0d));
        presets.put("chaos", Map.of(
                Param.COUNT, 12d, Param.WANDER, 2.4, Param.MAX_SPEED, 420d,
                Param.MAX_FORCE, 2200d, Param.CROSSOVER, 2.2, Param.CROSS_SWIRL, -2d,
                Param.BOUNCE, 1.15, Param.TRAIL, 0.6));
    }

    /** The names {@link #applyPreset} takes. */
    public static List<String> presetNames() {
        return List.copyOf(presets.keySet());
    }

    /**
     * Resets and then applies the named set of overrides.
     *
     * @throws IllegalArgumentException if there is no such preset
     */
    public void applyPreset(String name) {
        Map<Param, Double> preset = presets.get(name);
        if (preset == null) throw new IllegalArgumentException("no such preset: " + name);
        for (Param p : Param.values()) values[p.ordinal()] = preset.getOrDefault(p, p.def);
        fire();
    }

    // ----- persistence -----

    /** This set as properties, one line per parameter. */
    public Properties toProperties() {
        Properties props = new Properties();
        for (Param p : Param.values()) {
            props.setProperty(p.key(), p.isIntegral()
                    ? Integer.toString(getInt(p)) : Double.toString(get(p)));
        }
        return props;
    }

    /** Takes what {@code props} names; anything it does not name is left alone. */
    public void fromProperties(Properties props) {
        for (Param p : Param.values()) {
            String v = props.getProperty(p.key());
            if (v == null) continue;
            try {
                values[p.ordinal()] = Math.clamp(Double.parseDouble(v.trim()), p.min, p.max);
            } catch (NumberFormatException e) {
                // a hand edited file is no reason to lose the rest of it
            }
        }
        fire();
    }

    public void save(Path path) throws IOException {
        try (OutputStream out = Files.newOutputStream(path)) {
            toProperties().store(out, "mdplayer boids visualizer parameters");
        }
    }

    public void load(Path path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        }
        fromProperties(props);
    }
}
