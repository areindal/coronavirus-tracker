package io.javabrains.coronavirustracker.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.javabrains.coronavirustracker.models.LocationStats;
import io.javabrains.coronavirustracker.services.CoronaVirusDataService;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService coronaVirusDataService;
    private static String ORDER_BY_CASES_CRECIENTE = "C";
    private static String ORDER_BY_CASES_DECRECIENTE = "D";
    private static String COMPRESSED_DATA = "C"; //Shows data group by country, not by region

    @GetMapping("/")
    public String home(@RequestParam(name = "type", required=false) String opcion,@RequestParam(name = "order", required=false) String order,Model model) {
        List<LocationStats> allStats = coronaVirusDataService.getAllStats();
        if (opcion!=null && opcion.equals(COMPRESSED_DATA))
        	allStats = join(allStats);
        allStats = order(allStats, order);
        int totalReportedCases = allStats.stream().mapToInt(stat -> stat.getLatestTotalCases()).sum();
        int totalNewCases = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay()).sum();
        int totalNewCases2 = allStats.stream().mapToInt(stat -> stat.getDiffFromPrevDay2()).sum();
        model.addAttribute("locationStats", allStats);
        model.addAttribute("lastDateData", allStats.get(0).getLastDateData());
        model.addAttribute("totalReportedCases", totalReportedCases);
        model.addAttribute("totalNewCases", totalNewCases);
        model.addAttribute("totalNewCases2", totalNewCases2);

        return "home";        
    }
    
    private List<LocationStats> join (List<LocationStats> stats){
    	HashMap<String, LocationStats> aux = new HashMap<>();
    	
    	for (LocationStats stat:stats) {
	    		if (aux.containsKey(stat.getCountry())) {
	    			LocationStats statAux = aux.get(stat.getCountry());
	    			statAux.setDiffFromPrevDay(statAux.getDiffFromPrevDay()+stat.getDiffFromPrevDay());
	    			statAux.setDiffFromPrevDay2(statAux.getDiffFromPrevDay2()+stat.getDiffFromPrevDay2());
	    			statAux.setLatestTotalCases(statAux.getLatestTotalCases() + stat.getLatestTotalCases());
	    		}else {
	    			LocationStats statAux = new LocationStats();
	    			BeanUtils.copyProperties(stat, statAux);
	    			statAux.setState("");
	    			aux.put(stat.getCountry(),statAux);
	    		}
	    	//}
    	}
    	List<LocationStats> res = new ArrayList<LocationStats>();
    	
    	for (String clave:aux.keySet()) {
    		res.add(aux.get(clave));
    	}

    	return res;
    }
    
    private List<LocationStats> order (List<LocationStats> stats,String order){
    	if (ORDER_BY_CASES_DECRECIENTE.equals(order))
    		stats.sort(Comparator.comparing(LocationStats::getLatestTotalCases).reversed());
    	else if (ORDER_BY_CASES_CRECIENTE.equals(order))
    		stats.sort(Comparator.comparing(LocationStats::getLatestTotalCases));
    	else
    		stats.sort(Comparator.comparing(LocationStats::getCountry));
    	
    	return stats;
    }
}
