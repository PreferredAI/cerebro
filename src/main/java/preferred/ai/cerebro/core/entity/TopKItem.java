package preferred.ai.cerebro.core.entity;

import java.io.Serializable;
/**
 * Definition of a top-k item 
 * @author ddle.2015
 *
 */
public class TopKItem implements Serializable{
	private static final long serialVersionUID = 7606047916434415063L;
	
	private String itemId;
	private double score;
	
	public TopKItem() {}
	
	public TopKItem(String itemId, double score) {
		super();
		this.itemId = itemId;
		this.score = score;
	}

	public String getItemId() { return itemId; }
	public void setItemId(String itemId) { this.itemId = itemId; }
	
	public double getScore() { return score; }
	public void setScore(double score) { this.score = score; }
}
