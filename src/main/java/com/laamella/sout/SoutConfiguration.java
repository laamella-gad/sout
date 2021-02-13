package com.laamella.sout;

import java.util.List;

public class SoutConfiguration {
    final char openChar;
    final char escapeChar;
    final char closeChar;
    final char separatorChar;
    final List<NameResolver> nameResolvers;
    final List<TypeHandler> typeHandlers;

    public SoutConfiguration(char openChar, char separatorChar, char closeChar, char escapeChar, List<NameResolver> nameResolvers, List<TypeHandler> typeHandlers) {
        this.openChar = openChar;
        this.escapeChar = escapeChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
        this.nameResolvers = nameResolvers;
        this.typeHandlers = typeHandlers;
    }
}
