package org.example;

import com.opencsv.CSVWriter;
import mftool.MFTool;
import mftool.models.output.SchemeNameCodePair;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {

    private final List<PortfolioItem> portfolioItemList= new ArrayList<>();
    private MFTool mfTool=null;

    public Portfolio() {
        mfTool= new MFTool();
    }

    public void addToPortfolio(Map<String,Double> schemeNames){

        //BigDecimal.valueOf(schemeNames.get("a"));
        HashMap<String,SchemeNameCodePair> schemeNameCodePairHashMap= new HashMap<>();
                for(String schemName:schemeNames.keySet()){
                    List<SchemeNameCodePair> response = getPortfolioDetails(schemName);
                    if(response.size()>0) {
                        schemeNameCodePairHashMap.put(schemName, response.get(0));
                    }else{
                        if(schemName.contains("Motilal")){
                            SchemeNameCodePair schemeNameCodePair= new SchemeNameCodePair();
                            schemeNameCodePair.setCode("148381");
                            schemeNameCodePair.setName("Motilal Oswal S&P 500 Index Fund - Direct Plan Growth");
                        schemeNameCodePairHashMap.put(schemName,schemeNameCodePair);
                        }

                    }
        }
              portfolioItemList.addAll(  schemeNameCodePairHashMap.entrySet()
                        .stream()
                        .map(ent -> getportfolioItem(ent.getValue(),schemeNames.getOrDefault(ent.getKey(),0.00)))
                        .toList());
    }

    @NotNull
    private PortfolioItem getportfolioItem(SchemeNameCodePair code, Double units) {
        try {
            return new PortfolioItem(code.getName(), code.getCode(), mfTool.getCurrentNav(code.getCode()), mfTool.getYesterdayNav(code.getCode()),BigDecimal.valueOf(units));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private  List<SchemeNameCodePair> getPortfolioDetails(String schemeName){
        try {
            return  mfTool.updateScheme(schemeName)
                    .stream()
                    .filter(schemeNameCodePair -> schemeNameCodePair.getName().toLowerCase().contains("growth") && schemeNameCodePair.getName().toLowerCase().contains("direct"))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public  void printPortfolio(){
        portfolioItemList.forEach(System.out::println);
    }

    public void portfolioSummary(){
        BigDecimal dayProfitOrloss = portfolioItemList.stream()
                .map(p -> p.DayProfitOrLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
       /* portfolioItemList.stream()
                        .sorted((s1,s2)->s2.DayProfitOrLoss.subtract(s1.DayProfitOrLoss).intValue())
                                .forEach(System.out::println);*/

        //System.out.println("Sort By Percentage");

       portfolioItemList.stream()
                .sorted((s1,s2)->s2.percentChange.compareTo(s1.percentChange))
                .forEach(System.out::println);
        BigDecimal totalValuePresent=totalPortfolioValue();
        BigDecimal yesterdayValue = totalValuePresent.subtract(dayProfitOrloss);
        BigDecimal percentage = dayProfitOrloss.divide(yesterdayValue,5, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

       // ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

        //executor.awaitTermination()
        System.out.println("Profit or Loss for the Day:"+ LocalDate.now() +"  Rs.:"+ dayProfitOrloss);
        System.out.println("Profit or Loss Percentage the Day:"+ LocalDate.now()+"  " + percentage +"%");
        NumberFormat formatter = NumberFormat.getNumberInstance();
        String formattedNumber = formatter.format(totalValuePresent);
        System.out.println("Portfolio Value Rs."+formattedNumber);

    }

    /*public void PostgresInsertExample() {

        // JDBC URL, username, and password
         final String URL = "jdbc:postgresql://localhost:5432/postgres";
         final String USER = "postgres";
         final String PASSWORD = "mysecretpassword";


            // SQL INSERT statement
            String sql = "INSERT INTO your_table_name (column1, column2, ...) VALUES (?, ?, ...)";

            try (
                    // Establish database connection
                    Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    // Create a PreparedStatement object
                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ) {
                // Set values for parameters in the PreparedStatement
                preparedStatement.setString(1, "value1");
                preparedStatement.setString(2, "value2");
                // Set values for other parameters as needed

                // Execute the INSERT statement
                int rowsInserted = preparedStatement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new row has been inserted successfully.");
                } else {
                    System.out.println("Failed to insert the row.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/


    public BigDecimal totalPortfolioValue(){
        return portfolioItemList.stream()
                .map(p -> p.units.multiply(p.nav))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void getPortfolioCsvFile(){


        File f= new File("C:/Users/Srinivas/Documents/azure-prac/portfolio-"+LocalDate.now()+".csv");

        try (CSVWriter writer = new CSVWriter(new FileWriter(f))) {
            // Writing header
            String[] header = {"SchemeName", "Units", "AmountInvested"};
            writer.writeNext(header);

            // Writing data
            for (PortfolioItem portfolio : portfolioItemList) {
                String[] data = {portfolio.getSchemeName(), portfolio.getUnits().toString(), portfolio.getCurrentValue().toString()};
                writer.writeNext(data);
            }

            System.out.println("CSV file created successfully Created here."+f.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
