package com.joadtor.escam.component;

import java.util.Comparator;

public class CompareIcon implements Comparator<Icon> {
    @Override
    public int compare(Icon o1, Icon o2) {
        return o1.getY() - o2.getY();
    }
}
