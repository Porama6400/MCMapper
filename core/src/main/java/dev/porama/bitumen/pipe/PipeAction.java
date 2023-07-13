package dev.porama.bitumen.pipe;

public enum PipeAction {
    /**
     * Submit the current state
     */
    COMMIT,
    /**
     * Ignore the current state and passthroughs input
     */
    PASSTHROUGHS,
    /**
     * Discard and remove output target (e.g. file)
     */
    DISCARD
}
