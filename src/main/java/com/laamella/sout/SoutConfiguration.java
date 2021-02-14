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
    final DataConverter dataConverter;
    final ModelTraveller modelTraveller;

    public SoutConfiguration(char openChar, char separatorChar, char closeChar, char escapeChar, List<NameRenderer> nameRenderers, List<TypeRenderer> typeRenderers) {
        this.openChar = openChar;
        this.escapeChar = escapeChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
        this.nameRenderers = new ArrayList<>(nameRenderers);
        this.typeRenderers = new ArrayList<>(typeRenderers);
        dataConverter = new DataConverter(this);
        modelTraveller = new ModelTraveller();
    }
}
