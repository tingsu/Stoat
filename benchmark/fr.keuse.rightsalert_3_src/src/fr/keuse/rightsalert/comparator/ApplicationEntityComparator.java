package fr.keuse.rightsalert.comparator;

import java.util.Comparator;

import android.util.Log;

import fr.keuse.rightsalert.entity.ApplicationEntity;

public class ApplicationEntityComparator implements Comparator<ApplicationEntity> {
	private String sort;
	
	public ApplicationEntityComparator(String sort) {
		this.sort = sort;
	}
	
	public int compare(ApplicationEntity lae, ApplicationEntity rae) {
		if("score".equals(sort))
			return scoreCompare(lae, rae);
		else if("name".equals(sort))
			return nameCompare(lae, rae);
		else {
			Log.w("error","Unknown sort type");
			return nameCompare(lae, rae);
		}
	}
	
	private int nameCompare(ApplicationEntity lae, ApplicationEntity rae) {
		return lae.getName().toLowerCase().compareTo(rae.getName().toLowerCase());
	}
	
	private int scoreCompare(ApplicationEntity lae, ApplicationEntity rae) {
		Integer laeScore = lae.getScore();
		return laeScore.compareTo(rae.getScore());
	}
}
