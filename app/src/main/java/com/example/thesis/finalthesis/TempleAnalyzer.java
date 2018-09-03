package com.example.thesis.finalthesis;


import android.util.Log;
import java.util.LinkedList;


public class TempleAnalyzer {

    final static String TAG = "TempleAnalyzer";

    LinkedList noteList = new LinkedList();
    LinkedList tempList = new LinkedList();

    public LinkedList analyzeTemple(LinkedList dataList, int dataBpm)
    {

        while(dataList.size()!=0)
        {
            String beginCheck = dataList.getFirst().toString();

            if( beginCheck.equals("rest"+"n\u0020"+",4") )
            {
                dataList.pop();
            }
            else {
                break;  // list
            }
        }


        int beatCounter = 1;
        int beat = 0;
        int i = 0;
        String soundValue;

        int listSize = dataList.size();


        while( i < listSize )
        {
            soundValue = dataList.get(i).toString();

            while (( (i+beatCounter) < listSize ) && soundValue.equals(dataList.get(i+beatCounter).toString()))
            {
                    beatCounter++;
            }
            tempList.add(soundValue+","+beatCounter);
            i = i + beatCounter;
        }


        for(int j=0; j<tempList.size(); j++)
        {
            String s = tempList.get(j).toString();
            String message[] = s.split(",");
            soundValue = message[0]+","+message[1];
            int beatNum = Integer.parseInt(message[2]);
            int manyBeat;
            int redundantBeat;

            if (dataBpm >= 60 && dataBpm <= 100) {
                manyBeat = beatNum / 4;
                redundantBeat = beatNum % 4;
                if (manyBeat > 0) {
                    for (int m = 0; m < manyBeat; m++) {
                        beat = 4;
                        noteList.add(soundValue + "," + beat);
                    }

                    if (redundantBeat != 0) {
                        switch (redundantBeat) {
                            case 3:
                                beat = 8;
                                break;
                            case 2:
                                beat = 8;
                                break;
                            case 1:
                                beat = 16;
                                break;
                        }
                        noteList.add(soundValue + "," + beat);
                    }
                } else if (manyBeat == 0) {
                    switch (redundantBeat) {
                        case 3:
                            beat = 8;
                            break;
                        case 2:
                            beat = 8;
                            break;
                        case 1:
                            beat = 16;
                            break;
                    }
                    noteList.add(soundValue + "," + beat);
                }
            }
            else if (dataBpm > 100 && dataBpm <= 140) {
                manyBeat = beatCounter / 2;
                redundantBeat = beatCounter % 2;
                if (manyBeat > 0) {
                    for (int m = 0; m < manyBeat; m++) {
                        beat = 4;
                        noteList.add(soundValue + "," + beat);
                    }

                    if (redundantBeat != 0) {
                        beat = 8;
                        noteList.add(soundValue + "," + beat);
                    }
                } else if (manyBeat == 0) {
                    beat = 8;
                    noteList.add(soundValue + "," + beat);
                }
            }
        }

        return noteList;
    }

}
