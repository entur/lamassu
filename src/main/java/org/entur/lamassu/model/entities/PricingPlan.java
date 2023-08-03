package org.entur.lamassu.model.entities;

import java.util.List;

public class PricingPlan implements Entity {

  private String id;
  private TranslatedString name;
  private String url;
  private String currency;
  private Float price;
  private Boolean isTaxable;
  private TranslatedString description;
  private List<PricingSegment> perKmPricing;
  private List<PricingSegment> perMinPricing;
  private Boolean surgePricing;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TranslatedString getName() {
    return name;
  }

  public void setName(TranslatedString name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public Boolean getTaxable() {
    return isTaxable;
  }

  public void setTaxable(Boolean taxable) {
    isTaxable = taxable;
  }

  public TranslatedString getDescription() {
    return description;
  }

  public void setDescription(TranslatedString description) {
    this.description = description;
  }

  public List<PricingSegment> getPerKmPricing() {
    return perKmPricing;
  }

  public void setPerKmPricing(List<PricingSegment> perKmPricing) {
    this.perKmPricing = perKmPricing;
  }

  public List<PricingSegment> getPerMinPricing() {
    return perMinPricing;
  }

  public void setPerMinPricing(List<PricingSegment> perMinPricing) {
    this.perMinPricing = perMinPricing;
  }

  public Boolean getSurgePricing() {
    return surgePricing;
  }

  public void setSurgePricing(Boolean surgePricing) {
    this.surgePricing = surgePricing;
  }

  @Override
  public String toString() {
    return (
      "PricingPlan{" +
      "id='" +
      id +
      '\'' +
      ", name='" +
      name +
      '\'' +
      ", url='" +
      url +
      '\'' +
      ", currency='" +
      currency +
      '\'' +
      ", price=" +
      price +
      ", isTaxable=" +
      isTaxable +
      ", description='" +
      description +
      '\'' +
      ", perKmPricing=" +
      perKmPricing +
      ", perMinPricing=" +
      perMinPricing +
      ", surgePricing=" +
      surgePricing +
      '}'
    );
  }
}
