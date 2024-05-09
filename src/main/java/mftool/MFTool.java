package mftool;

import com.fasterxml.jackson.databind.ObjectMapper;
import mftool.models.Data;
import mftool.models.input.InputSchemeDetails;
import mftool.models.input.Scheme;
import mftool.models.output.SchemeDetails;
import mftool.models.output.SchemeNameCodePair;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MFTool {

    public static final String BASE_URL = "https://api.mfapi.in";
    private final OkHttpClient client = new OkHttpClient();
    private final List<SchemeNameCodePair> schemeNameCodePairList = new ArrayList<>();
    private final Map<String, SchemeDetails> schemeDetailMap = new HashMap<>();
    private final Map<String, List<Data>> schemeNavMap = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    LocalDate dataUpdateDate;
    Map<String, LocalDate> navUpdatedMap = new HashMap<>();

    public List<SchemeNameCodePair> matchingScheme(String searchTerm) throws IOException {
        if (isSchemePairNeedToUpdate()) {
            dataUpdateDate = LocalDate.now();
            updateSchemeNameCodePairList();
        }
        return schemeNameCodePairList
                .stream()
                .filter(obj -> obj.getName().contains(searchTerm))
                .sorted()
                .collect(Collectors.toList());
    }


    public List<SchemeNameCodePair> allSchemes() throws IOException {
        if (isSchemePairNeedToUpdate()) {
            dataUpdateDate = LocalDate.now();
            updateSchemeNameCodePairList();
        }
        return schemeNameCodePairList
                .stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public SchemeDetails schemeDetails(String code) throws IOException {
        if (isSchemeDetailNeedToUpdate(code)) {
            updateSchemeDetails(code);
        }
        return schemeDetailMap.get(code);
    }

    public List<Data> historicNavForScheme(String code) throws IOException {
        if (isSchemeDetailNeedToUpdate(code)) {
            updateSchemeDetails(code);
        }
        return schemeNavMap.get(code);
    }

    public BigDecimal getCurrentNav(String code) throws IOException {
        if (isSchemeDetailNeedToUpdate(code)) {
            updateSchemeDetails(code);
        }
        return schemeNavMap.get(code).get(0).getNav();
    }


    public BigDecimal getYesterdayNav(String code) throws IOException {
        if (isSchemeDetailNeedToUpdate(code)) {
            updateSchemeDetails(code);
        }
        return schemeNavMap.get(code).get(1).getNav();
    }
    public BigDecimal getNavFor(String code, LocalDate date) throws IOException {
        if (isSchemeDetailNeedToUpdate(code)) {
            updateSchemeDetails(code);
        }
        List<Data> d = schemeNavMap.get(code)
                .stream()
                .filter(data -> compareDate(date, data.getDate()))
                .sorted()
                .toList();
        if (!d.isEmpty()) {
            return d.get(0).getNav();
        }
        return new BigDecimal("0");

    }

    private void updateSchemeDetails(String code) throws IOException {
        var request = new Request.Builder()
                .url(BASE_URL + "/mf/" + code)
                .build();
        var response = client.newCall(request).execute();
        var schemeDetails = mapper.readValue(Objects.requireNonNull(response.body()).string(), InputSchemeDetails.class);
        navUpdatedMap.put(code, LocalDate.now());
        schemeDetailMap.put(code, schemeDetails.mapToSchemeDetail());
        schemeNavMap.put(code, schemeDetails.mapToNav());
    }

    public List<SchemeNameCodePair> updateScheme(String schemName) throws IOException {
        updateSchemeNameCodePairList(schemName);
        return schemeNameCodePairList
                .stream()
                .filter(obj -> obj.getName().contains(schemName))
                .sorted()
                .collect(Collectors.toList());
    }
    public BigDecimal getLatestNavV2(String schemeCode) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        String netassetvalue="Net Asset Value";
        Request request = new Request.Builder()
                .url("https://latest-mutual-fund-nav.p.rapidapi.com/fetchLatestNAV?SchemeCode="+schemeCode)
                .get()
                .addHeader("X-RapidAPI-Key", "81979dc548msha8fa6fce2a269e7p1019d7jsn8ee430d735d3")
                .addHeader("X-RapidAPI-Host", "latest-mutual-fund-nav.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();

        List<Object> awert = mapper.readValue(response.body().string(), List.class);
        for(Object obj : awert){

            LinkedHashMap<String,String> details= (LinkedHashMap<String, String>) obj;

            if(details.containsKey(netassetvalue)){
                return new BigDecimal(details.get(netassetvalue));
            }

        }
        return new BigDecimal(0);
    }
    public BigDecimal getYesterdayNavV2(String code) throws IOException {
        try{
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        LocalDate currentDate = LocalDate.now(); // Get the previous day
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();

        // If the previous day is Saturday or Sunday, move back accordingly
        if (currentDayOfWeek == DayOfWeek.SATURDAY) {
            currentDate = currentDate.minusDays(2); // Move back to Friday
        } else if (currentDayOfWeek == DayOfWeek.SUNDAY) {
            currentDate = currentDate.minusDays(3); // Move back to Friday
        } else if (currentDayOfWeek==DayOfWeek.MONDAY) {
            currentDate=currentDate.minusDays(4);
        }
        else if (currentDayOfWeek==DayOfWeek.TUESDAY) {
            currentDate=currentDate.minusDays(4);
        }else{
            currentDate=currentDate.minusDays(2);
        }

        String formattedDate = currentDate.format(formatter);
        String url = "https://latest-mutual-fund-nav.p.rapidapi.com/fetchHistoricalNAV?Date=" + formattedDate + "&SchemeCode="+code;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("X-RapidAPI-Key", "81979dc548msha8fa6fce2a269e7p1019d7jsn8ee430d735d3")
                .addHeader("X-RapidAPI-Host", "latest-mutual-fund-nav.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();
        String netassetvalue="Net Asset Value";

        List<Object> awert = mapper.readValue(response.body().string(), List.class);

        for(Object obj : awert){

            LinkedHashMap<String,String> details= (LinkedHashMap<String, String>) obj;

            if(details.containsKey(netassetvalue)){
                return new BigDecimal(details.get(netassetvalue));
            }

        }}catch (Exception e){
            return new BigDecimal(0);
        }
        return new BigDecimal(0);
    }

    private void updateSchemeNameCodePairList() throws IOException {
        var request = new Request.Builder()
                .url(BASE_URL + "/mf")
                .build();
        var response = client.newCall(request).execute();
        List<Scheme> list = mapper.readValue(Objects.requireNonNull(response.body()).string(),
                mapper.getTypeFactory().constructCollectionType(List.class, Scheme.class));
        schemeNameCodePairList.addAll(list.stream().map(o -> o.map(o)).toList());
    }


    private void updateSchemeNameCodePairList(String schemeName) throws IOException {
        var request = new Request.Builder()
                .url(BASE_URL + "/mf/search?q="+schemeName)
                .build();
        var response = client.newCall(request).execute();
        List<Scheme> list = mapper.readValue(Objects.requireNonNull(response.body()).string(),
                mapper.getTypeFactory().constructCollectionType(List.class, Scheme.class));
        schemeNameCodePairList.addAll(list.stream().map(o -> o.map(o)).toList());
    }

    private boolean compareDate(LocalDate date1, LocalDate date2) {
        return date1.equals(date2) || date1.isBefore(date2);
    }

    public boolean isSchemeDetailNeedToUpdate(String code) {
        return !schemeDetailMap.containsKey(code) || (navUpdatedMap.get(code) != null && LocalDate.now().compareTo(navUpdatedMap.get(code)) != 0);
    }

    public boolean isSchemePairNeedToUpdate() {
        return schemeNameCodePairList.isEmpty() || LocalDate.now().compareTo(dataUpdateDate) != 0;
    }
}
