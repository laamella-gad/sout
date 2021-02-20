package com.laamella.sout;

/**
 * Everything that is configurable.
 */
public class SoutConfiguration {
    final char openChar;
    final char escapeChar;
    final char closeChar;
    final char separatorChar;
    final NameRenderer nameRenderer;
    final TypeRenderer typeRenderer;
    final boolean allowNullValues;
    final boolean allowNullLoops;

    /**
     * @param openChar        the character that opens a name or loop, like "{" or "<"
     * @param separatorChar   the character that separates parts of a loop, like "|"
     * @param closeChar       the character that closes a name or loop, like "}" or ">"
     * @param escapeChar      the character that can escape the openChar, separatorChar, and closeChar
     * @param nameRenderer    a {@link NameRenderer} that will be asked if they can render a simple name (so no loops.)
     * @param typeRenderer    a list of {@link TypeRenderer} that will be asked if they can render a specific type (class) that was encountered in the model.
     * @param allowNullValues true: a value of null is rendered as an empty string. false: a value of null throws an exception. This does not affect allowNullLoops.
     * @param allowNullLoops  true: a loop over a null collection is rendered as an empty string. false: a null collection throws an exception. This does not affect allowNullValues.
     */
    public SoutConfiguration(
            char openChar, char separatorChar, char closeChar, char escapeChar,
            NameRenderer nameRenderer, TypeRenderer typeRenderer,
            boolean allowNullValues, boolean allowNullLoops) {
        this.openChar = openChar;
        this.escapeChar = escapeChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
        this.nameRenderer = nameRenderer;
        this.typeRenderer = typeRenderer;
        this.allowNullValues = allowNullValues;
        this.allowNullLoops = allowNullLoops;
    }
}
