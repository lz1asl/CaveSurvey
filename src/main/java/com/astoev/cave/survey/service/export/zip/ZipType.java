package com.astoev.cave.survey.service.export.zip;

public enum ZipType {
    DATA, ALL;

    public static ZipType fromIndex(int index) {
        switch (index) {
            case 0: return DATA;
            case 1: return ALL;
            default: throw new RuntimeException();
        }
    }
}
