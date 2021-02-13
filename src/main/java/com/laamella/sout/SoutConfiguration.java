package com.laamella.sout;

import java.util.ArrayList;
import java.util.List;

public class SoutConfiguration {
    final char openChar;
    final char escapeChar;
    final char closeChar;
    final char separatorChar;
    final List<NameResolver> nameResolvers;
    final List<TypeHandler> typeHandlers;
    final DataConverter dataConverter;
    final DataTraveller dataTraveller;

    public SoutConfiguration(char openChar, char separatorChar, char closeChar, char escapeChar, List<NameResolver> nameResolvers, List<TypeHandler> typeHandlers) {
        this.openChar = openChar;
        this.escapeChar = escapeChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
        this.nameResolvers = new ArrayList<>(nameResolvers);
        this.typeHandlers = new ArrayList<>(typeHandlers);
        dataConverter = new DataConverter(this);
        dataTraveller = new DataTraveller(this);
    }
}
