package com.laamella.sout;

class TestModel {

    static class Recurser {
        final Recurser recurser;
        final int value;

        public Recurser(int value, Recurser recurser) {
            this.value = value;
            this.recurser = recurser;
        }

        public Recurser(int value) {
            this.value = value;
            recurser = null;
        }
    }

    final Recurser recurser = new Recurser(1, new Recurser(2, new Recurser(3)));
    final String field = "*field*";

    String getGetter() {
        return "*getter*";
    }

    boolean isIsser() {
        return true;
    }

    int plainMethod() {
        return 15;
    }
}
