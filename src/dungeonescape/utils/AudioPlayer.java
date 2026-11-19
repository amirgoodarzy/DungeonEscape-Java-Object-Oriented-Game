package dungeonescape.utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Procedural sound synthesizer utilizing standard JDK javax.sound.sampled.
 * Generates immediate audio feedback for gameplay interactions without needing external audio files.
 * Thread-safe and fails gracefully if system audio is muted or unavailable.
 */
public final class AudioPlayer {
    private static boolean soundEnabled = true;

    private AudioPlayer() {}

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Synthesizes and plays a tone at the specified frequency and duration in a daemon background thread.
     * 
     * @param frequencyHz Frequency in Hertz (e.g. 440 for concert A).
     * @param durationMs Duration of tone in milliseconds.
     * @param volume Normalized volume between 0.0 and 1.0.
     */
    public static void playTone(final int frequencyHz, final int durationMs, final double volume) {
        if (!soundEnabled) return;

        Thread soundThread = new Thread(() -> {
            try {
                final float sampleRate = 44100f;
                byte[] buffer = new byte[(int) (sampleRate * (durationMs / 1000f))];
                double amplitude = Math.max(0.0, Math.min(1.0, volume)) * 127.0;

                for (int i = 0; i < buffer.length; i++) {
                    double angle = 2.0 * Math.PI * i / (sampleRate / frequencyHz);
                    // Sine wave generation with mild envelope fade-out to prevent clicks
                    double fade = 1.0 - ((double) i / buffer.length);
                    buffer[i] = (byte) (Math.sin(angle) * amplitude * fade);
                }

                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();
                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception ignored) {
                // Fails silently if audio line unavailable in test environments
            }
        });
        soundThread.setDaemon(true);
        soundThread.start();
    }

    public static void playAttackSound() {
        playTone(320, 80, 0.4);
    }

    public static void playHitSound() {
        playTone(180, 120, 0.6);
    }

    public static void playPickupSound() {
        playTone(660, 90, 0.5);
    }

    public static void playDoorUnlockSound() {
        playTone(880, 150, 0.5);
    }

    public static void playErrorSound() {
        playTone(140, 160, 0.5);
    }

    public static void playVictoryFanfare() {
        playTone(523, 100, 0.6); // C5
        try { Thread.sleep(110); } catch (InterruptedException ignored) {}
        playTone(659, 100, 0.6); // E5
        try { Thread.sleep(110); } catch (InterruptedException ignored) {}
        playTone(783, 250, 0.7); // G5
    }
}
