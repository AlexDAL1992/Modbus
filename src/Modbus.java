import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Modbus {

    public static String bcdConverter(String number){
        switch(number){
            case "0000": return "0";
            case "0001": return "1";
            case "0010": return "2";
            case "0011": return "3";
            case "0100": return "4";
            case "0101": return "5";
            case "0110": return "6";
            case "0111": return "7";
            case "1000": return "8";
            case "1001": return "9";
            case "1010": return "A";
            case "1011": return "B";
            case "1100": return "C";
            case "1101": return "D";
            case "1110": return "E";
            case "1111": return "F";
            default: return "";
        }
    }

    public static String dateSuffix(String date){
        switch(date){
            case "01": case "21": case "31": return "st";
            case "02": case "22": return "nd";
            case "03": case "23": return "rd";
            default: return "th";
        }
    }

    public static String language(String number){
        switch(number){
            case "0": return "English";
            case "1": return "Chinese";
            default: return "Other language";
        }
    }

    public static ArrayList readData(String filename) throws IOException, ParseException {

        ArrayList<String> dataList = new ArrayList<>();
        BufferedReader input = new BufferedReader(new FileReader(filename));

        String line;
        while((line = input.readLine()) != null){
            dataList.add(line);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        dataList.set(0, "" + dateFormat.parse(dataList.get(0)));

        for(int i = 1; i < dataList.size(); i++){
            String[] rawData = dataList.get(i).split(":");
            dataList.set(i, rawData[1]);
        }

        return dataList;
    }

    public static String fillUpBits(String number, int numberOfBits){
        while(number.length() < numberOfBits){
            number = "0" + number;
        }
        return number;
    }

    public static String bcdToString(String number){
        String raw, value;

        raw = Integer.toBinaryString(Integer.parseInt(number));
        value = fillUpBits(raw, 16);

        StringBuilder bcd = new StringBuilder();

        for(int i = 0; i < value.length(); i+=4){
            bcd.append(bcdConverter(value.substring(i, i+4)));
        }

        return bcd.toString();
    }

    public static String bcdToString(String low, String middle, String high) {

        String bcdLow = bcdToString(low);
        String bcdMiddle = bcdToString(middle);
        String bcdHigh = bcdToString(high);

        bcdLow = bcdLow.substring(2) + bcdLow.substring(0,2);
        bcdMiddle = bcdMiddle.substring(2) + bcdMiddle.substring(0,2);
        bcdHigh = bcdHigh.substring(2) + bcdHigh.substring(0,2);

        return bcdLow + bcdMiddle + bcdHigh;
    }

    public static String calendar(String number){

        return "20" + number.substring(10) + "-" + number.substring(8,10) + "-" + number.substring(6,8)
                + " " + number.substring(4,6) + ":" + number.substring(2,4) + ":" + number.substring(0,2);
    }

    public static String autosave(String number){
        return number.substring(2) + ":00 on "
                + (number.substring(0,2).equals("00") ? "everyday" : number.substring(0,2) + dateSuffix(number.substring(0,2)));
    }

    public static String signalQuality(String number) throws NumberFormatException {
        String raw, value, quality;

        raw = Integer.toBinaryString(Integer.parseInt(number));
        value = fillUpBits(raw, 16);
        quality = Integer.toString(Integer.parseInt(value.substring(8), 2));

        return quality;
    }

    public static String workingStep(String number) throws NumberFormatException {
        String raw, value, step;

        raw = Integer.toBinaryString(Integer.parseInt(number));
        value = fillUpBits(raw, 16);
        step = Integer.toString(Integer.parseInt(value.substring(0,8), 2));

        return step;
    }

    public static int longToInt(String low, String high) throws NumberFormatException {
        String number, original, firstCompliment = "";
        int sign, value;

        high = Integer.toBinaryString(Integer.parseInt(high));
        low = Integer.toBinaryString(Integer.parseInt(low));
        number = fillUpBits(high,16) + fillUpBits(low,16);

        sign = (int)Math.pow(-1, Integer.parseInt("" + number.charAt(0)));
        original = number.substring(1);

        if (sign == 1) {
            value = Integer.parseInt(original, 2);
        }else {
            for (int i = 0; i < original.length(); i++) {
                firstCompliment += (original.charAt(i) == '0' ? '1' : '0');
            }
            value = Integer.parseInt(firstCompliment, 2) + 1;
        }

        return sign * value;
    }

    public static double real4ToDouble(String low, String high) throws NumberFormatException {
        //form a single number from both high and low values of the registers
        //and extract it into 3 parts: sign, mantissa, exponent
        //from which the double value will be formed.
        String number;
        double sign, exponent, mantissa;

        //Convert both high and low register values into binary values,
        //fill up the binary values up to 16 bits
        // add add them up in order: high first, low second
        high = Integer.toBinaryString(Integer.parseInt(high));
        low = Integer.toBinaryString(Integer.parseInt(low));
        number = fillUpBits(high,16) + fillUpBits(low,16);

        //take the first bit as sign
        sign = Integer.parseInt(Character.toString(number.charAt(0)));

        //take the next 8 bits as exponent, convert to integer value of biased exponent,
        //subtract it with 127 to get the real value of exponent.
        exponent = Integer.parseInt(number.substring(1,9),2) - 127;

        //take the rest 23 bits as mantissa, convert it to integer,
        //divide it by 2^23=8388608 as a way of shifting 23 bit to the right,
        //add 1 to the mantissa to give the real value due to the omission of 1
        mantissa = ((double)Integer.parseInt(number.substring(9), 2)) / 8388608 + 1;

        //Putting it together
        return Math.pow(-1, sign) * mantissa * Math.pow(2, exponent);
    }

    public static void main(String[] args) throws Exception {

        String filename = "src/data.txt";
        ArrayList<String> dataList = readData(filename);

        System.out.println("Date and time of the reading: " + dataList.get(0));
        System.out.println();

        System.out.println("Flow rate: " + real4ToDouble(dataList.get(1), dataList.get(2)) + " m3/h");
        System.out.println("Energy flow rate: " + real4ToDouble(dataList.get(3), dataList.get(4)) + " GJ/h");
        System.out.println("Velocity: " + real4ToDouble(dataList.get(5), dataList.get(6)) + " m/h");
        System.out.println("Fluid sound speed: " + real4ToDouble(dataList.get(7), dataList.get(8)) + " m/h");
        System.out.println();

        System.out.println("Positive accumulator: " + longToInt(dataList.get(9), dataList.get(10)) + ", unit N/A");
        System.out.println("Positive decimal fraction: " + real4ToDouble(dataList.get(11), dataList.get(12)) + ", unit N/A");
        System.out.println("Negative accumulator: " + longToInt(dataList.get(13), dataList.get(14)) + ", unit N/A");
        System.out.println("Negative decimal fraction: " + real4ToDouble(dataList.get(15), dataList.get(16)) + ", unit N/A");
        System.out.println();

        System.out.println("Positive energy accumulator: " + longToInt(dataList.get(17), dataList.get(18)) + ", unit N/A");
        System.out.println("Positive energy decimal fraction: " + real4ToDouble(dataList.get(19), dataList.get(20)) + ", unit N/A");
        System.out.println("Negative energy accumulator: " + longToInt(dataList.get(21), dataList.get(22)) + ", unit N/A");
        System.out.println("Negative energy decimal fraction: " + real4ToDouble(dataList.get(23), dataList.get(24)) + ", unit N/A");
        System.out.println();

        System.out.println("Net accumulator: " + longToInt(dataList.get(25), dataList.get(26)) + ", unit N/A");
        System.out.println("Net decimal fraction: " + real4ToDouble(dataList.get(27), dataList.get(28)) + ", unit N/A");
        System.out.println("Net energy accumulator: " + longToInt(dataList.get(29), dataList.get(30)) + ", unit N/A");
        System.out.println("Net energy decimal fraction: " + real4ToDouble(dataList.get(31), dataList.get(32)) + ", unit N/A");
        System.out.println();

        System.out.println("Temperature #1/inlet: " + real4ToDouble(dataList.get(33), dataList.get(34)) + " degree C");
        System.out.println("Temperature #2/outlet: " + real4ToDouble(dataList.get(35), dataList.get(36)) + " degree C");
        System.out.println();

        System.out.println("Analog input AI3: " + real4ToDouble(dataList.get(37), dataList.get(38)) + ", unit N/A");
        System.out.println("Analog input AI4: " + real4ToDouble(dataList.get(39), dataList.get(40)) + ", unit N/A");
        System.out.println("Analog input AI5: " + real4ToDouble(dataList.get(41), dataList.get(42)) + ", unit N/A");
        System.out.println();

        System.out.println("Current input at AI3: " + real4ToDouble(dataList.get(43), dataList.get(44)) + " mA");
        System.out.println("Current input at AI4: " + real4ToDouble(dataList.get(45), dataList.get(46)) + " mA");
        System.out.println("Current input at AI5: " + real4ToDouble(dataList.get(47), dataList.get(48)) + " mA");
        System.out.println();

        //System password at 49-50 registers

        System.out.println("Password for hardware: " + bcdToString(dataList.get(51)));
        System.out.println("Calendar (date and time): " + calendar(bcdToString(dataList.get(53),dataList.get(54),dataList.get(55))));
        System.out.println("Day+hour for auto-save: " + autosave(bcdToString(dataList.get(56))));
        System.out.println();

        System.out.println("Key to input: " + Integer.parseInt(dataList.get(59)));
        System.out.println("Go to Window #: " + Integer.parseInt(dataList.get(60)));
        System.out.println("LCD back-lit lights: " + Integer.parseInt(dataList.get(61)) + " seconds");
        System.out.println("Times for the beeper: " + Integer.parseInt(dataList.get(62)));
        System.out.println("Pulses left for OCT: " + Integer.parseInt(dataList.get(63)));
        System.out.println();

        //Error code at 72 register

        System.out.println("PT100 resistance of inlet: " + real4ToDouble(dataList.get(77), dataList.get(78)) + " Ohm");
        System.out.println("PT100 resistance of outlet: " + real4ToDouble(dataList.get(79), dataList.get(80)) + " Ohm");
        System.out.println();

        System.out.println("Total travel time: " + real4ToDouble(dataList.get(81), dataList.get(82)) + " micro-second");
        System.out.println("Delta travel time: " + real4ToDouble(dataList.get(83), dataList.get(84)) + " nano-second");
        System.out.println("Upstream travel time: " + real4ToDouble(dataList.get(85), dataList.get(86)) + " micro-second");
        System.out.println("Downstream travel time: " + real4ToDouble(dataList.get(87), dataList.get(88)) + " micro-second");
        System.out.println();

        System.out.println("Output current: " + real4ToDouble(dataList.get(89), dataList.get(90)) + " mA");
        System.out.println("Working step: " + workingStep(dataList.get(92)));
        System.out.println("Signal quality: " + signalQuality(dataList.get(92)));
        System.out.println("Upstream strength: " + Integer.parseInt(dataList.get(93)));     //needs modification
        System.out.println("Downstream strength: " + Integer.parseInt(dataList.get(94)));   //needs modification
        System.out.println();

        System.out.println("Language used in user interface: " + language(dataList.get(96)));
        System.out.println("The rate of the measured travel time by the calculated travel time: " + real4ToDouble(dataList.get(97), dataList.get(98)) + ", normal 100+-3%");
        System.out.println("Reynolds number: " + real4ToDouble(dataList.get(99), dataList.get(100)));
        System.out.println();
    }
}