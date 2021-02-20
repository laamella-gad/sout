package com.laamella.sout;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything that is configurable.
 */
public class SoutConfiguration {
    final char openChar;
    final char escapeChar;
    final char closeChar;
    final char separatorChar;
    final List<NameRenderer> nameRenderers;
    final List<TypeRenderer> typeRenderers;
    final boolean allowNullValues;
    final boolean allowNullLoops;

    /**
     * @param openChar        the character that opens a name or loop, like "{" or "<"
     * @param separatorChar   the character that separates parts of a loop, like "|"
     * @param closeChar       the character that closes a name or loop, like "}" or ">"
     * @param escapeChar      the character that can escape the openChar, separatorChar, and closeChar
     * @param nameRenderers   a list of {@link NameRenderer}s that will be asked if they can render a simple name (so no loops.) An empty list is okay.
     * @param typeRenderers   a list of {@link TypeRenderer}s that will be asked if they can render a specific type (class) that was encountered in the model. An empty list is okay.
     * @param allowNullValues true: a value of null is rendered as an empty string. false: a value of null throws an exception. This does not affect allowNullLoops.
     * @param allowNullLoops  true: a loop over a null collection is rendered as an empty string. false: a null collection throws an exception. This does not affect allowNullValues.
     */
    public SoutConfiguration(
            char openChar, char separatorChar, char closeChar, char escapeChar,
            List<NameRenderer> nameRenderers, List<TypeRenderer> typeRenderers,
            boolean allowNullValues, boolean allowNullLoops) {
        this.openChar = openChar;
        this.escapeChar = escapeChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
        this.nameRenderers = new ArrayList<>(nameRenderers);
        this.typeRenderers = new ArrayList<>(typeRenderers);
        this.allowNullValues = allowNullValues;
        this.allowNullLoops = allowNullLoops;
    }
}
