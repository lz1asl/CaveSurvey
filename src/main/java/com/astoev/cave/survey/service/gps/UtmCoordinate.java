package com.astoev.cave.survey.service.gps;

// source: https://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm
public class UtmCoordinate {

    private double Easting;
    private double Northing;
    private int Zone;
    private char Letter;


    public UtmCoordinate(double latitude, double longitude) {
        Zone = (int) Math.floor(longitude / 6 + 31);
        if (latitude < -72)
            Letter = 'C';
        else if (latitude < -64)
            Letter = 'D';
        else if (latitude < -56)
            Letter = 'E';
        else if (latitude < -48)
            Letter = 'F';
        else if (latitude < -40)
            Letter = 'G';
        else if (latitude < -32)
            Letter = 'H';
        else if (latitude < -24)
            Letter = 'J';
        else if (latitude < -16)
            Letter = 'K';
        else if (latitude < -8)
            Letter = 'L';
        else if (latitude < 0)
            Letter = 'M';
        else if (latitude < 8)
            Letter = 'N';
        else if (latitude < 16)
            Letter = 'P';
        else if (latitude < 24)
            Letter = 'Q';
        else if (latitude < 32)
            Letter = 'R';
        else if (latitude < 40)
            Letter = 'S';
        else if (latitude < 48)
            Letter = 'T';
        else if (latitude < 56)
            Letter = 'U';
        else if (latitude < 64)
            Letter = 'V';
        else if (latitude < 72)
            Letter = 'W';
        else
            Letter = 'X';
        Easting = 0.5 * Math.log((1 + Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(latitude * Math.PI / 180) * Math.sin(longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2) / 3) + 500000;
        Easting = Math.round(Easting * 100) * 0.01;
        Northing = (Math.atan(Math.tan(latitude * Math.PI / 180) / Math.cos((longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) - latitude * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(latitude * Math.PI / 180) * Math.sin((longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) / (1 - Math.cos(latitude * Math.PI / 180) * Math.sin((longitude * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (latitude * Math.PI / 180 - 0.005054622556 * (latitude * Math.PI / 180 + Math.sin(2 * latitude * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (latitude * Math.PI / 180 + Math.sin(2 * latitude * Math.PI / 180) / 2) + Math.sin(2 * latitude * Math.PI / 180) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (latitude * Math.PI / 180 + Math.sin(2 * latitude * Math.PI / 180) / 2) + Math.sin(2 * latitude * Math.PI / 180) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) / 4 + Math.sin(2 * latitude * Math.PI / 180) * Math.pow(Math.cos(latitude * Math.PI / 180), 2) * Math.pow(Math.cos(latitude * Math.PI / 180), 2)) / 3);
        if (Letter < 'M')
            Northing = Northing + 10000000;
        Northing = Math.round(Northing * 100) * 0.01;
    }

    public double getEasting() {
        return Easting;
    }

    public double getNorthing() {
        return Northing;
    }

    public int getZone() {
        return Zone;
    }

    public char getLetter() {
        return Letter;
    }
}
