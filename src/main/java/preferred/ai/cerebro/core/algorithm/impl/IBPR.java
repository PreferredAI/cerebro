package preferred.ai.cerebro.core.algorithm.impl;

import preferred.ai.cerebro.core.algorithm.Algorithm;
import preferred.ai.cerebro.core.entity.AbstractVector;
/**
 * This class implements the IBPR algorithms
 * @author ddle.2015
 *
 */
public class IBPR extends Algorithm{

	@Override
	public double score(AbstractVector userVector, AbstractVector itemVector) {
		// TODO Auto-generated method stub
		double unorm = Math.sqrt(userVector.innerProduct(userVector));
		double inorm = Math.sqrt(itemVector.innerProduct(itemVector));
		return Math.acos(userVector.innerProduct(itemVector)/(unorm * inorm));
	}

	@Override
	public void update(AbstractVector userVector, AbstractVector v, double multiplier) {
		// TODO Auto-generated method stub
		int nbFactors = getParams().getValueAsInt("nbFactors");
		double learnRate = getParams().getValueAsDouble("learnRate");
		double userReg = getParams().getValueAsDouble("userReg");
		
		double unorm = Math.sqrt(userVector.innerProduct(userVector));
		double vnorm = Math.sqrt(v.innerProduct(v));
		double cos_uv = Math.cos(score(userVector, v));
		
		for (int f = 0; f < nbFactors; f++) {
			double uf = userVector.getElement(f);
			double vf = v.getElement(f);
			
			userVector.setElement(f, uf + learnRate * (1/unorm * (- (- vf/vnorm +   cos_uv * uf/unorm)/Math.sqrt(1 - cos_uv * cos_uv)) - userReg * uf));
		}
	}

	@Override
	public String getType() {
		return "bpr";
	}

}
