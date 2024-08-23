package snurkabill.net.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Brick {

	private int volume;

	private Bin bin;

	public void setVolume(int volume) {
		this.volume = volume;
	}

	@PlanningVariable(valueRangeProviderRefs = { "bins" })
	@JsonIgnore
	public Bin getBin() {
		return bin;
	}

	public void setBin(Bin bin) {
		this.bin = bin;
	}

	public int getVolume() {
		return volume;
	}

	@Override
	public String toString() {
		return "Brick [" + volume + "]";
	}
}
