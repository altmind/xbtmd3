package cwr.exchanges;

import jodd.jerry.Jerry;

public class CoinMarketCapRecord {
  public String exchange;
  public String pair;
  public Double volume_usd;
  public Double volume_btc;
  public String volume_native;
  public Double price_usd;
  public Double price_btc;
  public String price_native;
  public boolean no_fees;

  public CoinMarketCapRecord(String exchange,
                             String pair,
                             Double volume_usd,
                             Double volume_btc,
                             String volume_native,
                             Double price_usd,
                             Double price_btc,
                             String price_native,
                             boolean no_fees) {
    this.exchange = exchange;
    this.pair = pair;
    this.volume_usd = volume_usd;
    this.volume_btc = volume_btc;
    this.volume_native = volume_native;
    this.price_usd = price_usd;
    this.price_btc = price_btc;
    this.price_native = price_native;
    this.no_fees = no_fees;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("CoinMarketCapRecord{");
    sb.append("exchange='").append(exchange).append('\'');
    sb.append(", pair='").append(pair).append('\'');
    sb.append(", volume_usd=").append(volume_usd);
    sb.append(", volume_btc=").append(volume_btc);
    sb.append(", volume_native='").append(volume_native).append('\'');
    sb.append(", price_usd=").append(price_usd);
    sb.append(", price_btc=").append(price_btc);
    sb.append(", price_native='").append(price_native).append('\'');
    sb.append(", no_fees=").append(no_fees);
    sb.append('}');
    return sb.toString();
  }
}
