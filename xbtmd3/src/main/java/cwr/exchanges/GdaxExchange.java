package cwr.exchanges;

import cwr.DataSaver;
import info.bitrich.xchangestream.bitstamp.BitstampStreamingExchange;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.logging.Logger;

public class GdaxExchange extends Exchange implements Runnable {



  public GdaxExchange(DataSaver ds, CurrencyPair... currencyPairs) {
    super(ds, currencyPairs);
    exchangeName = GDAXStreamingExchange.class;
    LOGGER = Logger.getLogger(GdaxExchange.class.getName());
  }


}
