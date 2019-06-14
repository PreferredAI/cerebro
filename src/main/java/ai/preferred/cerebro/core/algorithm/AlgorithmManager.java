package ai.preferred.cerebro.core.algorithm;

import ai.preferred.cerebro.core.algorithm.impl.IBPR;
import ai.preferred.cerebro.core.algorithm.impl.PMF;

public class AlgorithmManager {
	private Algorithm[] algorithms;

	public AlgorithmManager() {
		this(new Algorithm[] {
				new PMF(),
				new IBPR()
		});
	}
	
	public AlgorithmManager(Algorithm ... algorithms) { this.algorithms = algorithms; }

	public Algorithm getAlgorithm(String modelCode, String settingStr){
		for(Algorithm alg:algorithms) {
			if(modelCode.contains(alg.getType())) {
				alg.loadSettings(settingStr);
				return alg;
			}
		}
		return null;
	}
}
