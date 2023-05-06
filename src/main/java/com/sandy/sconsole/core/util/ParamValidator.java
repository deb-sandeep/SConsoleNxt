package com.sandy.sconsole.core.util;

public class ParamValidator {

    public static void inRange( int value, int min, int max ) {
        if( value < min || value > max ) {
            throw new IllegalArgumentException( "Out of range [" + min + "," + max + "]" ) ;
        }
    }

    public static void inAscendingOrder( int firstVal, int secondVal, int... vals ) {
        if( firstVal > secondVal ) {
            throw new IllegalArgumentException( "Values not in ascending order" ) ;
        }
        int minVal = secondVal ;
        if( vals != null ) {
            for( int val : vals ) {
                if( minVal > val ) {
                    throw new IllegalArgumentException( "Values not in ascending order" ) ;
                }
                minVal = val ;
            }
        }
    }
}
