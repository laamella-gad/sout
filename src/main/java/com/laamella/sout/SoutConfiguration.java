package com.laamella.sout;

import java.util.ArrayList;
import java.util.List;

public class SoutConfiguration {
    final char openChar;
    final char escapeChar;
    final char closeChar;
    final char separatorChar;
    final List<NameHandler> nameHandlers;
    final List<TypeHandler> typeHandlers;
    final DataConverter dataConverter;
    final DataTraveller dataTraveller;

    public SoutConfiguration(char openChar, char separatorChar, char closeChar, char escapeChar, List<NameHandler> nameHandlers, List<TypeHandler> typeHandlers) {
        this.openChar = openChar;
        this.escapeChar = escapeChar;
        this.closeChar = closeChar;
        this.separatorChar = separatorChar;
        this.nameHandlers = new ArrayList<>(nameHandlers);
        this.typeHandlers = new ArrayList<>(typeHandlers);
        dataConverter = new DataConverter(this);
        dataTraveller = new DataTraveller(this);
    }
}
