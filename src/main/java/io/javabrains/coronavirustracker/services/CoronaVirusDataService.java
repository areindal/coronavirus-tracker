package io.javabrains.coronavirustracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.javabrains.coronavirustracker.models.LocationStats;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(CoronaVirusDataService.class);

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* 1 * * * *")
    public void fetchVirusData() throws IOException, InterruptedException {
    	LOGGER.info("------------------------------------------------------------------------------------------------------------");
    	LOGGER.info("--------------------------------------->Recuperando informacion!<--------------------------------------------");
    	LOGGER.info("------------------------------------------------------------------------------------------------------------");
        List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        //CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        String resultsLastDate = ((CSVParser)records).getHeaderNames().get(((CSVParser)records).getHeaderNames().size()-1);
        //CSVRecord aux = (CSVRecord)records.
        for (CSVRecord record : records) {
        	
            LocationStats locationStat = new LocationStats();
            locationStat.setLastDateData(resultsLastDate);
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));

            int latestCases = Integer.parseInt(record.get(record.size() - 1));
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
            int prevDayCases2 = Integer.parseInt(record.get(record.size() - 3));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffFromPrevDay((latestCases - prevDayCases>0)?latestCases - prevDayCases:0);
            locationStat.setDiffFromPrevDay2(prevDayCases-prevDayCases2);
            //loca
            newStats.add(locationStat);
        }
        this.allStats = newStats;
    }

}
