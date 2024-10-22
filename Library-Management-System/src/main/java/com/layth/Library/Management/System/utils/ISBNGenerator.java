package com.layth.Library.Management.System.utils;

import java.util.Random;

public final class ISBNGenerator {
    private ISBNGenerator(){

    }
    public static String GenerateISBN(){
        Random random = new Random();
        String prefix = "979";
        String groupPublisher = String.format("%09d",random.nextInt(1000000000));
        int checkDigit = random.nextInt(10);

        return prefix + "-" + groupPublisher + "-" + checkDigit;
    }
}
