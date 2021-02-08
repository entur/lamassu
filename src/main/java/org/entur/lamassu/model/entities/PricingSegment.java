package org.entur.lamassu.model.entities;

public class PricingSegment {
    private Integer start;
    private Float rate;
    private Integer interval;
    private Integer end;

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "PricingSegment{" +
                "start=" + start +
                ", rate=" + rate +
                ", interval=" + interval +
                ", end=" + end +
                '}';
    }
}
