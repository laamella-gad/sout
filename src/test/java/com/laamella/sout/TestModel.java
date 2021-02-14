package com.laamella.sout;

class TestModel {
    final Recurser recurser = new Recurser(1, new Recurser(2, new Recurser(3)));
    final String field = "*field*";

    String getGetter() {
        return "*getter*";
    }

    boolean isIsser() {
        return true;
    }

    Renderable renderable() {
        return (data, output) -> {
            output.append("<<<RENDERABLE on class ");
            output.append(data.getClass().getSimpleName());
            output.append(">>>");
        };
    }
}

class Recurser {
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
