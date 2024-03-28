package dev.isnow.masscanlinker.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

public class NewIPRangeSplitter {

    public List<List<String>> splitIpRangesToClients(List<String> splitRanges, int numClients) {
        List<List<String>> rangesPerClient = new ArrayList<>();
        for (int i = 0; i < numClients; i++) {
            rangesPerClient.add(new ArrayList<>());
        }
        for (int i = 0; i < splitRanges.size(); i++) {
            rangesPerClient.get(i % numClients).add(splitRanges.get(i));
        }
        return rangesPerClient;
    }

    public List<String> splitIpRanges(List<String> ranges, int numClients) {
        List<String> splitRanges = new ArrayList<>();
        for (String range : ranges) {
            String[] rangeParts = range.split("-");
            String startIp = rangeParts[0];
            String endIp = rangeParts[1];

            long start = ipToLong(startIp);
            long end = ipToLong(endIp);

            long rangeSize = end - start + 1;
            long subRangeSize = rangeSize / numClients;
            long remaining = rangeSize % numClients;

            long subRangeStart = start;
            for (int i = 0; i < numClients; i++) {
                long subRangeEnd = subRangeStart + subRangeSize - 1;
                if (remaining > 0) {
                    subRangeEnd++;
                    remaining--;
                }
                splitRanges.add(longToIp(subRangeStart) + "-" + longToIp(subRangeEnd));
                subRangeStart = subRangeEnd + 1;
            }
        }
        return splitRanges;
    }


    private long ipToLong(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += (Long.parseLong(parts[i]) << (8 * (3 - i)));
        }
        return result;
    }

    private String longToIp(long i) {
        return ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + (i & 0xFF);
    }

}