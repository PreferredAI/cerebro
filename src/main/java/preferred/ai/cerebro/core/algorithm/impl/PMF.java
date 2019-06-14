package preferred.ai.cerebro.core.algorithm.impl;

import preferred.ai.cerebro.core.algorithm.Algorithm;
import preferred.ai.cerebro.core.entity.AbstractVector;
/**
 * This class implements the PMF algorithm
 * @author ddle.2015
 *
 */
public class PMF extends Algorithm{

	@Override
	public double score(AbstractVector userVector, AbstractVector itemVector) {
		double globalBias = getParams().getValueAsDouble("globalBias");
		return userVector.innerProduct(itemVector) + globalBias;
	}

	@Override
	public void update(AbstractVector userVector, AbstractVector v, double multiplier) {
		int nbFactors = getParams().getValueAsInt("nbFactors");
		double learnRate = getParams().getValueAsDouble("learnRate");
		double userReg = getParams().getValueAsDouble("userReg");
		
		for(int f = 0; f < nbFactors; f++){
			double u_f = userVector.getElement(f);
			double i_f = v.getElement(f);
			
			double delta_u = -multiplier*i_f - userReg*u_f;
			double newVal = u_f - learnRate*delta_u;
			userVector.setElement(f, newVal);
		}
	}
	
	@Override
	public String getType() {
		return "pmf";
	}

}
