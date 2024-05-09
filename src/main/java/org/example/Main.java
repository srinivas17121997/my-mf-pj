package org.example;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {


       // Optional<Integer> awe = Stream.of(1, 2, 3, 4, 5, 6).limit(2).skip(1).findFirst();

        //System.out.println(awe.get());

        Map<String, Double> abc = Map.of("Quant Active", 140.510, "quant Mid Cap Fund - Growth", 466.252, "Axis Bluechip", 1222.244, "Axis Gold", 2465.237, "Axis Midcap", 1267.276, "Axis Small", 457.770, "HDFC Small", 216.372, "SBI Small", 803.196,  "ICICI Prudential Bluechip", 865.430);


      //  Scheme{code='118835', name='Mirae Asset Emerging Bluechip Fund - Direct Plan - Dividend'}
        List<String> schemes =
                List.of("Quant Active","Quant Mid","Axis Blue", "Axis Gold", "Axis Mid", "Axis Small","HDFC Small","SBI Small","Motilal Oswal S&P 500 Index Fund","ICICI Prudential Blue");
        Portfolio portfolio= new Portfolio();
       portfolio.addToPortfolio(abc);
        portfolio.addToPortfolio(Map.of("Mirae Asset Emerging Bluechip Fund",766.695));
       portfolio.addToPortfolio(Map.of("Motilal Oswal S&P 500", 10731.2901));
               portfolio.addToPortfolio(Map.of(     "quant Small",85.941));

       // List.of("Axis Blue",  "Axis Mid", "Motilal Oswal S&P 500 Index Fund","ICICI Prudential Blue");

        //  portfolio.printPortfolio();
        portfolio.portfolioSummary();
      // portfolio.totalPortfolioValue();
       portfolio.getPortfolioCsvFile();
    }
 /*   public static List<SchemeNameCodePair> abc(MFTool tool, String schemeName){

        try {

            return  tool.matchingScheme(schemeName)
                    .stream()
                            .filter(schemeNameCodePair -> schemeNameCodePair.getName().toLowerCase().contains("growth") && schemeNameCodePair.getName().toLowerCase().contains("direct"))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
}

//public class RequestProcessor {
//    public int[] processRequests(long[] timestamps, String[] ipAddresses, int totalLimit, long window) {
//        Map<String, Queue<Long>> ipTimestamps = new HashMap<>();
//
//        int[] acceptedRequests = new int[timestamps.length];
//        for (int i = 0; i < timestamps.length; i++) {
//            long timestamp = timestamps[i];
//            String ipAddress = ipAddresses[i];
//
//            // Initialize the queue of timestamps for this IP address if it's not already set
//            if (!ipTimestamps.containsKey(ipAddress)) {
//                ipTimestamps.put(ipAddress, new LinkedList<>());
//            }
//
//            Queue<Long> ipTimestampsQueue = ipTimestamps.get(ipAddress);
//
//            // Add the current timestamp to the queue
//            ipTimestampsQueue.add(timestamp);
//
//            // Check if the request should be accepted based on the limit and window
//            int countWithinWindow = 0;
//            long currentTimestamp = System.currentTimeMillis() / 1000;
//            Iterator<Long> iterator = ipTimestampsQueue.iterator();
//            while (iterator.hasNext()) {
//                long queuedTimestamp = iterator.next();
//                if (currentTimestamp - queuedTimestamp <= window) {
//                    countWithinWindow++;
//                } else {
//                    iterator.remove();
//                }
//            }
//
//            if (countWithinWindow <= totalLimit) {
//                acceptedRequests[i] = 1;
//            } else {
//                acceptedRequests[i] = 0;
//            }
//        }
//
//        return acceptedRequests;
//    }
//
//    public static void main(String[] args) {
//        RequestProcessor processor = new RequestProcessor();
//        long[] timestamps = {1632345600L, 1632345700L, 1632345800L, 1632345900L, 1632346000L};
//        String[] ipAddresses = {"192.168.1.1", "192.168.1.2", "192.168.1.1", "192.168.1.3", "192.168.1.2"};
//        int totalLimit = 2;
//        long window = 3; // In seconds, e.g., 3 seconds
//        int[] result = processor.processRequests(timestamps, ipAddresses, totalLimit, window);
//        System.out.println(Arrays.toString(result));
//    }
//}
