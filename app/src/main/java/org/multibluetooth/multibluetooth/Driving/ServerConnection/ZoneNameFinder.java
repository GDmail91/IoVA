package org.multibluetooth.multibluetooth.Driving.ServerConnection;

import android.location.Location;

/**
 * Created by YS on 2016-11-02.
 */
public class ZoneNameFinder {

    static final double KoFarEast = 131.52; // 동경
    static final double KoFarWest = 125.04; // 동경
    static final double KoFarNorth = 38.27; // 북위
    static final double KoFarSouth = 33.06; // 북위

    public String getKoZNF(Location location) {
        // TODO validation
        // TODO 현재 위치가 FarWest 부터 FarEast 안에 있는지
        // TODO 현재 위치가 FarNorth 부터 FarSouth 안에 있는지
        if (location.getLatitude() < KoFarNorth
        && location.getLatitude() > KoFarSouth
        && location.getLongitude() < KoFarEast
        && location.getLongitude() > KoFarWest) {
            // TODO 현재 위치 계산
            double cLon = KoFarEast - location.getLongitude();
            double cLat = KoFarNorth - location.getLatitude();
            // TODO FarWest, FarNorth 부터 현재 위치가 몇 Km 후에 있는지
            int lonKm = (int) lonToKilometer(cLon, cLat);
            int latKm = (int) latToKilometer(cLat);
            // TODO 해당 위치의 키값을 반환
            return "" + lonKm + "_" + latKm;
        }

        return "";
    }

    private double latToKilometer(double cLat){
        // 위도는 어디에서 계산하든 1도의 길이는 111km 이다

        return cLat * 111;
    }

    private double lonToKilometer(double cLon, double cLat){
        // 해당 위도(X)에서 경도 1도의 길이는 2πr cos X × 1/360

        return cLon * (2 * 3.14 * 6400 * Math.cos(cLat) * 1/360);
    }
}
