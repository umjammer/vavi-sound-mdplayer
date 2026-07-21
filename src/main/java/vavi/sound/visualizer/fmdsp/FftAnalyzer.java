/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.visualizer.fmdsp;


/**
 * The spectrum analyzer bars, computed from the rendered PCM.
 * <p>
 * A data source feeds the mixed-down samples to {@link #push(float)} as they are rendered and hands
 * this over as its {@link FmDspDataSource#fft()}.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-07-14 nsano initial version <br>
 */
public class FftAnalyzer implements FftDataSource {

    /** power of two, ~93 ms at 44100 Hz. Long enough to resolve the low end of the printed axis. */
    private static final int fftLength = 4096;

    /**
     * The renderer prints its frequency axis 48 px per octave and draws the 70 bars 4 px apart, so
     * a bar spans an octave / 12 and 250 Hz sits on bar 10.5. Anything else and the bars don't
     * stand where the labels say they do.
     */
    private static final double fftBarsPerOctave = 12;

    /** frequency of bar 0, so that bar 10.5 is 250 Hz */
    private static final double fftLow = 250 / Math.pow(2, 10.5 / fftBarsPerOctave);

    /** the axis is labelled 0 dB at the top and -48 dB at the bottom, one bar step is 1.5 dB */
    private static final double fftFloor = -48;

    private final int sampleRate;

    private final float[] pcm = new float[fftLength];

    private int pcmPosition;

    private final double[] window = new double[fftLength];

    private final double[] re = new double[fftLength];
    private final double[] im = new double[fftLength];

    /** magnitude of each fft bin, normalized so a full scale sine reads 1.0 */
    private final double[] magnitude = new double[fftLength / 2];

    public FftAnalyzer(int sampleRate) {
        this.sampleRate = sampleRate;
        for (int i = 0; i < fftLength; i++) {
            window[i] = 0.5 - 0.5 * Math.cos(2 * Math.PI * i / (fftLength - 1));
        }
    }

    /** one rendered sample, {@code -32768 .. 32767} */
    public void push(float sample) {
        pcm[pcmPosition] = sample;
        pcmPosition = (pcmPosition + 1) % fftLength;
    }

    @Override
    public void readFft(int[] out) {
        int position = pcmPosition;
        for (int i = 0; i < fftLength; i++) {
            re[i] = pcm[(position + i) % fftLength] / 32768.0 * window[i];
            im[i] = 0;
        }
        fft(re, im);

        // the hann window halves the amplitude and a real signal splits it over two bins
        for (int b = 0; b < magnitude.length; b++) {
            magnitude[b] = Math.sqrt(re[b] * re[b] + im[b] * im[b]) * 4 / fftLength;
        }

        double binWidth = (double) sampleRate / fftLength;
        for (int i = 0; i < FftDataSource.LENGTH; i++) {
            double from = fftLow * Math.pow(2, (i - 0.5) / fftBarsPerOctave) / binWidth;
            double to = fftLow * Math.pow(2, (i + 0.5) / fftBarsPerOctave) / binWidth;
            double peak = 0;
            int first = (int) Math.floor(from);
            int last = (int) Math.ceil(to);
            for (int b = Math.max(1, first); b <= Math.min(last, magnitude.length - 1); b++) {
                peak = Math.max(peak, magnitude[b]);
            }
            double db = 20 * Math.log10(Math.max(peak, 1e-9));
            out[i] = Math.clamp(
                    Math.round((db - fftFloor) / -fftFloor * (FftDataSource.MAX + 1)), 0, FftDataSource.MAX);
        }
    }

    /** in place radix-2 FFT */
    private static void fft(double[] re, double[] im) {
        int n = re.length;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) {
                j ^= bit;
            }
            j ^= bit;
            if (i < j) {
                double t = re[i]; re[i] = re[j]; re[j] = t;
                t = im[i]; im[i] = im[j]; im[j] = t;
            }
        }
        for (int len = 2; len <= n; len <<= 1) {
            double angle = -2 * Math.PI / len;
            double wr = Math.cos(angle);
            double wi = Math.sin(angle);
            for (int i = 0; i < n; i += len) {
                double cr = 1, ci = 0;
                for (int j = 0; j < len / 2; j++) {
                    double ur = re[i + j], ui = im[i + j];
                    double vr = re[i + j + len / 2] * cr - im[i + j + len / 2] * ci;
                    double vi = re[i + j + len / 2] * ci + im[i + j + len / 2] * cr;
                    re[i + j] = ur + vr;
                    im[i + j] = ui + vi;
                    re[i + j + len / 2] = ur - vr;
                    im[i + j + len / 2] = ui - vi;
                    double nr = cr * wr - ci * wi;
                    ci = cr * wi + ci * wr;
                    cr = nr;
                }
            }
        }
    }
}
