package preferred.ai.cerebro.core.util;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * StaticMap
 * @author ddle.2015
 *
 */
public class StatisticMap {

	private Map<String, Map<String, Integer>> maps = new HashMap<String, Map<String, Integer>>();
	
	/**
	 * 
	 * @param cat
	 * @param subCat
	 * @param freq
	 */
	public void incr(String cat, String subCat, int freq){
		Map<String, Integer> subMap = maps.get(cat);

		if(ObjectUtils.isNull(subMap)){
			subMap = new HashMap<String, Integer>();
			subMap.put(subCat, freq);
		} else {
			doIncr(subMap, subCat, freq);
		}
		doIncr(subMap, "all", freq);
		maps.put(cat, subMap);
	}
	
	/**
	 * 
	 * @param withPercentage
	 */
	public void report(boolean withPercentage){
		report(System.out, withPercentage, true);
	}

	/**
	 * 
	 * @param out
	 * @param withPercentage
	 */
	public void report(PrintStream out, boolean withPercentage){
		report(out, withPercentage, true);
	}

	/**
	 * 
	 * @param out
	 * @param withPercentage
	 * @param sortSubset
	 */
	public void report(PrintStream out, boolean withPercentage, boolean sortSubset){
		for(String key: maps.keySet()){
			out.println("----------------------------------------------------------------------------------------");
			out.println("Category: " + key);
			out.println("----------------------------------------------------------------------------------------");

			Map<String, Integer> subMap = maps.get(key);

			if(sortSubset) subMap = sortByComparator(subMap, true);
			int allFreq = subMap.get("all");

			for(String subKey: subMap.keySet()){
				if(StringUtils.isNullOrEmpty(subKey)) continue;
				if(subKey.equals("all")) out.println("all" + StringUtils.indent("all", 60) + allFreq);
				else {
					int subFreq = subMap.get(subKey);
					double percentage = NumberUtils.roundDouble((double)subFreq*100/allFreq, 2);
					out.print(subKey + StringUtils.indent(subKey, 60) + subFreq);
					if(withPercentage) out.print(" ( " + percentage + "% )");
					out.println();
				}
			}
		}
	}


	public Set<String> getAllCategories(){
		return maps.keySet();
	}

	/**
	 * 
	 * @param cat
	 * @return
	 */
	public Map<String, Integer> getSubmap(String cat){
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return null;
		return subMap;
	}

	/**
	 * 
	 * @param cat
	 * @param subCat
	 * @return
	 */
	public int getValue(String cat, String subCat){
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return 0;
		Integer subFreq = subMap.get(subCat);
		return (!ObjectUtils.isNull(subFreq)) ? subFreq : 0;
	}
	
	/**
	 * 
	 * @param cat
	 * @return
	 */
	public String getMostFrequentSubcat(String cat){
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return "";
		subMap = sortByComparator(subMap, true);
		for(String subKey: subMap.keySet())
			if(!"all".equalsIgnoreCase(subKey))
				return subKey;

		return "";
	}
	
	/**
	 * 
	 * @param cat
	 * @param topN
	 * @return
	 */
	public Map<String, Integer> getTopSubCat(String cat, int topN){
		Map<String, Integer> holder = new HashMap<String, Integer>();
		
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return holder;
		subMap = sortByComparator(subMap, true);
		
		int prevVal = 0;
		int topCount = 1;
		
		for(String subKey: subMap.keySet()){
			if("all".equalsIgnoreCase(subKey)) continue;
			
			int val = subMap.get(subKey);
			
			if(holder.size() != 0){
				if(val != prevVal)
					topCount++;
				if(topCount > topN) break;
			}
			
			holder.put(subKey, topCount);
			prevVal = val;
		}
		
		return holder;
	}

	/**
	 * 
	 * @param cat
	 * @param freqThreshold
	 */
	public void compactByFrequency(String cat, int freqThreshold){
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return;

		Set<String> subKeys = subMap.keySet();
		Set<String> removingSubKeys = new HashSet<String>();

		int smallValue = 0;

		for(String subKey: subKeys){
			int subFreq = subMap.get(subKey);
			if(subFreq < freqThreshold) {
				smallValue += subFreq;
				removingSubKeys.add(subKey);
			}
		}

		subMap.put("lowerGroup", smallValue);

		for(String subKey: removingSubKeys)
			remove(cat, subKey);
	}
	
	
	/**
	 * 
	 * @param cat
	 * @param freqThreshold
	 */
	
	public void eliminateByFrequency(String cat, int freqThreshold){
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return;

		Set<String> subKeys = subMap.keySet();
		Set<String> removingSubKeys = new HashSet<String>();


		for(String subKey: subKeys){
			int subFreq = subMap.get(subKey);
			if(subFreq < freqThreshold) 
				removingSubKeys.add(subKey);
		}
		
		for(String subKey: removingSubKeys)
			remove(cat, subKey);
	}

	/**
	 * 
	 * @param cat
	 * @param subKey
	 */
	public void remove(String cat, String subKey){
		Map<String, Integer> subMap = maps.get(cat);
		if(ObjectUtils.isNull(subMap)) return;
		subMap.remove(subKey);
	}

	public void clearAll(){
		maps.clear();
	}

	/**
	 * 
	 * @param subMap
	 * @param subCat
	 * @param freq
	 */
	private void doIncr(Map<String, Integer> subMap, String subCat, int freq){
		Integer curFreq = subMap.get(subCat);
		if(ObjectUtils.isNull(curFreq)) subMap.put(subCat, freq);
		else subMap.put(subCat, curFreq + freq);
	}

	/**
	 * 
	 * @param unsortMap
	 * @param desc
	 * @return
	 */
	public static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean desc) {

		// Convert Map to List
		List<Map.Entry<String, Integer>> list = 
				new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				if(!desc)
					return (o1.getValue()).compareTo(o2.getValue());
				else 
					return (-1)*(o1.getValue()).compareTo(o2.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
}
