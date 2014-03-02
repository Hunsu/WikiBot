package org.wikipedia.tvseries.model;

import java.util.List;

public class Season {

    private List<Episode> episodes;
    private String serie;
    private String number;

    public Season(String name, String season) {
	serie = name;
	number = season;
    }

    /**
     * @return the serie
     */
    public String getSerie() {
	return serie;
    }

    /**
     * @param serie
     *            the serie to set
     */
    public void setSerie(String serie) {
	this.serie = serie;
    }

    /**
     * @return the number
     */
    public String getNumber() {
	return number;
    }

    /**
     * @param number
     *            the number to set
     */
    public void setNumber(String number) {
	this.number = number;
    }

    /**
     * @param episodes
     *            the episodes to set
     */
    public void setEpisodes(List<Episode> episodes) {
	this.episodes = episodes;
    }

    public List<Episode> getEpisodes(){
	return episodes;
    }

}
